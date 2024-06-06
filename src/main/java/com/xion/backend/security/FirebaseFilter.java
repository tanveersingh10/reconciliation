package com.xion.backend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.xion.backend.security.properties.SecurityProperties;
import com.xion.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class FirebaseFilter extends OncePerRequestFilter {

	private static Logger logger = Logger.getLogger(FirebaseFilter.class.getName());

	@Value("${spring.profiles.active}")
	private String activeProfile;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private CookieUtils cookieUtils;

	@Autowired
	private SecurityProperties securityProps;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		boolean authenticated;
		if (activeProfile.equals("local")){
			authenticated = true;
			UserPrincipal user = (UserPrincipal) userDetailsService.loadUserByUsername("alex@counto.in");
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
					new Credentials(Credentials.CredentialType.SESSION, null, null, null), null);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}else{
			authenticated = verifyToken(request);
		}

		if (!authenticated){
			if(activeProfile.equals("prod"))
				response.sendRedirect("https://auth.xion.ai/?source=widgets");
			else
				response.sendRedirect("https://auth-dev.xion.ai/?source=widgets");
			return;
		}

		filterChain.doFilter(request, response);

	}

	private boolean verifyToken(HttpServletRequest request) {
		String session = null;
		FirebaseToken decodedToken = null;
		Credentials.CredentialType type = null;
		boolean strictServerSessionEnabled = securityProps.getFirebaseProps().isEnableStrictServerSession();
		Cookie sessionCookie = cookieUtils.getCookie("counto_session");
		String token = securityService.getBearerToken(request);
		try {
			if (sessionCookie != null) {
				session = sessionCookie.getValue();
				if (securityService.getTokenTimestampMap().containsKey(session) && within5Minutes(securityService.getTokenTimestampMap().get(session).getLeft())){
					Pair<Date, FirebaseToken> dateFirebaseTokenPair = securityService.getTokenTimestampMap().get(session);
					decodedToken = dateFirebaseTokenPair.getRight();
				}else {
					decodedToken = FirebaseAuth.getInstance().verifySessionCookie(session,
							securityProps.getFirebaseProps().isEnableCheckSessionRevoked());
					securityService.getTokenTimestampMap().put(token, new Pair<>(new Date(), decodedToken));
				}
				type = Credentials.CredentialType.SESSION;
			} else if (!strictServerSessionEnabled) {
				if (token != null && !token.equalsIgnoreCase("undefined")) {
					if (securityService.getTokenTimestampMap().containsKey(token) && within5Minutes(securityService.getTokenTimestampMap().get(token).getLeft())){
						Pair<Date, FirebaseToken> dateFirebaseTokenPair = securityService.getTokenTimestampMap().get(token);
						decodedToken = dateFirebaseTokenPair.getRight();
					}else {
						decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
						securityService.getTokenTimestampMap().put(token, new Pair<>(new Date(), decodedToken));
					}
					type = Credentials.CredentialType.ID_TOKEN;
				}
			}
		} catch (FirebaseAuthException e) {
//			e.printStackTrace();
//			logger.severe("Firebase Exception: " + e.getLocalizedMessage());
			return false;
		}

		UserPrincipal user = decodeUserPrincipalCookie(decodedToken);
		if (user != null) {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
					new Credentials(type, decodedToken, token, session), null);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContext context = SecurityContextHolder.getContext();
			context.setAuthentication(authentication);
		}
		if (user == null)
			return false;
		return true;
	}

	private UserPrincipal decodeUserPrincipalCookie(FirebaseToken decodedToken) {
		if (decodedToken == null)
			return null;
		Object fromSession =null;// UI.getCurrent().getSession().getAttribute(decodedToken.getEmail());
		if (fromSession != null){
			return (UserPrincipal) fromSession;
		}else {
			return (UserPrincipal) userDetailsService.loadUserByUsername(decodedToken.getEmail());
		}
	}

	private boolean within5Minutes(Date time){
		long MAX_DURATION = MILLISECONDS.convert(5, MINUTES);
		long duration = new Date().getTime() - time.getTime();
		return duration <= MAX_DURATION;
	}

}

package com.xion.backend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.xion.backend.security.properties.SecurityProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private SecurityProperties securityProperties;

    public FirebaseAuthenticationSuccessHandler(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        try {
            List<String> userRoles = new ArrayList<>();
            userPrincipal.getAuthorities().forEach( a -> userRoles.add(a.getAuthority()));

            Map<String, Object> additionalClaims = new HashMap<String, Object>();
            additionalClaims.put("email", userPrincipal.getEmail());
            additionalClaims.put("role", userRoles.get(0));
            additionalClaims.put("perms", userPrincipal.getUserPermissions());
            additionalClaims.put("client", userPrincipal.getClientID());
            String token = FirebaseAuth.getInstance().createCustomToken(userPrincipal.getId()+"");

            Cookie cookie = new Cookie("session", token);
            cookie.setSecure(securityProperties.getCookieProps().isSecure());
            cookie.setHttpOnly(securityProperties.getCookieProps().isHttpOnly());
            cookie.setPath(securityProperties.getCookieProps().getPath());
            cookie.setDomain(securityProperties.getCookieProps().getDomain());
            cookie.setMaxAge(securityProperties.getCookieProps().getMaxAgeInMinutes());

            response.addCookie(cookie);

        } catch (FirebaseAuthException e) {
            e.printStackTrace();
        }
    }
}

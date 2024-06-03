package com.xion.backend.security;

import com.google.firebase.auth.FirebaseToken;
import com.xion.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class SecurityService {

    private static Logger logger = Logger.getLogger(SecurityService.class.getName());

    private HashMap<String, Pair<Date, FirebaseToken>> tokenTimestampMap = new HashMap<>();

    @Autowired
    @Qualifier("firebaseRestTemplate")
    private RestTemplate restTemplate;

    private static class FirebaseLoginRequest{
        private String email;
        private String password;
        private boolean returnSecureToken;

        public FirebaseLoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
            this.returnSecureToken = true;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isReturnSecureToken() {
            return returnSecureToken;
        }

        public void setReturnSecureToken(boolean returnSecureToken) {
            this.returnSecureToken = returnSecureToken;
        }
    }

    public String getBearerToken(HttpServletRequest request) {
        String bearerToken = null;
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7);
        }
        return bearerToken;
    }

    public Map<String, String> login(String userName, String password) throws Exception{
        try{
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=AIzaSyB3TLuVvz6j5kyVKDTslcWSvG5m43Ibamk";
            FirebaseLoginRequest firebaseLoginRequest = new FirebaseLoginRequest(userName, password);

            HttpEntity<FirebaseLoginRequest> request = new HttpEntity<>(firebaseLoginRequest);


            ResponseEntity response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Object.class
            );

            Map<String, Object> body = (Map<String, Object>) response.getBody();
            String idToken = (String) body.get("idToken");
            String refreshToken = (String) body.get("refreshToken");

            Map<String, String> ret = new HashMap<>();
            ret.put("idToken", idToken);
            ret.put("refreshToken", refreshToken);

            return ret;
        }catch (Exception e){
            logger.severe(e.getMessage());
            throw new Exception("Could not log in " + userName);
        }
    }

    public HashMap<String, Pair<Date, FirebaseToken>> getTokenTimestampMap() {
        return tokenTimestampMap;
    }

    public void setTokenTimestampMap(HashMap<String, Pair<Date, FirebaseToken>> tokenTimestampMap) {
        this.tokenTimestampMap = tokenTimestampMap;
    }
}
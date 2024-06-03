package com.xion.backend.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("security")
public class SecurityProperties {

    CookieProperties cookieProps;
    FireBaseProperties firebaseProps;
    boolean allowCredentials;
    List<String> allowedOrigins;
    List<String> allowedHeaders;
    List<String> exposedHeaders;
    List<String> allowedMethods;
    List<String> allowedPublicApis;

    public CookieProperties getCookieProps() {
        return cookieProps;
    }

    public void setCookieProps(CookieProperties cookieProps) {
        this.cookieProps = cookieProps;
    }

    public FireBaseProperties getFirebaseProps() {
        return firebaseProps;
    }

    public void setFirebaseProps(FireBaseProperties firebaseProps) {
        this.firebaseProps = firebaseProps;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedPublicApis() {
        return allowedPublicApis;
    }

    public void setAllowedPublicApis(List<String> allowedPublicApis) {
        this.allowedPublicApis = allowedPublicApis;
    }
}
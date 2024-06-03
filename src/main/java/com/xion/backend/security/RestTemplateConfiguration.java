package com.xion.backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

//    @Bean
//    @ConfigurationProperties("client.oauth2.client")
//    protected ClientCredentialsResourceDetails oAuthBillingDetails() {
//        return new ClientCredentialsResourceDetails();
//    }
//
//    @Bean(name = "cashRestTemplate")
//    protected RestTemplate cashRestTemplate() {
//        return new OAuth2RestTemplate(oAuthBillingDetails());
//    }


    @Bean
    @ConfigurationProperties("cash.oauth2.client")
    protected ClientCredentialsResourceDetails oAuthCashService() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    @ConfigurationProperties("comments.oauth2.client")
    protected ClientCredentialsResourceDetails oAuthCommentsService() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean
    @ConfigurationProperties("payments.oauth2.client")
    protected ClientCredentialsResourceDetails oAuthPaymentsService() {
        return new ClientCredentialsResourceDetails();
    }

    @Bean(name = "cashRestTemplate")
    protected RestTemplate cashRestTemplate() {
        return new OAuth2RestTemplate(oAuthCashService());
    }

    @Bean(name = "commentsRestTemplate")
    protected RestTemplate commentsRestTemplate() {
        return new OAuth2RestTemplate(oAuthCommentsService());
    }

    @Bean(name = "paymentsRestTemplate")
    protected RestTemplate paymentsRestTemplate() {
        return new OAuth2RestTemplate(oAuthCommentsService());
    }

    @Bean(name = "firebaseRestTemplate")
    protected RestTemplate firebaseRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "genericRestTemplate")
    protected RestTemplate genericRestTemplate() {
        return new RestTemplate();
    }
}

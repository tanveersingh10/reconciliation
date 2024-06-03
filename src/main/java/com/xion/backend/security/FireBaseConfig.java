package com.xion.backend.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Configuration
public class FireBaseConfig {

    private static Logger logger = Logger.getLogger(FireBaseConfig.class.getName());

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Primary
    @Bean
    public void firebaseInit() {
        InputStream inputStream = null;
        try {
            if (activeProfile.equals("prod"))
                inputStream = new ClassPathResource("serviceAccountKeyProd.json").getInputStream();
            else
                inputStream = new ClassPathResource("serviceAccountKeyDev.json").getInputStream();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        try {

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            logger.info("Firebase Initialize");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
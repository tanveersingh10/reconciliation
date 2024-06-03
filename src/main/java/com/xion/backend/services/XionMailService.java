package com.xion.backend.services;

import com.xion.app.dto.SendSimpleTemplateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class XionMailService {

    private static Logger logger = Logger.getLogger(XionMailService.class.getName());

    @Value("#{ @environment['client.xionMailBaseURL'] }")
    private String serverBaseUrl;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    private RestTemplate restTemplate = new RestTemplate();

    public void sendInviteEmail(String email, String name, String link){
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("LINK", link);
        replacementMap.put("NAME", name);
        replacementMap.put("ENV", getEnv());

        Map<String, List<String>> toMap = new HashMap<>();
        List<String> to = new ArrayList<>();
        to.add(email);
        toMap.put("to", to);

        SendSimpleTemplateRequest sstr = new SendSimpleTemplateRequest();
        sstr.setId("counto-invite");
        sstr.setReplacementMap(replacementMap);
        sstr.setSubject("You have been invited to Counto");
        sstr.setTo(toMap);

        sendSendSimpleTemplateRequest(sstr);
    }

    public void sendPasswordResetEmail(String email, String name, String link){
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("LINK", link);
        replacementMap.put("NAME", name);
        replacementMap.put("ENV", getEnv());

        Map<String, List<String>> toMap = new HashMap<>();
        List<String> to = new ArrayList<>();
        to.add(email);
        toMap.put("to", to);

        SendSimpleTemplateRequest sstr = new SendSimpleTemplateRequest();
        sstr.setId("counto-password-reset");
        sstr.setReplacementMap(replacementMap);
        sstr.setSubject("You have been invited to Counto");
        sstr.setTo(toMap);

        sendSendSimpleTemplateRequest(sstr);

    }

    private boolean sendSendSimpleTemplateRequest(SendSimpleTemplateRequest sstr){
        HttpEntity<SendSimpleTemplateRequest> request = new HttpEntity<>(sstr);
        ResponseEntity<Void> response = null;
        try {
            String url = serverBaseUrl + "/send/template/simple";
            logger.info("sending to: " + url);
            response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            logger.severe("Error sending mail for " + request);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getEnv(){
        return activeProfile.equals("prod") ?
                "" :
                "-" + activeProfile;
    }

}

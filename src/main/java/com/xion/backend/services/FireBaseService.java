package com.xion.backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.xion.components.UserService;
import com.xion.models.user.User;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.logging.Logger;



@Service
public class FireBaseService {
    private static Logger logger = Logger.getLogger(FireBaseService.class.getName());

    private UserService userService;
    private XionMailService xionMailService;

    public FireBaseService(UserService userService, XionMailService xionMailService) {
        this.userService = userService;
        this.xionMailService = xionMailService;
    }

    public void sendPasswordReset(User user) throws Exception{
        String link = FirebaseAuth.getInstance().generatePasswordResetLink(user.getEmail());
        xionMailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName() + " " + user.getLastName(), link);
    }

    public void inviteUser(User user) throws Exception{
        try {
            if (userService.existsByEmail(user.getEmail()))
                throw new Exception("User with email: " + user.getEmail() + " already exists");
            if (userService.existsByPhone(user.getPhoneNumber()))
                throw new Exception("User with phone number: " + user.getPhoneNumber() + " already exists");

            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest();

            createRequest.setDisplayName(user.getFirstName() + " " + user.getLastName());
            createRequest.setEmail(user.getEmail());
            String password = generatePassword(16);
            createRequest.setPassword(password);
            createRequest.setPhoneNumber(user.getPhoneNumber());
            createRequest.setEmailVerified(true);

            FirebaseAuth.getInstance().createUser(createRequest);

            String link = FirebaseAuth.getInstance().generatePasswordResetLink(user.getEmail());

            xionMailService.sendInviteEmail(user.getEmail(), user.getFirstName() + " " + user.getLastName(), link);
            userService.save(user);
        }catch (Exception e){
            logger.info(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static String generatePassword(int length) {
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[length];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));

        for(int i = 4; i< length ; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return new String(password);
    }

}

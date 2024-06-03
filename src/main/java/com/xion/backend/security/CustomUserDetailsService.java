package com.xion.backend.security;

import com.xion.components.UserRepository;
import com.xion.exceptions.ResourceNotFoundException;
import com.xion.models.user.Role;
import com.xion.models.user.User;
import com.xion.models.user.permissions.Company;
import com.xion.models.user.permissions.PermissionDAO;
import com.xion.models.user.permissions.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private PermissionDAO permissionDAO;

    @Value("#{ @environment['intercom.secret'] }")
    private String intercomSecret;

    private static Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private Map<String, User> emailUserMap;

    public CustomUserDetailsService(UserRepository userRepository, PermissionDAO permissionDAO) {
        this.userRepository = userRepository;
        this.permissionDAO = permissionDAO;
        emailUserMap = new HashMap<>();
    }

    public User refreshUser(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email : " + email)
                );

        assignIntercomHash(user);
        user = loadPermissions(user);
        emailUserMap.put(email, user);
        return user;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        if (emailUserMap.containsKey(email))
            return UserPrincipal.create(emailUserMap.get(email));

        try {
            User user = refreshUser(email);
            return UserPrincipal.create(user);
        } catch (Exception e) {
            String msg = "No permissions loaded for user: " + email;
            logger.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    @Transactional
    public List<User> loadAllUsers(long clientId){
        List<User> users =  userRepository.findAllByClientID(clientId).stream().map( user -> {
            try {
                logger.info("Attempting to load permissions for: " + user.getEmail());
                assignIntercomHash(user);
                return loadPermissions(user);
            } catch (Exception e) {
                logger.warn("No permissions loaded for user: " + user.getEmail());
                logger.warn(e.getMessage());
                e.printStackTrace();
                return user;
            }
        }).collect(Collectors.toList());
        return users;
    }


    @Transactional
    public UserDetails loadUserPrincipalById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );

        try {
            return UserPrincipal.create(loadPermissions(user));
        } catch (Exception e) {
            logger.warn("No permissions loaded for user: " + id);
            logger.warn(e.getMessage());
            e.printStackTrace();
            return UserPrincipal.create(user);
        }
    }

    @Transactional
    public User loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id)
        );

        assignIntercomHash(user);

        try {
            return loadPermissions(user);
        } catch (Exception e) {
            logger.warn("No permissions loaded for user: " + id);
            return user;
        }
    }

    @Transactional
    public List<User> loadUserByIds(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);

        for (User user : users) {
            assignIntercomHash(user);
            try {
                user = loadPermissions(user);
            } catch (Exception e) {
                logger.warn("No permissions loaded for user: " + user.getId());
            }
        }

        return users;

    }

    @Transactional
    public User save(User user) throws Exception{
        try {
            assignIntercomHash(user);
            return userRepository.save(user);
        }catch (RuntimeException e){
            logger.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @Transactional
    public User loadByEmail(String email){
        User user =  userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );

        assignIntercomHash(user);

        try {
            return loadPermissions(user);
        } catch (Exception e) {
            logger.warn("No permissions loaded for user: " + email);
            return user;
        }
    }

    @Transactional
    public boolean existsByEmail(String email){ return userRepository.existsByEmail(email); }

    @Transactional
    public void addAndSavePermission(User user, Company company, Product product, Role role) throws Exception {
        try {
            user.getPermissionWrapper().addPermission(company.getPermID(), product, role);
            permissionDAO.writePermission(user.getId(), company, product, role);
        }catch (RuntimeException e){
            e.printStackTrace();
            String msg = "Error running addAndSavePermission with " +
                    user.getEmail() + " " +
                    company.getCompanyID() + " " +
                    product + " " +
                    role;
            logger.error( msg );
            logger.error(e.getMessage());
            throw new Exception( msg );
        }
    }

    private User loadPermissions(User user) throws Exception {
        user.setPermissionWrapper(permissionDAO.getPermissions(user));
        logger.info("Permissions set for user: " + user.getEmail());
        return user;
    }

    @Transactional
    public boolean isEmailVerified(String email) throws Exception {
        logger.info("checking if email is verified for: " + email);
        User user = userRepository.findByEmail(email).orElseThrow( () ->
                new Exception("No User found for email: " + email)
        );

        return user.isEmailVerified();
    }

    @Transactional
    public void saveClientID(long userID, long clientID) throws Exception {
        logger.info("Attempting to save clientID for user: " + userID);
        save(loadUserById(userID).setClientID(clientID));

    }

    private void assignIntercomHash(User user){
        if (user.getIntercomUserHash()==null || user.getIntercomUserHash().isBlank()) {
            try {
                Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret_key = new SecretKeySpec(intercomSecret.getBytes(), "HmacSHA256");
                sha256_HMAC.init(secret_key);

                byte[] hash = (sha256_HMAC.doFinal(user.getEmail().getBytes()));
                StringBuffer result = new StringBuffer();
                for (byte b : hash) {
                    result.append(String.format("%02x", b));
                }
                user.setIntercomUserHash(result.toString());
            } catch (Exception e) {
                logger.error("error in creating stripe key");
            }
        }
    }

}

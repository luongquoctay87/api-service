package com.example.sample.service;

import com.example.sample.config.Translator;
import com.example.sample.exception.ResourceNotFoundException;
import com.example.sample.model.AppUser;
import com.example.sample.model.Token;
import com.example.sample.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "USER_SERVICE")
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private MailService mailService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AppUser appUser = userRepo.findByUsername(username);
        if (appUser == null) {
            log.info("User not found in the database");
            throw new UsernameNotFoundException(Translator.toLocale("user-name-not-found"));
        } else {
            log.info("User found in the database {}", username);
        }
        return new org.springframework.security.core.userdetails.User(appUser.getUsername(), appUser.getPassword(), getAuthority(username));
    }

    /**
     * Get user authorities
     *
     * @param username
     * @return
     */
    private List<SimpleGrantedAuthority> getAuthority(String username) {
        List<String> roles = userRepo.findRoleByUsername(username);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
            authorities.add(authority);
        }

        return authorities;
    }

    /**
     * Get all users
     *
     * @param keyword
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Page<AppUser> findAll(String keyword, int pageNo, int pageSize) {
        log.info("Fetching all users from the database");

        String name = "%";
        if (StringUtils.isNotBlank(keyword)) {
            name = name + keyword.toLowerCase() + "%";
        }

        if (pageNo > 0) pageNo = pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<AppUser> pagedResult = userRepo.findAllUsers(name, pageable);
        return pagedResult;
    }

    /**
     * Get user by id
     *
     * @param id
     * @return
     * @throws ResourceNotFoundException
     */
    public AppUser getById(long id) throws ResourceNotFoundException {
        log.info("Fetching user {} from the database", id);
        Optional<AppUser> user = Optional.ofNullable(userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Translator.toLocale("user-id-not-found"))));
        return user.get();
    }

    /**
     * Get user by email
     *
     * @param email
     * @return
     * @throws ResourceNotFoundException
     */
    public AppUser getByEmail(String email) throws ResourceNotFoundException {
        log.info("Fetching user {} from the database", email);
        AppUser user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException(Translator.toLocale("user-email-not-found"));
        }
        return user;
    }

    /**
     * Delete terminate user
     *
     * @param id
     * @throws ResourceNotFoundException
     */
    public void delete(long id) throws ResourceNotFoundException {
        log.info("Deleting user {} from the database", id);
        getById(id);
        userRepo.deleteById(id);
    }

    /**
     * Save user
     *
     * @param user
     * @return
     */
    public AppUser save(AppUser user) {
        log.info("Adding user {} to the database", user.getUsername());
        return userRepo.save(user);
    }

    /**
     * Send link confirm in order to reset password token to email
     *
     * @param email
     * @throws ResourceNotFoundException
     * @throws MessagingException
     */
    public void sendResetTokenToEmail(String email) throws ResourceNotFoundException, MessagingException {
        // get user by email
        AppUser appUser = getByEmail(email);

        // reset password into database
        String resetToken = RandomStringUtils.randomNumeric(10);

        // Todo: replace domain name in order to navigate to change password page
        String resetLink = "http://domain.name/reset-password?token=" + resetToken;

        // send new password to email
        // Todo: Need to replace email content
        String from = "no-reply@service.api";
        String subject = "[API-Service] Reset password request";
        String body = String.format("Dear %s,\n\n" +
                "If your forgot password\n" +
                "Click link to reset password: %s\n\n" +
                "- BackEnd Team", appUser.getUsername(), resetLink);

        // send mail to user
        mailService.sendEmail(from, email, subject, body);

        // save reset password token into the database
        tokenService.saveResetToken(appUser, resetToken);
    }

   /**
     * Reset old password and update new password
     *
     * @param resetToken
     * @param newPassword
     */
    public void resetAndUpdatePassword(String resetToken, String newPassword) throws ResourceNotFoundException {
        Token token = tokenService.findByToken(resetToken);
        if (token == null) {
            throw new ResourceNotFoundException(Translator.toLocale("reset-token-not-found"));
        }
        if (token.getExpiryDate().before(new Date())) {
            throw new ResourceNotFoundException(Translator.toLocale("reset-token-expired"));
        }

        // Update new password
        AppUser appUser = token.getUser();
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        appUser.setPassword(encoder.encode(newPassword));
        userRepo.save(appUser);
    }

}

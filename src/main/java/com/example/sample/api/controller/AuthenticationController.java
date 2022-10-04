package com.example.sample.api.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.sample.api.response.ApiResponse;
import com.example.sample.api.response.TokenResponse;
import com.example.sample.config.Translator;
import com.example.sample.exception.ResourceNotFoundException;
import com.example.sample.model.Token;
import com.example.sample.service.TokenService;
import com.example.sample.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.sample.util.ApiConst.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(HttpServletRequest request, @RequestParam("username") String username, @RequestParam("password") String password) {
        log.info("Username is {} and Password is {}", username, password);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User user = (User) authentication.getPrincipal();
        TokenResponse tokens = tokenService.generateToken(user, request.getRequestURL().toString());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request) throws ResourceNotFoundException {
        log.info("Refreshing the token from database");

        String authorizeHeader = request.getHeader(AUTHORIZATION);
        if (authorizeHeader != null && authorizeHeader.startsWith(JWT_BEARER)) {
            String refreshToken = authorizeHeader.substring(JWT_BEARER.length());
            Token token = tokenService.findByToken(refreshToken);
            if (token == null) {
                throw new ResourceNotFoundException(Translator.toLocale("refresh-token-not-found"));
            }

            Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(refreshToken);
            String username = decodedJWT.getSubject();
            User user = (User) userService.loadUserByUsername(username);

            List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            String accessToken = JWT.create().withSubject(username).withExpiresAt(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)).withIssuer(request.getRequestURL().toString()).withClaim(JWT_ROLE, authorities).sign(algorithm);

            TokenResponse tokens = new TokenResponse(user.getUsername(), authorities.toString(), accessToken, refreshToken);
            return ResponseEntity.ok(tokens);
        } else {
            throw new ResourceNotFoundException(Translator.toLocale("refresh-header-not-found"));
        }
    }

    @PostMapping("/forgot-password")
    public ApiResponse forgotPassword(@RequestParam("email") @Email String email) throws MessagingException, ResourceNotFoundException {
        log.info("Sending reset token to email {}", email);
        userService.sendResetTokenToEmail(email);
        return new ApiResponse(ACCEPTED.value(), Translator.toLocale("user-forgot-password-success"));
    }

    @PostMapping("/reset-password")
    public ApiResponse resetPassword(@RequestParam("token") String resetToken, @RequestParam("newPassword") String newPassword) throws ResourceNotFoundException {
        log.info("Processing reset and update new password for token {}", resetToken);
        userService.resetAndUpdatePassword(resetToken, newPassword);
        return new ApiResponse(OK.value(), Translator.toLocale("user-change-password-success"));
    }
}

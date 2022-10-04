package com.example.sample.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.sample.api.response.TokenResponse;
import com.example.sample.exception.ResourceNotFoundException;
import com.example.sample.model.AppUser;
import com.example.sample.model.Token;
import com.example.sample.repository.TokenRepo;
import com.example.sample.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.sample.util.ApiConst.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepo tokenRepo;
    private final UserRepo userRepo;

    /**
     * Generate token information
     * @param user
     * @param url
     * @return
     */
    public TokenResponse generateToken(User user, String url) {
        Algorithm algorithm = Algorithm.HMAC256(JWT_SECRET.getBytes());
        List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .withIssuer(url)
                .withClaim(JWT_ROLE, authorities)
                .sign(algorithm);
        Date expiryDate = new Date(System.currentTimeMillis() + JWT_REFRESH_TOKEN_VALIDITY);

        String refreshToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expiryDate)
                .withIssuer(url)
                .sign(algorithm);

        saveRefreshToken(user.getUsername(), refreshToken, expiryDate);

        return TokenResponse.builder()
                .username(user.getUsername())
                .roles(authorities.toString())
                .access_token(accessToken)
                .refresh_token(refreshToken)
                .build();
    }

    /**
     * Find token by token string
     * @param token
     * @return
     */
    public Token findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    /**
     * Save refresh token after login successful
     *
     * @param username
     * @param refreshToken
     * @param expiryDate
     */
    public void saveRefreshToken(String username, String refreshToken, Date expiryDate) {
        log.info("Saving refresh token to the database");

        AppUser appUser = userRepo.findByUsername(username);

        Token ref = tokenRepo.findByUserId(appUser.getId());
        if (ref != null) {
            ref.setToken(refreshToken);
            ref.setExpiryDate(expiryDate);
            tokenRepo.save(ref);
        } else {
            Token token = Token.builder()
                    .user(appUser)
                    .token(refreshToken)
                    .expiryDate(expiryDate)
                    .build();
            tokenRepo.save(token);
        }
    }

    /**
     * Save reset password token
     *
     * @param appUser
     * @param resetToken Token will be expired after 1 hour
     * @throws ResourceNotFoundException
     */
    public void saveResetToken(AppUser appUser, String resetToken) throws ResourceNotFoundException {
        // Will be expired after 1 hour
        Date expiryDate = new Date(System.currentTimeMillis() + JWT_RESET_TOKEN_VALIDITY);

        Token token = tokenRepo.findByUserId(appUser.getId());
        if (token == null) {
            Token refreshToken = Token.builder()
                    .user(appUser)
                    .token(resetToken)
                    .expiryDate(expiryDate)
                    .build();
            tokenRepo.save(refreshToken);
        } else {
            token.setToken(resetToken);
            token.setExpiryDate(expiryDate);
            tokenRepo.save(token);
        }
    }
}

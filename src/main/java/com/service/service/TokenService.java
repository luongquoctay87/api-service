package com.service.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.service.api.response.TokenResponse;
import com.service.exception.ResourceNotFoundException;
import com.service.model.AppUser;
import com.service.model.Token;
import com.service.repository.TokenRepo;
import com.service.repository.UserRepo;
import com.service.util.ApiConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        Algorithm algorithm = Algorithm.HMAC256(ApiConst.JWT_SECRET.getBytes());
        List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String accessToken = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + ApiConst.JWT_TOKEN_VALIDITY))
                .withIssuer(url)
                .withClaim(ApiConst.JWT_ROLE, authorities)
                .sign(algorithm);
        Date expiryDate = new Date(System.currentTimeMillis() + ApiConst.JWT_REFRESH_TOKEN_VALIDITY);

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
        Date expiryDate = new Date(System.currentTimeMillis() + ApiConst.JWT_RESET_TOKEN_VALIDITY);

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

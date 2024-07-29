package br.com.api.service;

import br.com.api.domain.entities.Users;
import br.com.api.domain.enums.RoleName;
import br.com.api.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateToken(String username, RoleName roleName) {

        String scopes = roleName.name();

        var claims = JwtClaimsSet.builder()
                .issuer("DownloadAndUploadAPI")
                .issuedAt(Instant.now())
                .expiresAt(generateExpiryToken())
                .subject(username)
                .claim("scope", scopes)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public void checkIfTokenIsValid() {

        if(tokenIsStillValid(getExpiryFromAuthentication())) {

            throw new BadRequestException(returnIfTokenIsNoLongerValid());
        }
    }

    public Instant generateExpiryToken() {

        return Instant.now().plusSeconds(3600);
    }

    public String getSubjectFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Jwt jwt = jwtAuthenticationToken.getToken();
            return jwt.getSubject();
        }

        throw new BadRequestException("Unable to decode Token");
    }

    public Instant getExpiryFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Jwt jwt = jwtAuthenticationToken.getToken();
            return jwt.getExpiresAt();
        }

        throw new BadRequestException("Unable to decode Token");
    }

    public boolean tokenIsStillValid(Instant expiresAtToken) {

        return expiresAtToken.isBefore(Instant.now());
    }

    public String returnIfTokenIsNoLongerValid() {

        return "Your token has run out of time, please log in again";
    }

    public void checkIfUserWasDeleted(Users user) {

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException(returnIfTheUserWasDeleted());
        }
    }

    public String returnIfTheUserWasDeleted() {

        return "This user has been deleted";
    }
}

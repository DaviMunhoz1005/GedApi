package br.com.api.service;

import br.com.api.entities.User;
import br.com.api.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateToken(Authentication authentication) {

        String scopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority :: getAuthority)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("DownloadAndUploadAPI")
                .issuedAt(Instant.now())
                .expiresAt(generateExpiryToken())
                .subject(authentication.getName())
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

    public void checkIfUserWasDeleted(User user) {

        if(Boolean.TRUE.equals(user.getExcluded())) {

            throw new BadRequestException(returnIfTheUserWasDeleted());
        }
    }

    public String returnIfTheUserWasDeleted() {

        return "This user has been deleted";
    }
}

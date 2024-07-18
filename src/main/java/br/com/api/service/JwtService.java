package br.com.api.service;

import br.com.api.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    /*
    *
    * TODO - fazer método para pegar o tempo de expiração e ver se o token expirou ou não;
    *
    * */

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

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

    public Instant generateExpiryToken() {

        return Instant.now().plusSeconds(3600L);
    }

    public String getSubjectFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Jwt jwt = jwtAuthenticationToken.getToken();
            return jwt.getSubject();
        }

        throw new BadRequestException("Unable to decode Token");
    }
}

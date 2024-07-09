package br.com.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateToken(Authentication authentication) {

        Instant now = Instant.now();
        long expiryToken = 3600L;

        String scopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority :: getAuthority)
                .collect(Collectors.joining(" "));

        var claims = JwtClaimsSet.builder()
                .issuer("DownloadAndUploadAPI")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiryToken))
                .subject(authentication.getName())
                .claim("scope", scopes)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

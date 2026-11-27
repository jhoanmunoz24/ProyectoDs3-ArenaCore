package com.arenacore.identity.config;

import com.arenacore.identity.domain.Rol;
import com.arenacore.identity.domain.ServiceClient;
import com.arenacore.identity.domain.Usuario;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final RsaKeyProvider rsaKeyProvider;
    private final String issuer;
    private final long userExpirationSeconds;
    private final long serviceExpirationSeconds;

    public JwtTokenService(
            RsaKeyProvider rsaKeyProvider,
            @Value("${arenacore.jwt.issuer:arenacore-identity}") String issuer,
            @Value("${arenacore.jwt.expiration-seconds:28800}") long userExpirationSeconds,
            @Value("${arenacore.jwt.service-expiration-seconds:3600}") long serviceExpirationSeconds) {
        this.rsaKeyProvider = rsaKeyProvider;
        this.issuer = issuer;
        this.userExpirationSeconds = userExpirationSeconds;
        this.serviceExpirationSeconds = serviceExpirationSeconds;
    }

    public String generateUserToken(Usuario usuario) {
        Instant now = Instant.now();
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(usuario.getId().toString())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(userExpirationSeconds)))
                .claim("email", usuario.getEmail())
                .claim("nombreCompleto", usuario.getNombreCompleto())
                .claim("roles", roles)
                .claim("token_type", "USER")
                .build();

        return signClaims(claims);
    }

    public String generateServiceToken(ServiceClient client) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(client.getClientId())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(serviceExpirationSeconds)))
                .claim("service_name", client.getServiceName())
                .claim("roles", List.of("ROLE_SERVICE_INTERNAL"))
                .claim("token_type", "SERVICE_INTERNAL")
                .build();

        return signClaims(claims);
    }

    public long getUserExpirationSeconds() {
        return userExpirationSeconds;
    }

    public long getServiceExpirationSeconds() {
        return serviceExpirationSeconds;
    }

    private String signClaims(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(rsaKeyProvider.getRsaKey().getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new RSASSASigner(rsaKeyProvider.getRsaKey().toPrivateKey()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Error firmando token JWT RS256", e);
        }
    }
}

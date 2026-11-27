package com.arenacore.identity;

import com.arenacore.identity.config.JwtTokenService;
import com.arenacore.identity.config.RsaKeyProvider;
import com.arenacore.identity.domain.Rol;
import com.arenacore.identity.domain.ServiceClient;
import com.arenacore.identity.domain.Usuario;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    @Test
    void shouldSignUserAndServiceTokensWithRs256AndValidateAgainstJwks() throws Exception {
        RsaKeyProvider keyProvider = new RsaKeyProvider();
        JwtTokenService tokenService = new JwtTokenService(keyProvider, "arenacore-identity", 3600, 600);

        Usuario user = new Usuario();
        user.setId(UUID.randomUUID());
        user.setEmail("operador@arenacore.local");
        user.setNombreCompleto("Operador Prueba");
        user.setRoles(Set.of(new Rol(UUID.randomUUID(), "ROLE_OPERADOR", "Operador")));

        String userJwt = tokenService.generateUserToken(user);
        SignedJWT parsedUser = SignedJWT.parse(userJwt);
        assertTrue(parsedUser.verify(new RSASSAVerifier(keyProvider.getPublicKey())));
        assertEquals("USER", parsedUser.getJWTClaimsSet().getStringClaim("token_type"));
        assertTrue(parsedUser.getJWTClaimsSet().getStringListClaim("roles").contains("ROLE_OPERADOR"));

        ServiceClient client = new ServiceClient();
        client.setClientId("courts-service");
        client.setServiceName("Courts Service");
        String svcJwt = tokenService.generateServiceToken(client);
        SignedJWT parsedSvc = SignedJWT.parse(svcJwt);
        assertTrue(parsedSvc.verify(new RSASSAVerifier(keyProvider.getPublicKey())));
        assertEquals("SERVICE_INTERNAL", parsedSvc.getJWTClaimsSet().getStringClaim("token_type"));
        assertTrue(parsedSvc.getJWTClaimsSet().getStringListClaim("roles").contains("ROLE_SERVICE_INTERNAL"));
        assertFalse(keyProvider.getPublicJwkSet().getKeys().isEmpty());
    }
}

package com.arenacore.identity.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class RsaKeyProvider {

    private final RSAKey rsaKey;

    public RsaKeyProvider() {
        try {
            this.rsaKey = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID("arenacore-" + UUID.randomUUID())
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo generar el par de llaves RSA para RS256", e);
        }
    }

    public RSAKey getRsaKey() {
        return rsaKey;
    }

    public RSAPublicKey getPublicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new IllegalStateException("Error obteniendo la llave pública RSA", e);
        }
    }

    public JWKSet getPublicJwkSet() {
        return new JWKSet(rsaKey.toPublicJWK());
    }
}

package com.mhdanh.postgres.tenancy.rsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrivatePublicKeyGenerator {

    private KeyPair keyPair;

    @PostConstruct
    public void generateKeys(){
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);
            keyPair = keyGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PrivatePublicKeyGenerator.class.getName()).log(Level.SEVERE, "Error while generating the RSA key pair", ex);
        }
    }

    public KeyPair getRSAPrivatePublicKey() {
        return keyPair;
    }
    
    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }
    
    public RSAPrivateKey getPrivateKey() {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

}

package com.mhdanh.postgres.tenancy.service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.mhdanh.postgres.tenancy.rsa.PrivatePublicKeyGenerator;
import com.mhdanh.postgres.tenancy.util.MyUtil;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

@RequestScoped
public class TokenService {
	
	@Inject
	private PrivatePublicKeyGenerator keyGenerator;
	
	public String makeToken(String schema, String username, String password) throws JOSEException {
		LocalDateTime now = LocalDateTime.now();
		Builder claimSetBuilder = new JWTClaimsSet.Builder()
				.issuer(username + "|" + password)
				.issueTime(MyUtil.toDate(now))
				.expirationTime(MyUtil.toDate(now.plusMinutes(30)))
				.claim("username", username)
				.claim("schema", schema)
				.claim("user_roles", Arrays.asList("user"));
		
		JWSSigner signer = new RSASSASigner(keyGenerator.getPrivateKey());
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.parse("RS512")), claimSetBuilder.build());
		signedJWT.sign(signer);
		return signedJWT.serialize();
	}
	
	public RSAPublicKey getPublicKey() {
		return keyGenerator.getPublicKey();
	}
	
	public RSAPrivateKey getPrivateKey() {
		return keyGenerator.getPrivateKey();
	}
}

package com.mhdanh.postgres.tenancy.producer;

import java.io.Serializable;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import com.mhdanh.postgres.tenancy.annotation.CurrentSchema;
import com.mhdanh.postgres.tenancy.jwt.Token;
import com.mhdanh.postgres.tenancy.rsa.PrivatePublicKeyGenerator;
import com.mhdanh.postgres.tenancy.util.MyUtil;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@RequestScoped
public class TokenProducer implements Serializable{
    
	private static final long serialVersionUID = 1L;

	@Inject
    private PrivatePublicKeyGenerator keyGenerator;

    @Inject
    private HttpServletRequest request;

    @Produces
    @CurrentSchema
    public Token getTokenInfo() throws Exception {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        
        String tokenAsString = header.substring("Bearer".length()).trim();
        return decodeToken(tokenAsString, keyGenerator.getPublicKey());
    }
    
     private Token decodeToken(String tokenAsString, RSAPublicKey publicKey) throws Exception {
		SignedJWT signedJWT = SignedJWT.parse(tokenAsString);
		JWSVerifier verifier = new RSASSAVerifier(publicKey);
		if (!signedJWT.verify(verifier)) {
		    throw new ParseException("The signature does not match", 0);
		}
		JWTClaimsSet jwtClaimSet = signedJWT.getJWTClaimsSet();
		Token token = new Token();
		token.setSchema(jwtClaimSet.getStringClaim("schema"));
		token.setUsername(jwtClaimSet.getStringClaim("username"));
		token.setExpirationTime(MyUtil.toLocalDateTime(jwtClaimSet.getExpirationTime()));
        return token;
    }
}

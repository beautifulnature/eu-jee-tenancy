package com.mhdanh.postgres.tenancy.jwt;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.mhdanh.postgres.tenancy.annotation.CurrentSchema;
import com.mhdanh.postgres.tenancy.annotation.PublicAPI;


@Provider
@RequestScoped
@Priority(Priorities.AUTHORIZATION)
public class TokenAuthenticationRequestFilter implements ContainerRequestFilter {

	private static Logger LOG = Logger.getLogger(TokenAuthenticationRequestFilter.class.getName());
	
    @Context
    private ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
    	if(info.getResourceClass().isAnnotationPresent(PublicAPI.class) || info.getResourceMethod().isAnnotationPresent(PublicAPI.class)) {
    		return;
    	}
    	Map<String, String> map = new HashMap<>();
        Token token = injectToken();
        if (token == null) {
        	 map.put("code", "403");
        	 map.put("message", "Not allowed");
        	 requestContext.abortWith(Response.status(Status.FORBIDDEN).entity(map).build());
            return;
        } else if(LocalDateTime.now().isAfter(token.getExpirationTime())) {
        	map.put("code", "403");
       	 	map.put("message", "Token expire");
       	 	requestContext.abortWith(Response.status(Status.FORBIDDEN).entity(map).build());
        }

        checkTenantIdsPathParameters(token, requestContext);
    }

    private Token injectToken() {
    	return CDI.current().select(Token.class, new CurrentSchema() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return CurrentSchema.class;
			}
		}).get();
    }

    private void checkTenantIdsPathParameters(Token token, ContainerRequestContext requestContext) {
        String pathTenantId = requestContext.getUriInfo().getPathParameters().getFirst("schema-name");
        if (pathTenantId != null && !pathTenantId.equals(token.getSchema())) {
            LOG.log(Level.INFO, "Exception occurred {0}", "The schema in the token " + token.getSchema()
                    + " does not match the schema in the path " + pathTenantId);
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
        }
    }
}

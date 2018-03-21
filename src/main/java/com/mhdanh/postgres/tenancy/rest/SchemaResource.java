package com.mhdanh.postgres.tenancy.rest;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.mhdanh.postgres.tenancy.annotation.PublicAPI;
import com.mhdanh.postgres.tenancy.flyway.SchemaCreator;
import com.mhdanh.postgres.tenancy.flyway.SchemaMigrationService;
import com.mhdanh.postgres.tenancy.service.TokenService;
import com.nimbusds.jose.JOSEException;

@RequestScoped
@Transactional
@Path("/{schema-name}")
public class SchemaResource {
	
	@Inject
    private SchemaCreator schema;
	
	@Inject
	private SchemaMigrationService schemaService;
	
	@Inject
	private TokenService tokenService;
	
	@PublicAPI
	@POST
    @Path("schema")
    public Response createTenantSchema(@PathParam("schema-name") String schemaName) {
			schema.createSchema(schemaName);
        	return Response.ok("Create schema successful").build();
    }
	
	@PublicAPI
	@POST
    @Path("schema/migrate")
    public Response createTablesInTenantSchema(@PathParam("schema-name") String schemaName) {
			schemaService.migrateSchema(schemaName);
        	return Response.ok("Migration schema successful").build();
    }
	
	@PublicAPI
	@POST
    @Path("token")
    public Object getToken(@PathParam("schema-name") String schemaName, @HeaderParam("USERNAME") String username, @HeaderParam("PASSWORD") String password) throws JOSEException {
		Map<String, Object> map = new HashMap<>();
		// call service to check username, password and schema belong to user or not
        if("admin".equalsIgnoreCase(username) && "123".equalsIgnoreCase(password)) {
            map.put("token", tokenService.makeToken(schemaName, username, password));
            map.put("publicKey", tokenService.getPublicKey().getEncoded());
        } else {
        	map.put("message", "Username or password invalid");
        }
		return map;	
    }
}

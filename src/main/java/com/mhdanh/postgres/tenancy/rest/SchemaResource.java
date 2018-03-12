package com.mhdanh.postgres.tenancy.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.mhdanh.postgres.tenancy.flyway.SchemaCreator;

@RequestScoped
@Transactional
@Path("/{schema-name}")
public class SchemaResource {
	
	@Inject
    private SchemaCreator schema;
	
	@POST
    @Path("schema")
    public Response createTenantSchema(@PathParam("schema-name") String schemaName) {
			schema.createSchema(schemaName);
        	return Response.ok().build();
    }
}

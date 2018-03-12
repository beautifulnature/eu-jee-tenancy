package com.mhdanh.postgres.tenancy.flyway;

import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@RequestScoped
public class SchemaCreator {
	
	@PersistenceContext
    EntityManager em;
    
    private static final String GENERIC_ROLE_NAME = "generic_role";
    private static final String PUBLIC_SCHEMA_NAME = "public";
	
	public void createSchema(String schemaName) {
		validateTenantId(schemaName);
        performSchemaCreation(schemaName);
	}

    private void validateTenantId(String tenantId) {
    	if(!(Pattern.compile("[A-Za-z0-9_]+").matcher(tenantId).matches())) {
    		throw new RuntimeException("Schema name invalid only constain character, number, underscore");
    	}
	}

	private void performSchemaCreation(String schemaName) {
        em.createNativeQuery("RESET ROLE").executeUpdate();
       
        if (tenantSchemaExists(schemaName)) {
        	return;
        }
        
        if(!checkRoleExist(schemaName)) {
            execute("CREATE ROLE " + schemaName + " WITH NOLOGIN NOINHERIT;");
        }
        
        if(!checkRoleExist(GENERIC_ROLE_NAME)) {
            execute("CREATE ROLE "+ GENERIC_ROLE_NAME + " WITH NOLOGIN NOINHERIT;");
            execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + GENERIC_ROLE_NAME + ";");
            execute("GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + GENERIC_ROLE_NAME + ";");
            execute("GRANT ALL PRIVILEGES ON SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + GENERIC_ROLE_NAME + ";");
            execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " GRANT ALL PRIVILEGES ON TABLES TO " + GENERIC_ROLE_NAME + ";");
            execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " GRANT ALL PRIVILEGES ON SEQUENCES TO " + GENERIC_ROLE_NAME + ";");
        }
        
        execute("CREATE SCHEMA AUTHORIZATION " + schemaName + ";");
        execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + schemaName + " TO " + schemaName + ";");
        execute("GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA " + schemaName + " TO " + schemaName + ";");
        execute("GRANT ALL PRIVILEGES ON SCHEMA " + schemaName + " TO " + schemaName + ";");
        execute("GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + schemaName + ";");
        execute("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + schemaName + ";");
        execute("GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + schemaName + ";");
        execute("GRANT ALL PRIVILEGES ON SCHEMA " + PUBLIC_SCHEMA_NAME + " TO " + schemaName + ";");
        execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " GRANT ALL PRIVILEGES ON TABLES TO " + schemaName + ";");
        execute("ALTER DEFAULT PRIVILEGES IN SCHEMA " + PUBLIC_SCHEMA_NAME + " GRANT ALL PRIVILEGES ON SEQUENCES TO " + schemaName + ";");
        
        String current_username = getConnectionCurrentUser();
        execute("GRANT " + GENERIC_ROLE_NAME + " TO " + current_username + ";");
        execute("GRANT " + schemaName + " TO " + current_username + ";");
        
        em.flush();
    }
    
    private void execute(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }
    
    private boolean tenantSchemaExists(String schemaName) {
    	Query checkSchemaExists = em.createNativeQuery("select exists(select schema_name from information_schema.schemata where schema_name = :schemaName)")
    			.setParameter("schemaName", schemaName);
    	return (boolean) checkSchemaExists.getSingleResult();
    }
    
    private boolean checkRoleExist(String rolename) {
        Query q = em.createNativeQuery("SELECT EXISTS(SELECT 1 FROM pg_roles WHERE rolname = ?)");
        q.setParameter(1, rolename);
        return ((boolean) q.getSingleResult());
    }
    
    private String getConnectionCurrentUser() {
        return (String) em.createNativeQuery("SELECT current_user;").getSingleResult();
    }
}

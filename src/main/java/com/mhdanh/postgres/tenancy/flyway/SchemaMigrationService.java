package com.mhdanh.postgres.tenancy.flyway;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;


@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SchemaMigrationService {
	
	@Resource(lookup = "java:jboss/datasources/postgres_tenancy")
    private DataSource dataSource;
	
	public void migrateSchema(String schemaName) {
		Flyway flyway = new Flyway();
        flyway.setLocations("db/migration");
        flyway.setDataSource(dataSource);
        flyway.setValidateOnMigrate(false);
        flyway.setSchemas(schemaName);
        flyway.setSkipDefaultCallbacks(true);
        flyway.setCallbacks(new SetRoleToSchemaFlywayCallback(schemaName));
        flyway.migrate();
	}

}

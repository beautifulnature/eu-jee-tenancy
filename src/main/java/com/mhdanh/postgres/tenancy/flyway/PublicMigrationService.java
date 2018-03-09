package com.mhdanh.postgres.tenancy.flyway;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class PublicMigrationService {
    
private static final Logger LOG = Logger.getLogger(PublicMigrationService.class.getName());
    
    @Resource(lookup = "java:jboss/datasources/postgres_tenancy")
    private DataSource dataSource;

    @PostConstruct
    public void startup() throws Exception {
    	Flyway flyway = new Flyway();
        flyway.setLocations("db/public");
        flyway.setDataSource(dataSource);
        for (MigrationInfo migrationInfo : flyway.info().pending()) {
            LOG.log(Level.INFO, "migrate task: {0} : {1} from file: {2}", new Object[]{migrationInfo.getVersion(), migrationInfo.getDescription(), migrationInfo.getScript()});
        }
        try {
            flyway.migrate();
        } catch (Exception ex) {
            throw new Exception("An Exception occurred while performing the migration on the datasource " + dataSource.toString(), ex);
        }
    }
}

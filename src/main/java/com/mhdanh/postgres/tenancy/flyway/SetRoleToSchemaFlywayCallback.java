package com.mhdanh.postgres.tenancy.flyway;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.BaseFlywayCallback;
import org.flywaydb.core.internal.util.StringUtils;

class SetRoleToSchemaFlywayCallback extends BaseFlywayCallback {
	
	public static final String RESET_ROLE = "RESET ROLE";
	public static final String SET_ROLE = "SET ROLE ?";
    public static final String SET_FLYWAY_TABLE_OWNER = "ALTER TABLE ?.schema_version OWNER TO ?;";

    private static final Logger log = Logger.getLogger(SetRoleToSchemaFlywayCallback.class.getName());

    private final String roleName;

    public SetRoleToSchemaFlywayCallback(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void afterValidate(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(RESET_ROLE);
        } catch (SQLException e) {
        	log.log(Level.INFO, "Failed to reset role in flyway migration for role {0} : {1}", new Object[]{roleName, e.getMessage()});
        }
    }

    @Override
    public void beforeValidate(Connection connection) {
        setSchemaVersionTableOwnerToTenant(connection);

        try {
            execute(connection, SET_ROLE.replace("?", roleName));
        } catch (SQLException e) {
            log.log(Level.INFO, "Failed to set the active role to {0} in flyway migration : {1}", new Object[]{roleName, e.getMessage()});
        }
    }

    @Override
    public void afterInfo(Connection connection) {
        afterValidate(connection);
    }

    @Override
    public void beforeInfo(Connection connection) {
        beforeValidate(connection);
    }

    @Override
    public void beforeMigrate(Connection connection) {
        try {
            execute(connection, SET_ROLE.replace("?", roleName));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterMigrate(Connection connection) {
        try {
            execute(connection, RESET_ROLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {
        afterMigrate(connection);
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
        beforeMigrate(connection);
    }
    
    @Override
    public void afterBaseline(Connection connection) {
    	setSchemaVersionTableOwnerToTenant(connection);
    }

    private void setSchemaVersionTableOwnerToTenant(Connection connection) {
    	try {
    		if(checkFlywayTableExists(connection)) {
    			execute(connection, SET_FLYWAY_TABLE_OWNER.replace("?", roleName));
    		}
    	} catch (SQLException e) {
    		log.log(Level.INFO, "The Flyway schema version table owner could not be set for role {0} : {1}", new Object[]{roleName, e.getMessage()});
    	}
    }

    private boolean checkFlywayTableExists(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT to_regclass('?.schema_version')".replace("?", roleName));
            if(rs.next()) {
                return StringUtils.hasText(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(SetRoleToSchemaFlywayCallback.class.getName()).
                    log(Level.WARNING, "A Problem occured while checking if the flyway schema_version table exists in the schema {0} Message: {1}", new Object[]{roleName, ex.getMessage()});
        }
        return false;
    }

    private void execute(Connection connection, String sql) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(sql);
    }
}

package com.mhdanh.postgres.tenancy.producer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.hibernate.jdbc.Work;

import com.mhdanh.postgres.tenancy.annotation.CurrentSchema;

@RequestScoped
@Transactional
public class SchemaEntityManagerFactory {
	
	private static final String SCHEMA_NAME = "SCHEMA_NAME";
	private static final String RESET_ROLE = "RESET ROLE";
	
	@PersistenceContext
	EntityManager em;

	@Inject
	HttpServletRequest request;

	@Produces
	@CurrentSchema
	@RequestScoped
	public EntityManager create() {
		return hasTenant() ? appliedTenant() : appliedPublic();
	}

	private EntityManager appliedTenant() {
		org.hibernate.Session hibernateSession = em.unwrap(org.hibernate.Session.class);
		hibernateSession.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				try {
					connection.createStatement().execute(RESET_ROLE);
					connection.createStatement().execute("SET ROLE " + getSchemaName());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return em;
	}

	private EntityManager appliedPublic() {
		org.hibernate.Session hibernateSession = em.unwrap(org.hibernate.Session.class);
		hibernateSession.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				try {
					connection.createStatement().execute(RESET_ROLE);
					connection.createStatement().execute("SET ROLE public_role");
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return em;
	}

	private boolean hasTenant() {
		return request.getHeader(SCHEMA_NAME) != null && !request.getHeader(SCHEMA_NAME).isEmpty();
	}

	private String getSchemaName() {
		return request.getHeader(SCHEMA_NAME);
	}
}

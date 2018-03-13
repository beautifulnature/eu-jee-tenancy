package com.mhdanh.postgres.tenancy.rest;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.mhdanh.postgres.tenancy.model.School;
import com.mhdanh.postgres.tenancy.service.SchoolService;

@Path("{schema-name}/schools")
public class SchoolResource {
	
	@Inject
	private SchoolService schoolService;
	
	@POST
	public String initData() {
		schoolService.initData();
		return "Init data mock succeful";
	}
	
	@GET
	public List<School> getAll() {
		return schoolService.getAll();
	}
	
}

package com.mhdanh.postgres.tenancy.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mhdanh.postgres.tenancy.annotation.CurrentSchema;
import com.mhdanh.postgres.tenancy.entity.CityEntity;
import com.mhdanh.postgres.tenancy.entity.SchoolEntity;
import com.mhdanh.postgres.tenancy.model.School;

@Stateless
public class SchoolService {
	
	@PersistenceContext
	private EntityManager publicSchema;
	
	@Inject
	@CurrentSchema
	EntityManager currentSchema;
	
	public List<School> getAll() {
		List<SchoolEntity> schools = currentSchema.createQuery("SELECT s FROM SchoolEntity s", SchoolEntity.class).getResultList();
		List<CityEntity> cities = publicSchema.createQuery("SELECT city FROM CityEntity city", CityEntity.class).getResultList();
		return schools.stream().map(e -> {
			School s = new School();
			s.setId(e.getId());
			s.setName(e.getName());
			CityEntity city = cities.stream().filter(ce -> ce.getCode().equalsIgnoreCase(e.getCityCode())).findFirst().orElse(null);
			s.setCity(city);
			return s;
		}).collect(Collectors.toList());
 	}

	public void initData() {
		CityEntity city1 = new CityEntity();
		city1.setCode("HCM");
		
		publicSchema.persist(city1);
		
		SchoolEntity schoolEntity = new SchoolEntity();
		schoolEntity.setCityCode("HCM");
		schoolEntity.setName("Nguyen Huu Canh");
		currentSchema.persist(schoolEntity);
	}

}

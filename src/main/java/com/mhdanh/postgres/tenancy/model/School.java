package com.mhdanh.postgres.tenancy.model;

import com.mhdanh.postgres.tenancy.entity.CityEntity;

public class School {
	
	private long id;

	private String name;

	private CityEntity city;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CityEntity getCity() {
		return city;
	}

	public void setCity(CityEntity city) {
		this.city = city;
	}

}

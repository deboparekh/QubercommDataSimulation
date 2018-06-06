package com.com.semaifour.facesix.data.datasource;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasourceRepository extends MongoRepository<Datasource, String> {

	public List<Datasource> findByName(String name);

	public List<Datasource> findByUid(String uid);

}
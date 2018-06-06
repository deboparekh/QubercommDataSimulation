package com.com.semaifour.facesix.data.entity.copy;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EntityRepository extends MongoRepository<Entity, String> {

	public List<Entity> findByName(String name);

	public List<Entity> findByUid(String uid);

}
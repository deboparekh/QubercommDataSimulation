package com.com.semaifour.facesix.data.device;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientDeviceRepository extends MongoRepository<ClientDevice, String> {
	public List<ClientDevice> findByName(String name);

	public List<ClientDevice> findByUid(String uid);
}
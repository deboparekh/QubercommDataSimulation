package com.com.semaifour.facesix.data.device;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<Device, String> {

	public List<Device> findByName(String name);

	public List<Device> findByUid(String uid);

	public List<Device> findByStatus(String status);

}
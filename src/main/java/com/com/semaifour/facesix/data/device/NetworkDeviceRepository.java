package com.com.semaifour.facesix.data.device;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NetworkDeviceRepository extends MongoRepository<NetworkDevice, String> {
	public List<NetworkDevice> findBySid(String sid);

	public List<NetworkDevice> findBySpid(String spid);

	public List<NetworkDevice> findByName(String name);

	public List<NetworkDevice> findByUid(String uid);

	public List<NetworkDevice> findBySvid(String svid);

	public List<NetworkDevice> findBySwid(String swid);

	public List<NetworkDevice> findByUuid(String uuid);
}
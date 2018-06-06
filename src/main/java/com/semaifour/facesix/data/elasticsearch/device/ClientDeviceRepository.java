package com.semaifour.facesix.data.elasticsearch.device;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientDeviceRepository extends MongoRepository<ClientDevice, String> {
	public List<ClientDevice> findByName(String name);

	public List<ClientDevice> findByUid(String uid);

	public List<ClientDevice> findByPid(String uid);

	public List<ClientDevice> findBySid(String sid);

	public List<ClientDevice> findBySpid(String spid);

	public List<ClientDevice> findByCid(String cid);

	public ClientDevice findByMac(String peer_mac);

	public ClientDevice findByPeermac(String peer_mac);
}
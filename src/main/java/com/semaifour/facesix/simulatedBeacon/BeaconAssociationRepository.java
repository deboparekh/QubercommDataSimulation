package com.semaifour.facesix.simulatedBeacon;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BeaconAssociationRepository extends MongoRepository<BeaconAssociation, String> {

	@Query("{cid:?0,macaddr:?1}")
	BeaconAssociation findByCidAndMacaddr(String cid, String macaddr);

	List<BeaconAssociation> findByCid(String spid);
	List<BeaconAssociation> findBySid(String spid);
	List<BeaconAssociation> findBySpid(String spid);
	List<BeaconAssociation> findByUid(String uid);

}

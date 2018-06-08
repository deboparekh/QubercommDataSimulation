package com.semaifour.facesix.simulatedBeacon;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BeaconAssociationRepository extends MongoRepository<BeaconAssociation, String> {

}

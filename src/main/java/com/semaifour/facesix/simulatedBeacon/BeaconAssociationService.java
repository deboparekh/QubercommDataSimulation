package com.semaifour.facesix.simulatedBeacon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BeaconAssociationService {
	private static String classname = BeaconAssociationService.class.getName();

	Logger LOG = LoggerFactory.getLogger(classname);

	@Autowired(required = false)
	private BeaconAssociationRepository repository;
	
	
}

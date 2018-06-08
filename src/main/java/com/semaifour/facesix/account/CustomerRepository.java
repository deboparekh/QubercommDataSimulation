package com.semaifour.facesix.account;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CustomerRepository extends MongoRepository<Customer, String> {

	public List<Customer> findByCustomerName(String customerName);

	public List<Customer> findOneById(String id);

	public Customer findById(String id);

	public Customer findByEmail(String emailId);

	@Query("{venueType:{$in:?0},status:?1}")
	public Iterable<Customer> findByVenueTypeAndStatus(List<String> venueType, String status);

	public Customer findByPreferedUrlName(String preferredUrl);

	@Query("{solution:{$in:?0},status:?1}")
	public Iterable<Customer> findBySolutionAndStatus(List<String> solution, String status);

	public List<Customer> findBySimulation(String simulation);

	@Query("{simulation:?0,solution:{$in:?1},status:?2}")
	public List<Customer> findBySimulationSolutionAndState(String simulation, List<String> solution, String state);

}

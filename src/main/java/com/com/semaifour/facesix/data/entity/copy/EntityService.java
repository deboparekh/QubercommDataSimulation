
package com.com.semaifour.facesix.data.entity.copy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

//@Service
public class EntityService {

	static Logger LOG = LoggerFactory.getLogger(EntityService.class.getName());

	@Autowired
	private EntityRepository repository;

	public EntityService() {
	}

	public Page<Entity> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Entity> findByName(String name) {
		return repository.findByName(name);
	}

	public List<Entity> findByUid(String uid) {
		return repository.findByUid(uid);
	}

	public Entity findOneByName(String name) {
		List<Entity> list = findByName(name);
		if (list != null & list.size() > 0)
			return list.get(0);
		return null;
	}

	public Entity findOneByUid(String uid) {
		List<Entity> list = findByUid(uid);
		if (list != null & list.size() > 0)
			return list.get(0);
		return null;
	}

	public Entity findById(String id) {
		return repository.findOne(id);
	}

	public boolean exists(String id) {
		return repository.exists(id);
	}

	public boolean exists(String uid, String name) {
		if (findOneByUid(uid) != null)
			return true;
		if (findOneByName(name) != null)
			return true;
		return false;
	}

	public void deleteAll() {
		repository.deleteAll();
	}

	public void delete(String id) {
		repository.delete(id);
	}

	public void delete(Entity entity) {
		repository.delete(entity);
	}

	public long count() {
		return repository.count();
	}

	/**
	 * Save entity and notify
	 * 
	 * @param entity
	 * @return
	 */
	public Entity save(Entity entity) {
		return save(entity, true);
	}

	/**
	 * 
	 * Save entity and notify=true or false
	 * 
	 * @param entity
	 * @param notify
	 * @return
	 */
	public Entity save(Entity entity, boolean notify) {
		entity = repository.save(entity);
		LOG.info("Device saved successfully :" + entity.getId());
		return entity;
	}

	public Iterable<Entity> findAll() {
		return repository.findAll();
	}

	public Iterable<Entity> findByQuery(String query, String sort, int page, int size) {

		// for(Sort s : sorts) {
		// sq.addSort(s);
		// }

		// return repository.search(sq);
		return null;
	}
}


package com.com.semaifour.facesix.data.device;

import java.text.MessageFormat;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.semaifour.facesix.mqtt.DeviceEventPublisher;

//@Service
public class DeviceService {

	static Logger LOG = LoggerFactory.getLogger(DeviceService.class.getName());

	@Autowired
	private DeviceRepository repository;

	@Autowired
	private DeviceEventPublisher mqttPublisher;

	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"newversion\":\"{3}\", \"value\":{4} ";

	public DeviceService() {
	}

	public Page<Device> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<Device> findByName(String name) {
		return repository.findByName(QueryParser.escape(name));
	}

	public List<Device> findByUid(String uid) {
		return repository.findByUid(QueryParser.escape(uid));
	}

	public Device findOneByName(String name) {
		List<Device> list = findByName(name);
		if (list != null & list.size() > 0)
			return list.get(0);
		return null;
	}

	public Device findOneByUid(String uid) {
		List<Device> list = findByUid(uid);
		if (list != null & list.size() > 0)
			return list.get(0);
		return null;
	}

	public Device findById(String id) {
		return repository.findOne(QueryParser.escape(id));
	}

	public boolean exists(String id) {
		return repository.exists(QueryParser.escape(id));
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
		repository.delete(QueryParser.escape(id));
	}

	public void delete(Device device) {
		repository.delete(device);
	}

	public long count() {
		return repository.count();
	}

	/**
	 * Save device and notify
	 * 
	 * @param device
	 * @return
	 */
	public Device save(Device device) {
		return save(device, true);
	}

	/**
	 * 
	 * Save device and notify=true or false
	 * 
	 * @param device
	 * @param notify
	 * @return
	 */
	public Device save(Device device, boolean notify) {
		device = repository.save(device);
		LOG.info("Device saved successfully :" + device.getId());
		if (notify) {
			try {
				String message = MessageFormat.format(mqttMsgTemplate, new Object[] { "UPDATE", device.getUid(),
						device.getModifiedBy(), device.getVersion(), device.getConf() });
				mqttPublisher.publish("{" + message + "}", device.getUid());
			} catch (Exception e) {
				LOG.warn("Failed to notify update", e);

			}
		}
		return device;
	}

	public Device reset(Device device, boolean notify) {

		if (notify) {
			try {
				String message = MessageFormat.format(mqttMsgTemplate, new Object[] { "RESET", device.getUid(),
						device.getModifiedBy(), device.getVersion(), device.getConf() });
				mqttPublisher.publish("{" + message + "}", device.getUid());
				LOG.info("Device Config MQTT Data" + message);
			} catch (Exception e) {
				LOG.warn("Failed to notify update", e);

			}
			LOG.info("Device reset successfully :" + device.getId());
		}
		return device;
	}

	public Iterable<Device> findAll() {
		return repository.findAll();
	}

	public Iterable<Device> findByQuery(String query, String sort, int page, int size) {

		// for(Sort s : sorts) {
		// sq.addSort(s);
		// }
		// return repository.search(sq);
		return null;
	}
}

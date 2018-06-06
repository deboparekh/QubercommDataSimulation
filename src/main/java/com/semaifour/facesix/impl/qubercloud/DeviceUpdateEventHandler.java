package com.semaifour.facesix.impl.qubercloud;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.binary.BinarySetting;
import com.semaifour.facesix.binary.BinarySettingService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.device.ClientDevice;
import com.semaifour.facesix.data.elasticsearch.device.ClientDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDevice;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDeviceService;
import com.semaifour.facesix.data.qubercast.QuberCast;
import com.semaifour.facesix.data.qubercast.QuberCastService;
import com.semaifour.facesix.device.data.DeviceItem;
import com.semaifour.facesix.device.data.DeviceItemService;
import com.semaifour.facesix.mqtt.DefaultMqttMessageReceiver;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.mqtt.Payload;
import com.semaifour.facesix.rest.DeviceRestController;
import com.semaifour.facesix.rest.NetworkConfRestController;
import com.semaifour.facesix.rest.NetworkDeviceRestController;
import com.semaifour.facesix.spring.SpringComponentUtils;
import net.sf.json.JSONObject;

public class DeviceUpdateEventHandler extends DefaultMqttMessageReceiver {

	private static Logger LOG = LoggerFactory.getLogger(DeviceUpdateEventHandler.class.getName());

	private static final String FORMAT_STRING = "HH:mm:ss.SSSZ";

	DeviceService _deviceService;

	NetworkDeviceService _networkDeviceService;

	ClientDeviceService _clientDeviceService;

	@Autowired
	QuberCastService _qubercastService;

	@Autowired
	NetworkConfRestController netRestController;

	@Autowired
	DeviceRestController _deviceRestController;

	@Autowired
	NetworkDeviceRestController networkDeviceRestController;

	@Autowired
	private DeviceEventPublisher _mqttPublisher;

	private DeviceItemService _deviceItemService;

	BinarySettingService _binarySettingService;

	public DeviceUpdateEventHandler() {
	}

	@Override
	public boolean messageArrived(String topic, MqttMessage message) {
		return messageArrived(topic, message.toString());
	}

	@Override
	public boolean messageArrived(String topic, String message) {
		if (LOG.isDebugEnabled())
			LOG.debug("handling msqtt message at " + topic + " : " + message);
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(message.toString(),
					new TypeReference<HashMap<String, Object>>() {
					});
			String op = (String) map.get("opcode");

			LOG.info("MQTT OPCODE " + op);
			LOG.info("MQTT MESSAGE " + message);

			if (StringUtils.equalsIgnoreCase(op, "device_update")) {
				return update(map);
			} else if (StringUtils.equalsIgnoreCase(op, "device_create")) {
				map.put("status", Device.STATUS.AUTOCONFIGURED.name());
				return create(map, true);
			} else if (StringUtils.equalsIgnoreCase(op, "device_register")) {
				map.put("status", Device.STATUS.REGISTERED.name());
				return create(map, false);
			} else if (StringUtils.equalsIgnoreCase(op, "device_add_item")) {
				String type = (String) map.get("typefs");
				return createDeviceItem(map, type != null ? type : "ITEMLIST");
			} else if (StringUtils.equalsIgnoreCase(op, "device_remove_item")) {
				String type = (String) map.get("typefs");
				return removeDeviceItem(map, type != null ? type : "ITEMLIST");
			} else if (StringUtils.equalsIgnoreCase(op, "device_unallow_item")) {
				return createDeviceItem(map, "ALLOWLIST");
			} else if (StringUtils.equalsIgnoreCase(op, "device_block_item")) {
				return createDeviceItem(map, "BLOCKLIST");
			} else if (StringUtils.equalsIgnoreCase(op, "device_unblock_item")) {
				return createDeviceItem(map, "BLOCKLIST");
			} else if (StringUtils.equals(op, "device_heartbeat")) {
				getDeviceService().updateDeviceHealth(map);
				return true;
			} else if (StringUtils.equals(op, "ping_request")) {
				return ping(map);
			} else if (StringUtils.equalsIgnoreCase(op, "peer_update")) {
				return peer_update(map);
			} else if (StringUtils.equalsIgnoreCase(op, "QCAST_GET_CFG")) {
				return qcast_update(map);
			} else if (StringUtils.equalsIgnoreCase(op, "UPGRADE")) {
				return binarySetting_upgrade(map);
			} else if (StringUtils.equalsIgnoreCase(op, "bandbalance")
					|| StringUtils.equalsIgnoreCase(op, "loadbalance")) {
				return getNetworkDeviceRestController().netGear(map);
			} else {
				LOG.warn("Unsupported opcode :" + op);
				return false;
			}
		} catch (Exception e) {
			LOG.error("Failed to process messageArrvied at " + topic + " : " + message, e);
			return false;
		}
	}

	private boolean binarySetting_upgrade(Map<String, Object> map) throws Exception {
		try {

			String uid = (String) map.get("uid");
			String reson = (String) map.get("REASON");

			if (uid != null) {
				String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
				NetworkDevice nd = getNetworkDeviceService().findOneByUuid(uuid);
				if (nd != null) {
					nd.setModifiedOn(new Date(System.currentTimeMillis()));
					nd.setBinaryreason(reson);
					getNetworkDeviceService().save(nd, false);
				}
			}
			return true;
		} catch (Exception e) {
			LOG.warn("Error " + e.getStackTrace());
		}
		return false;
	}

	private boolean createDeviceItem(Map<String, Object> map, String type) {
		try {
			String uid = (String) map.get("uid");
			String mac = (String) map.get("mac");
			String by = (String) map.get("by");
			DeviceItem item = new DeviceItem(uid, mac, type);
			item.setModifiedBy(by);
			item.setCreatedBy(by);
			Properties p = new Properties();
			p.putAll(map);
			item.setSettings(p);
			deviceItemService().save(item, Boolean.valueOf((String) map.get("notify")));
			// LOG.info("created DeviceItem[ {}, {}, {} ]", uid, mac, type);
			return true;
		} catch (Exception e) {
			LOG.warn("exception createDeviceItem() [{}]", map.toString(), e);
			return false;
		}
	}

	private boolean removeDeviceItem(Map<String, Object> map, String type) {
		try {
			String uid = (String) map.get("uid");
			String mac = (String) map.get("mac");
			deviceItemService().delete(mac);
			// LOG.info("removed DeviceItem[ {}, {}, {} ]", uid, mac, type);
			return true;
		} catch (Exception e) {
			LOG.warn("exception createDeviceItem() [{}]", map.toString(), e);
			return false;
		}
	}

	/**
	 * 
	 * Send ping response
	 * 
	 * @param map
	 * @return
	 */
	private boolean ping(Map<String, Object> map) {
		String uid = (String) map.get("uid");
		try {
			String msg = (String) map.get("message");
			Payload payload = new Payload("ping_response", uid, uid, msg);
			getDeviceEventPublisher().publish(payload.toJSONString(), uid);
		} catch (Exception e) {
			LOG.warn("ping_response for ping_reqquest from [{}] failed", uid, e);
		}
		return true;
	}

	public boolean create(Map<String, Object> map, boolean notify) throws Exception {
		String uid = (String) map.get("uid");
		String name = (String) map.get("name");
		String template = (String) map.get("template");
		String by = (String) map.get("by");
		String fstype = (String) map.get("type");
		String alias = (String) map.get("alias");
		String cid = (String) map.get("cid");
		String sid = (String) map.get("sid");
		String spid = (String) map.get("spid");

		String steering = "";
		if (map.get("steering") != null) {
			steering = (String) map.get("steering");
		}

		LOG.info("creating/registering device :" + uid);

		if (name == null)
			name = uid;
		Device device = getDeviceService().findOneByUid(uid);
		Date dt = new Date();// System.currentTimeMillis());

		if (device == null) {
			device = new Device();
			device.setCreatedBy(by);
			device.setCreatedOn(dt);
			device.setUid(uid);
			device.setName(name);
			device.setIp("0.0.0.0");
		}

		if (cid != null && !cid.isEmpty()) {
			device.setCid(cid);
		}
		if (sid != null && !sid.isEmpty()) {
			device.setSid(sid);
		}
		if (spid != null && !spid.isEmpty()) {
			device.setSpid(spid);
		}
		if (fstype != null && !fstype.isEmpty()) {
			device.setFstype(fstype);
		}
		if (alias != null && !alias.isEmpty()) {
			device.setAlias(alias);
		}
		if (steering != null && !steering.isEmpty()) {
			device.setSteering(steering);
		}

		device.setModifiedBy(by);
		device.setModifiedOn(dt);
		device.setStatus((String) map.get("status"));
		device.setState("inactive");

		String tconf = null;

		if (by.equals("Web")) {
			tconf = template;
		} else {
			tconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.device.template." + template);
		}

		if (tconf == null) {
			tconf = SpringComponentUtils.getApplicationMessages().getMessage("facesix.device.template.default");
		}
		device.setTemplate(template);
		device.setConf(tconf);
		// getDeviceService().save(device, notify);
		getDeviceService().saveAndSendMqtt(device, notify);
		return true;
	}

	public boolean update(Map<String, Object> map) throws Exception {

		String uid = (String) map.get("uid");
		String en = (String) map.get("_entity");
		Object vapcnt = map.get("_vapcnt");
		String ip = (String) map.get("ip");
		String role = (String) map.get("device_role");

		LOG.info("updating device>>>> :" + uid);
		LOG.info("updating device IP>>>>: " + ip);
		LOG.info("updating device Role>>>>: " + role);

		Device dv = getDeviceService().findOneByUid(uid);

		if (dv != null) {
			/*
			 * String conf = dv.getConf(); ObjectMapper mapper = new
			 * ObjectMapper(); Map<String,List<Map<String, String>>> config =
			 * mapper.readValue(conf, new
			 * TypeReference<Map<String,List<Map<String, String>>>>(){});
			 * 
			 * conf = mapper.writeValueAsString(config); dv.setConf(conf);
			 */
			// dv.setState("idle");

			/// LOG.info("entity " + en);

			if (en != null) {
				if (en.equals("radio2g")) {
					LOG.info("2G VAP Count" + vapcnt);
					dv.setVap2gcount(String.valueOf(vapcnt));
				} else if (en.equals("radio5g")) {
					LOG.info("5G VAP Count" + vapcnt);
					dv.setVap5gcount(String.valueOf(vapcnt));
				}
			}

			if (ip != null && !ip.isEmpty()) {
				dv.setIp(ip);
			}

			if (role != null) {
				dv.setRole(role);
			} else {
				dv.setRole("ap");
			}
			dv.setState("active");
			dv.setModifiedBy((String) map.get("by"));
			dv.setModifiedOn(new Date(System.currentTimeMillis()));
			// dv.setVer(dv.getVer() + 1);

			getDeviceService().save(dv, false);

			netRestController = new NetworkConfRestController();
			netRestController.updatestate(dv, getNetworkDeviceService());
		}

		/*
		 * netRestController = new NetworkConfRestController ();
		 * 
		 * 
		 * // Check any blocked-clients are available for this Node, if any
		 * update it String pid = uid.replaceAll("[^a-zA-Z0-9]", "");
		 * Iterable<ClientDevice> cdev =
		 * getClientDeviceService().findByPid(pid); if (cdev != null) { String[]
		 * stringArray = new String[]{"none"}; for (ClientDevice nd : cdev) { if
		 * (nd.pid.equals(pid)) { // Issue block list to node
		 * getDeviceRestController().rpc(uid,nd.ap,nd.mac, "BLOCK",
		 * stringArray); }
		 * 
		 * } }
		 */

		return true;
	}

	public boolean peer_update(Map<String, Object> map) throws Exception {

		String uid = (String) map.get("uid");
		int peer_count = (int) map.get("peer_count");

		String state = "active";

		// LOG.info("peer_count :" + peer_count);

		Device dv = getDeviceService().findOneByUid(uid);

		if (dv != null) {

			/*
			 * if (peer_count != 0) { state = "active"; } else { state = "idle";
			 * }
			 */

			LOG.info("PEER UID" + uid);
			LOG.info("peer_count" + peer_count);

			try {

				dv.setState(state);
				dv.setModifiedBy((String) map.get("by"));

				dv.setModifiedOn(new Date(System.currentTimeMillis()));
				getDeviceService().save(dv, false);
				netRestController = new NetworkConfRestController();
				netRestController.updatestate(dv, getNetworkDeviceService());

			} catch (NumberFormatException nfe) {
				LOG.info("Error in Format" + System.currentTimeMillis());
			}

			return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean qcast_update(Map<String, Object> map) throws Exception {
		String qcastmqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"by\":\"{2}\", \"newversion\":\"{3}\", \"value\":{4} ";

		JSONObject jsonObject = new JSONObject();
		String uid = (String) map.get("uid");
		QuberCast quber = getQuberCastService().findByReffId("a5a5");
		if (quber != null) {
			jsonObject.put("mediaPath", quber.getMediaPath());
			jsonObject.put("multicastPort", quber.getMulticastPort());
			jsonObject.put("mulicastAddress", quber.getMulicastAddress());
			jsonObject.put("totalFiles", quber.getLogFile());
			jsonObject.put("payLoad", quber.getLogLevel());
		}

		Device device = getDeviceService().findOneByUid(uid);
		String header = "AP_QCAST_START";
		if (device != null) {
			String msg = MessageFormat.format(qcastmqttMsgTemplate,
					new Object[] { header, device.getUid(), "qubercloud", "0xFE", jsonObject.toString() });
			getDeviceEventPublisher().publish("{" + msg + "}", device.getUid());
		}

		return true;
	}

	private DeviceService getDeviceService() {

		try {
			if (_deviceService == null) {
				_deviceService = Application.context.getBean(DeviceService.class);
			}
		} catch (Exception e) {

		}
		return _deviceService;
	}

	private ClientDeviceService getClientDeviceService() {
		try {
			if (_clientDeviceService == null) {
				_clientDeviceService = Application.context.getBean(ClientDeviceService.class);
			}
		} catch (Exception e) {

		}
		return _clientDeviceService;
	}

	private NetworkDeviceService getNetworkDeviceService() {

		try {
			if (_networkDeviceService == null) {
				_networkDeviceService = Application.context.getBean(NetworkDeviceService.class);
			}
		} catch (Exception e) {

		}
		return _networkDeviceService;
	}

	private DeviceRestController getDeviceRestController() {

		try {
			if (_deviceRestController == null) {
				_deviceRestController = Application.context.getBean(DeviceRestController.class);
			}
		} catch (Exception e) {

		}
		return _deviceRestController;
	}

	private QuberCastService getQuberCastService() {
		try {
			if (_qubercastService == null) {
				_qubercastService = Application.context.getBean(QuberCastService.class);
			}
		} catch (Exception e) {

		}
		return _qubercastService;
	}

	private DeviceEventPublisher getDeviceEventPublisher() {

		try {
			if (_mqttPublisher == null) {
				_mqttPublisher = Application.context.getBean(DeviceEventPublisher.class);
			}
		} catch (Exception e) {

		}
		return _mqttPublisher;
	}

	private DeviceItemService deviceItemService() {

		try {
			if (_deviceItemService == null) {
				_deviceItemService = Application.context.getBean(DeviceItemService.class);
			}
		} catch (Exception e) {

		}

		return _deviceItemService;
	}

	private BinarySettingService getBinarySettingService() {

		try {
			if (_binarySettingService == null) {
				_binarySettingService = Application.context.getBean(BinarySettingService.class);
			}
		} catch (Exception e) {

		}
		return _binarySettingService;
	}

	private NetworkDeviceRestController getNetworkDeviceRestController() {

		try {
			if (networkDeviceRestController == null) {
				networkDeviceRestController = Application.context.getBean(NetworkDeviceRestController.class);
			}
		} catch (Exception e) {
			LOG.info("NetworkDeviceRestController  Bean Initialization failed...");
		}
		return networkDeviceRestController;
	}

	@Override
	public String getName() {
		return "DefaultMqttMessageReceiver";
	}

}

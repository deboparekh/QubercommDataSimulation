package com.semaifour.facesix.rest;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.device.ClientDevice;
import com.semaifour.facesix.data.elasticsearch.device.ClientDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;
import net.sf.json.JSONObject;

/**
 * 
 * Rest Device Controller handles all rest calls for network configuration
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/site/portion/clientdevice")
public class ClientConfRestController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(ClientConfRestController.class.getName());

	ClientDeviceService _clientDeviceService;

	DeviceService _deviceService;

	@Autowired
	private DeviceEventPublisher mqttPublisher;

	@RequestMapping(value = "/byuid", method = RequestMethod.GET)
	public List<ClientDevice> getuid(@RequestParam("uid") String uid) {
		String uid_s = uid.replaceAll("[^a-zA-Z0-9]", "");
		List<ClientDevice> list = getClientDeviceService().findByUid(uid_s);
		return list;
	}

	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public Iterable<ClientDevice> query(@RequestParam("uid") String uid,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size) {
		String uid_s = uid.replaceAll("[^a-zA-Z0-9]", "");
		Iterable<ClientDevice> list = getClientDeviceService().findByQuery(uid_s, type, status, sort, page, size);
		return list;
	}

	@RequestMapping(value = "/getid", method = RequestMethod.GET)
	public ClientDevice getid(@RequestParam("id") String id) {
		ClientDevice nd = getClientDeviceService().findById(id);
		return nd;
	}

	/**
	 * Delete ClientDevice by its 'id'.
	 * 
	 * @param id
	 * @return
	 */

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public void delete(@RequestParam("uid") String uid) throws Exception {

		String uid_s = uid.replaceAll("[^a-zA-Z0-9]", "");
		ClientDevice device = getClientDeviceService().findOneByUid(uid_s);
		if (device != null) {
			getClientDeviceService().delete(device);
		}

	}

	@RequestMapping(value = "/delall", method = RequestMethod.GET)
	public void delall() {
		getClientDeviceService().deleteAll();
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ClientDevice save(@RequestBody String newfso, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		ClientDevice qubeClient = null;
		try {

			JSONObject json = JSONObject.fromObject(newfso);
			String rssi = (String) json.get("rssi");
			String radio = (String) json.get("radio");
			String mac = (String) json.get("uid");
			String status = "blocked";
			String type = (String) json.get("client");
			String pid = (String) json.get("pid");
			String ssid = (String) json.get("ssid");
			String ap = (String) json.get("ap");
			String rx = (String) json.get("rx");
			String tx = (String) json.get("tx");
			String devname = (String) json.get("devname");

			String sid = "";
			String spid = "";
			String cid = "";

			Device device = null;
			device = getDeviceService().findOneByUid(pid); // AP ID
			if (device != null) {
				if (device.getCid() != null) {
					cid = device.getCid();
				}
				if (device.getSid() != null) {
					sid = device.getSid();
				}
				if (device.getSpid() != null) {
					spid = device.getSpid();
				}
			}
			String ACLMQTTMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"ssid\":\"{2}\", \"peer_mac\":\"{3}\" ";
			String peer_mac = mac.replaceAll("[^a-zA-Z0-9]", "");
			boolean bIsUpdate = true;

			qubeClient = getClientDeviceService().findByPeermac(peer_mac);

			if (qubeClient == null) {
				qubeClient = new ClientDevice();
				bIsUpdate = false;
			}
			qubeClient.setCid(cid);
			qubeClient.setSid(sid);
			qubeClient.setSpid(spid);
			qubeClient.setUid(pid);// AP ID
			qubeClient.setMac(mac);
			qubeClient.setPeermac(peer_mac);
			qubeClient.setPid("uid"); // policy
			qubeClient.setRssi(rssi);
			qubeClient.setRadio(radio);
			qubeClient.setStatus(status);
			qubeClient.setTypefs(type);
			qubeClient.setAP(ap);
			qubeClient.setRx(rx);
			qubeClient.setTx(tx);
			qubeClient.setDevname(devname);
			qubeClient.setSsid(ssid);
			qubeClient.setCreatedOn(new Date());
			qubeClient.setModifiedOn(new Date());
			if (bIsUpdate == true) {
				getClientDeviceService().save(qubeClient);
			} else {
				qubeClient.setCreatedOn(new Date());
				qubeClient.setCreatedBy(SessionUtil.currentUser(request.getSession()));
				qubeClient.setModifiedOn(new Date());
				qubeClient.setModifiedBy(qubeClient.getCreatedBy());
				qubeClient = getClientDeviceService().save(qubeClient);
			}

			qubeClient = getClientDeviceService().findByPeermac(peer_mac);
			if (qubeClient != null) {
				String message = MessageFormat.format(ACLMQTTMsgTemplate,
						new Object[] { "BLOCK", pid.toLowerCase(), ssid, qubeClient.getMac() });
				mqttPublisher.publish("{" + message + "}", pid.toLowerCase());
				LOG.info("ACL MQTT MESSAGE" + message);
			}

		} catch (Exception e) {
			LOG.info("While BLK Client Save Error ," + e);
		}

		return qubeClient;
	}

	private ClientDeviceService getClientDeviceService() {
		if (_clientDeviceService == null) {
			_clientDeviceService = Application.context.getBean(ClientDeviceService.class);
		}
		return _clientDeviceService;
	}

	private DeviceService getDeviceService() {
		if (_deviceService == null) {
			_deviceService = Application.context.getBean(DeviceService.class);
		}
		return _deviceService;
	}

}

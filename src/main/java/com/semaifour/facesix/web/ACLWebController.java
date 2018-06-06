package com.semaifour.facesix.web;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.itextpdf.text.Font;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.device.ClientDevice;
import com.semaifour.facesix.data.elasticsearch.device.ClientDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDeviceService;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.spring.CCC;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;

@Controller
@RequestMapping("/web/acl")
public class ACLWebController extends WebController {

	static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

	static Logger LOG = LoggerFactory.getLogger(ACLWebController.class.getName());

	@Autowired
	CCC _CCCC;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	NetworkDeviceService networkDeviceService;

	@Autowired
	SiteService siteService;

	@Autowired
	PortionService portionService;

	@Autowired
	FSqlRestController fsqlRestController;

	@Autowired
	ClientDeviceService clientDeviceService;

	@Autowired
	DeviceService devService;

	@Autowired
	private DeviceEventPublisher mqttPublisher;

	@Autowired
	DeviceService deviceManager;

	@Autowired
	DeviceEventPublisher deviceEventMqttPub;

	@Autowired
	CustomerUtils CustomerUtils;

	String mqttMsgTemplate = " \"opcode\":\"{0}\", \"uid\":\"{1}\", \"ssid\":\"{2}\", \"peer_mac\":\"{3}\" ";

	ClientDeviceService _clientDeviceService;

	static private String spid_profile = "";
	static private String sid_profile = "";

	@RequestMapping(value = "profile", method = RequestMethod.POST)
	public String profile(Map<String, Object> model, @ModelAttribute ClientDevice client,
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid, HttpServletRequest request,
			HttpServletResponse response) {

		String page = null;
		try {

			boolean bIsUpdate = true;
			String peer_mac = client.conn;
			ClientDevice qubeClient = null;
			String universalId = null;
			String MQTTMessage = null;
			boolean policyFlag = true;

			if (peer_mac != null) {
				peer_mac = peer_mac.replaceAll("[^a-zA-Z0-9]", "");
			}

			if (SessionUtil.isAuthorized(request.getSession())) {
				Device device = null;
				if (uid != null) {
					device = getDeviceService().findOneByUid(uid);
				}

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

				qubeClient = getClientDeviceService().findByPeermac(peer_mac);

				if (qubeClient == null) {
					qubeClient = new ClientDevice();
					bIsUpdate = false;
				}

				LOG.info("ACL " + peer_mac);
				LOG.info("ACL " + client.devname);
				LOG.info("ACL " + client.acl);
				LOG.info("ACL " + client.pid);
				LOG.info("ACL " + client.conn);
				LOG.info("ACL SID " + sid);
				LOG.info("ACL UID " + uid);

				if (client.pid.equals("uid")) {
					qubeClient.setUid(uid);
				} else {
					qubeClient.setUid("ff:ff:ff:ff:ff:ff");
				}
				qubeClient.setSsid(client.ssid);
				qubeClient.setAcl(client.acl);
				qubeClient.setMac(client.conn);
				qubeClient.setPeermac(peer_mac);
				qubeClient.setPid(client.pid);
				qubeClient.setCid(cid);
				qubeClient.setSid(sid);
				qubeClient.setSpid(spid);
				qubeClient.setTypefs(" ");
				qubeClient.setStatus("blocked");
				qubeClient.setRssi("0");
				qubeClient.setTx("0");
				qubeClient.setRx("0");
				qubeClient.setRadio("0");
				qubeClient.setDevname("UNKNOWN");

				if (bIsUpdate == true) {
					getClientDeviceService().save(qubeClient);
				} else {
					qubeClient.setCreatedOn(new Date());
					qubeClient.setCreatedBy(SessionUtil.currentUser(request.getSession()));
					qubeClient.setModifiedOn(new Date());
					qubeClient.setModifiedBy(qubeClient.getCreatedBy());
					getClientDeviceService().save(qubeClient);
				}

				if (client.pid.equals("Customer") && cid != null && !cid.trim().isEmpty()) {
					universalId = cid;
				} else if (client.pid.equals("Venue") && sid != null && !sid.trim().isEmpty()) {
					universalId = sid;
				} else if (client.pid.equals("Floor") && spid != null && !spid.trim().isEmpty()) {
					universalId = spid;
				} else {
					universalId = uid;
					policyFlag = false;
				}

				if (policyFlag == true) { // GLOBAL MQTT
					MQTTMessage = MessageFormat.format(mqttMsgTemplate,
							new Object[] { "BLOCK", universalId, client.ssid, qubeClient.getMac() });
					mqttPublisher.publish("{" + MQTTMessage + "}", universalId.toLowerCase());
				} else {
					MQTTMessage = MessageFormat.format(mqttMsgTemplate,
							new Object[] { "BLOCK", universalId, client.ssid, qubeClient.getMac() });
					mqttPublisher.publish("{" + MQTTMessage + "}", universalId.toLowerCase());
				}

				LOG.info("ACL MQTT MESSAGE " + MQTTMessage);

				model.put("quberACL", qubeClient);

				prepare(model, request, response);
				String str = "/facesix/web/acl/profile?sid=" + sid_profile + "&spid=" + spid_profile + "&cid=" + cid;
				response.sendRedirect(str);
				page = _CCC.pages.getPage("facesix.acl", "acl");

			} else {
				page = _CCC.pages.getPage("facesix.login", "login");
			}

		} catch (Exception e) {
			LOG.info("While ACL Save Error ," + e);
			page = _CCC.pages.getPage("facesix.acl", "acl");
		}
		return page;
	}

	@RequestMapping(value = "profile", method = RequestMethod.GET)
	public String qubercast(Map<String, Object> model, @RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "client_mac", required = false) String client_mac, HttpServletRequest request,
			HttpServletResponse response) {

		ClientDevice qubeClient = null;
		if (SessionUtil.isAuthorized(request.getSession())) {

			if (qubeClient == null) {
				qubeClient = new ClientDevice();
			}

			if (client_mac != null) {
				qubeClient.setConn(client_mac);
			}
			ArrayList<Device> devicelist = new ArrayList<Device>();
			List<Device> device = null;
			device = deviceManager.findByCid(cid);

			if (device != null) {
				for (Device dv : device) {

					devicelist.add(dv);
				}
			}

			if (client_mac != null && !client_mac.isEmpty()) {
				model.put("client_mac", client_mac);
			}
			model.put("devicelist", devicelist);

			prepare(model, request, response);
			model.put("quberACL", qubeClient);
			model.put("sid", sid);
			model.put("spid", spid);
			model.put("cid", cid);

			if (cid == null || cid.isEmpty()) {
				cid = SessionUtil.getCurrentCustomer(request.getSession());
			}

			model.put("GatewayFinder", CustomerUtils.GatewayFinder(cid));
			model.put("GeoFinder", CustomerUtils.GeoFinder(cid));
			model.put("Gateway", CustomerUtils.Gateway(cid));
			model.put("GeoLocation", CustomerUtils.GeoLocation(cid));
			model.put("Heatmap", CustomerUtils.Heatmap(cid));

			spid_profile = spid;
			sid_profile = sid;

			LOG.info("getquberACL" + qubeClient);
			return _CCC.pages.getPage("facesix.acl", "acl");
		} else {
			return _CCC.pages.getPage("facesix.login", "login");
		}
	}

	private DeviceService getDeviceService() {
		if (devService == null) {
			devService = Application.context.getBean(DeviceService.class);
		}
		return devService;
	}

	private ClientDeviceService getClientDeviceService() {
		if (_clientDeviceService == null) {
			_clientDeviceService = Application.context.getBean(ClientDeviceService.class);
		}
		return _clientDeviceService;
	}

}
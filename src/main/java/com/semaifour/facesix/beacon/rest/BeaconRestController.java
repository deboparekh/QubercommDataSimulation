package com.semaifour.facesix.beacon.rest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.mqtt.Payload;
import com.semaifour.facesix.rest.FSqlRestController;
import com.semaifour.facesix.simulatedBeacon.BeaconAssociation;
import com.semaifour.facesix.simulatedBeacon.BeaconAssociationService;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * Rest Controller for managing beacon scanning, checkout, checkin, etc
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/beacon")
public class BeaconRestController extends WebController {

	@Autowired
	BeaconDeviceService beaconDeviceService;
	
	@Autowired
	CustomerUtils customerUtils;
	
	@Autowired
	BeaconAssociationService beaconAssociationService;

	/* Room show preference */
	Map<String, Integer> roomInxPreference = new HashMap<String, Integer>() {
		{
			put("AR & NCT (Ground Floor)", 0);
			put("Refraction Room (Ground Floor)", 1);
			put("Consultation Room-1 (Ground Floor)", 2);
			put("Consultation Room-2 (Ground Floor)", 3);
			put("Consultation Room-3 (Ground Floor)", 4);
			put("Counselling Room (Ground Floor)", 5);
			put("Pharmacy (Ground Floor)", 6);
			put("Opticals (Ground Floor)", 7);
			put("Cash (Ground Floor)", 8);
			put("Hospital", 9);

		}
	};

	static Logger LOG = LoggerFactory.getLogger(BeaconRestController.class.getName());

	@Autowired
	private BeaconService beaconService;

	@Autowired
	private FSqlRestController fsqlRestController;

	@Autowired
	private DeviceEventPublisher mqttPublisher;

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/scanned", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> scannedList(HttpServletRequest request, HttpServletResponse response) {
		// FIXME: enable scoping and return only becons scanned by this session
		// return beaconService.getScannedBeacons(request.getSession().getId());
		return beaconService.getScannedBeacons();
	}

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/checkedout", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> BycheckedoutList(
			@RequestParam(value = "cid", required = false) String cid) {
		String status = "checkedout";
		if (cid != null) {
			return beaconService.getSavedBeaconByCidAndStatus(cid, status);
		}
		return beaconService.getSavedBeaconsByStatus(status);
	}

	/**
	 * 
	 * Return list of beacons in scanned list currently
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/scanner", method = RequestMethod.GET)
	public @ResponseBody Collection<Beacon> checkedoutList(@RequestParam(value = "suid", required = true) String suid) {
		return beaconService.getSavedBeaconsByScanner(suid);
	}

	/**
	 * 
	 * Assign a beacon to use
	 * 
	 * @param macaddr
	 * @param assto
	 * @return
	 */
	/*
	 * @RequestMapping(value = "/checkout", method = RequestMethod.POST)
	 * public @ResponseBody Beacon checkout(@RequestParam(value="macaddr",
	 * required=true) String macaddr,
	 * 
	 * @RequestParam(value="assto", required=true) String assto,
	 * HttpServletRequest request, HttpServletResponse response) { return
	 * beaconService.checkout(macaddr, assto, "unknown", whoami(request,
	 * response),request); }
	 */

	/**
	 * Check-in a used beacon
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/checkin", method = RequestMethod.POST)
	public @ResponseBody Beacon checkin(@RequestParam(value = "id", required = true) String id,
			HttpServletRequest request, HttpServletResponse response) {
		return beaconService.checkin(id, whoami(request, response));
	}

	/**
	 * 
	 * Returns beacon found in scanned list of beacon
	 * 
	 * @param macaddr
	 * @return
	 */
	@RequestMapping(value = "/get/scanned", method = RequestMethod.GET)
	public Beacon scanned(@RequestParam("macaddr") String macaddr, HttpServletRequest request,
			HttpServletResponse response) {
		return beaconService.getScannedBeacon(macaddr, request.getSession().getId());
	}

	@RequestMapping(value = "/remove/scanned", method = RequestMethod.DELETE)
	public Beacon removeScannedBeacon(@RequestParam("macaddr") String macaddr, HttpServletRequest request,
			HttpServletResponse response) {
		return beaconService.removeScannedBeacon(macaddr, request.getSession().getId());
	}

	@RequestMapping(value = "/clear/scanned", method = RequestMethod.DELETE)
	public void removeScannedBeacon(HttpServletRequest request, HttpServletResponse response) {
		beaconService.clearScannedBeacons(request.getSession().getId());
	}

	@RequestMapping(value = "/triggerscan", method = RequestMethod.GET)
	public Payload triggerscan(@RequestParam("cid") String cid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(cid, "scan-beacon-tags", "start scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/start", method = RequestMethod.GET)
	public Payload triggerScanStart(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "start-scan-beacon-tags", "start scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/abort", method = RequestMethod.GET)
	public Payload triggerScanAbort(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "abort-scan-beacon-tags", "abort scanning beacon tags", request, response);
	}

	@RequestMapping(value = "/trigger/scan/complete", method = RequestMethod.GET)
	public Payload triggerScanComplete(@RequestParam("suid") String suid, HttpServletRequest request,
			HttpServletResponse response) {
		return this.sendScannerCommand(suid, "complete-scan-beacon-tags", "complete scanning beacon tags", request,
				response);
	}

	/**
	 * 
	 * Builds a Paylod and sends it as a scanner command
	 * 
	 * @param suid
	 * @param opcode
	 * @param message
	 * @param request
	 * @param response
	 * @return
	 */
	private Payload sendScannerCommand(String cid, String opcode, String message, HttpServletRequest request,
			HttpServletResponse response) {
		Payload payload = new Payload(opcode, whoami(request, response), cid, message);
		payload.put("reqid", request.getSession().getId());
		payload.put("cid", cid);
		if (beaconService.sendBeaconCommand(payload)) {
			payload.put("status", "sent-success");
		} else {
			payload.put("status", "sent-failed");
		}
		return payload;
	}


	@RequestMapping(value = "/checkout/beacon", method = RequestMethod.POST)
	public @ResponseBody Beacon checkout(@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "assto", required = false) String assto,
			@RequestParam(value = "patient", required = false) String type,
			@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "bi", required = false) String bi,
			@RequestParam(value = "txpwr", required = false) String txpwr,
			@RequestParam(value = "tagmod", required = false) String tagmod,
			@RequestParam(value = "reftx", required = false) String reftx, HttpServletRequest request,
			HttpServletResponse response) {

		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		// String patientNameAndType = assto + "/" + type;
		// LOG.info(" macaddr " + macaddr + " assto " + assto + " patient Type "
		// + type + " cid " + cid+ "tagname" + name);
		return beaconService.checkout(macaddr, assto, type, cid, name, bi, txpwr, tagmod, reftx, null,
				whoami(request, response), request);
	}

	@RequestMapping(value = "/checkin/beacon", method = RequestMethod.POST)
	public @ResponseBody Beacon checkin(@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "macaddr", required = false) String macaddr, HttpServletRequest request,
			HttpServletResponse response) {

		// LOG.info("ID==>" + id);
		return beaconService.checkin(id, whoami(request, response));
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public @ResponseBody void delete(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "id", required = false) String id, HttpServletRequest request,
			HttpServletResponse response) {

		// LOG.info("==========Checkedin ALL TAGS ==>");
		// LOG.info("========== CID ==> " + cid + " id " + id);

		List<Beacon> beacon = new ArrayList<Beacon>();

		if (cid != null && !cid.isEmpty() && id == null) {
			List<Beacon> BybeaconCid = beaconService.getSavedBeaconByCid(cid);
			beacon.addAll(BybeaconCid);
			// LOG.info("==========BY TAG CId " + cid);
		} else {
			Beacon BybeaconId = beaconService.getSavedBeacon(id);
			beacon.add(BybeaconId);
			// LOG.info("==========BY TAG Id " + id);
		}

		if (beacon != null) {
			for (Beacon b : beacon) {
				beaconService.markExitForBeacon(b);
				b.setStatus("checkedin");
				b.setUpdatedstatus("checkedin");
				beaconService.save(b, true);
				// LOG.info("macAdddr " + b.getMacaddr());
				// LOG.info("Message " + message);
			}
			beaconService.delete(beacon);
		}
	}

	@RequestMapping(value = "/scanned", method = RequestMethod.GET)
	@SuppressWarnings("unchecked")
	public JSONObject scanned(@RequestParam(value = "cid", required = true) String cid) throws IOException {

		JSONObject devlist = new JSONObject();
		try {

			String beacon_cid = "";
			JSONObject dev = null;
			JSONArray dev_array = new JSONArray();

			// LOG.info(" *** cid *** " +cid);

			Collection<Beacon> beacon = beaconService.getScannedBeacons();

			// LOG.info("TAG LIST " +beacon.toString());

			if (beacon != null) {
				for (Beacon dv : beacon) {
					beacon_cid = dv.getCid();
					// LOG.info("===cid==== " +cid +"beacon_cid " +beacon_cid);
					if (cid.equals(beacon_cid)) {
						// LOG.info("===cid EQU==== " +cid +"beacon_cid "
						// +beacon_cid);
						dev = new JSONObject();
						String scid = dv.getScannerUid();
						String mid = dv.getMacaddr();
						dev.put("id", dv.getId());
						dev.put("uid", dv.getUid());
						dev.put("macaddr", mid);
						dev.put("scannerUid", scid);

						Beacon bcMac = beaconService.findOneByMacaddr(mid);
						if (bcMac != null) {
							// LOG.info("Beacon CheckedIn Already!!!!");
							dev.put("checkedin", "checkedin");
						} else {
							// LOG.info("New beacon!!!!");
							dev.put("newbeacon", "newbeacon");
						}
						BeaconDevice device = beaconDeviceService.findOneByUid(scid);
						if (device != null) {
							dev.put("scanner", device.getName());
						} else {
							dev.put("scanner", "Unknown");
						}

						dev.put("minor", dv.getMinor());
						dev.put("major", dv.getMajor());
						dev.put("dev_name", dv.getDevice_name());
						dev_array.add(dev);
					} else {
						// LOG.info("other customer beacon " + cid);
						continue;
					}
				}
				devlist.put("scanned", dev_array);
			}
		} catch (Exception e) {
			LOG.info("while getting scanned beacon list error", e);
		}
		return devlist;
	}

	@RequestMapping(value = "/tagimport", method = RequestMethod.POST)
	public JSONObject jsonFileRead(MultipartHttpServletRequest request, String cid, HttpServletRequest req,
			HttpServletResponse res) throws IOException {

		net.sf.json.JSONObject JSONCONF = null;
		// LOG.info("CID" + cid);
		try {

			Iterator<String> itrator = request.getFileNames();
			MultipartFile multiFile = request.getFile(itrator.next());

			String content = new String(multiFile.getBytes(), "UTF-8");
			JSONCONF = net.sf.json.JSONObject.fromObject(content);
			String scannerUid = "";

			if (JSONCONF.get("scannerUid") != null) {
				scannerUid = (String) JSONCONF.get("scannerUid");
			} else {
				LOG.info("$$$$$$$ SCANNER UID NULL $$$$$$$$$$ ");
				return null;
			}

			LOG.info(" ScannerUid" + scannerUid);
			LOG.info("CONFIG  Template " + JSONCONF);

			JSONArray array = JSONCONF.getJSONArray("attributes");
			for (int i = 0; i < array.size(); i++) {
				String mac = array.getJSONObject(i).getString("macaddr");
				String ato = array.getJSONObject(i).getString("assignedTo");
				String tag = array.getJSONObject(i).getString("tag_type");
				String tagmod = array.getJSONObject(i).getString("tag_model");
				String reftx = array.getJSONObject(i).getString("ref_txpwr");
				if (tagmod == null || tagmod.isEmpty()) {
					tagmod = "neck";
				}

				if (reftx == null || reftx.isEmpty()) {
					reftx = "-59";
				}

				if (ato == null || ato.isEmpty()) {
					ato = "quser";
				}
				if (tag == null || tag.isEmpty()) {
					tag = "qtag";
				}
				// LOG.info("JSOn STR " +
				// array.getJSONObject(i).getString("macaddr"));

				beaconService.checkout(mac, ato, tag, cid, "qubertag", "1000", "4", tagmod, reftx, scannerUid,
						whoami(req, res), req);
			}

		} catch (Exception e) {
			LOG.info("WHILE FILE UPLOAD ERROR {}", e);
		}
		return JSONCONF;
	}

	@RequestMapping(value = "/checkedout", method = RequestMethod.GET)
	public JSONObject checkedout(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "name", required = false) String name) throws IOException {

		JSONObject devlist = new JSONObject();
		try {
			JSONObject dev = null;
			JSONArray dev_array = new JSONArray();
			List<Beacon> beacon = null;

			if (name != null && name.contains(":")) {
				beacon = beaconService.getSavedBeaconByCidAndMacAddr(cid, name.toUpperCase());
			} else if (sid != null && !sid.equals("") && name != null && !name.equalsIgnoreCase("undefined")) {
				beacon = beaconService.getSavedBeaconBySidAndAssignedTo(sid, name);
				if (beacon == null || beacon.isEmpty()) {
					beacon = beaconService.getSavedBeaconBySidAndTagType(sid, name);
					if (beacon == null || beacon.isEmpty()) {
						beacon = beaconService.getSavedBeaconBySid(sid);
					}
				}
			} else {
				beacon = beaconService.getSavedBeaconByCidAndAssignedTo(cid, name);
				if (beacon == null || beacon.isEmpty()) {
					beacon = beaconService.getSavedBeaconByCidAndTagType(cid, name);
				}
			}

			String status = "";
			String color = "";
			String fafa = "";

			if (beacon == null || beacon.isEmpty()) {
				beacon = beaconService.getSavedBeaconByCid(cid);
			}

			if (beacon != null) {
				for (Beacon dv : beacon) {
					status = dv.getStatus();
					if (status.equals("checkedout")) {
						dev = new JSONObject();
						dev.put("id", dv.getId());
						dev.put("uid", dv.getUid());
						dev.put("macaddr", dv.getMacaddr());
						dev.put("state", dv.getState().toUpperCase());
						dev.put("dev_name", dv.getDevice_name());
						dev.put("assignedTo", dv.getAssignedTo().toUpperCase());
						dev.put("tagtype", dv.getTagType().toUpperCase());

						String debug = "unchecked";
						if (dv.getDebug() != null) {
							debug = dv.getDebug().equals("enable") ? "checked" : "unchecked";
						}
						dev.put("debugflag", debug);

						if (dv.getBattery_level() != 0) {
							int battery = dv.getBattery_level();
							String batteryinfo = beaconService.batteryStatus(battery);
							dev.put("fafa", batteryinfo.split("&")[0]);
							dev.put("color", batteryinfo.split("&")[1]);
							dev.put("battery", battery);// battery percentage
						} else {
							color = "black";
							fafa = "fa fa-battery-empty fa-2x";
							dev.put("fafa", fafa);
							dev.put("color", color);
							dev.put("battery", 0);
						}
						String floorName = (dv.getLocation() == null || dv.getLocation().isEmpty()) ? "NA"
								: dv.getLocation().toUpperCase();
						dev.put("location", floorName); // floor name

						String reciverAlias = dv.getReciveralias() == null ? "NA" : dv.getReciveralias().toUpperCase();
						dev.put("alias", reciverAlias);

						dev_array.add(dev);
					}

				}
				devlist.put("checkedout", dev_array);
			} // LOG.info("devlist >>>>>>>>." + devlist.toString());
		} catch (Exception e) {
			LOG.info("while getting checkedout beacon list error", e);
		}
		return devlist;
	}

	@RequestMapping(value = "/debugByTag", method = RequestMethod.POST)
	public void debugByTag(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "debugflag", required = true) String flag, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		try {

			JSONObject tagJson = new JSONObject();
			JSONArray tagArray = new JSONArray();

			ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
			ArrayList<Beacon> list = new ArrayList<Beacon>();

			String status = "checkedout";
			Collection<Beacon> beacon = null;
			String template = " \"opcode\":\"{0}\",\"tag_list\":{1}";
			String message = "";
			String opcode = "tag_logging";
			String debugflag = "disable";

			if (flag.equals("true")) {
				debugflag = "enable";
			}

			if (cid == null || cid.isEmpty()) {
				cid = SessionUtil.getCurrentCustomer(request.getSession());
			}

			if (macaddr == null) { // selectAll
				beacon = beaconService.getSavedBeaconByCidAndStatus(cid, status);
				beaconList.addAll(beacon);
				LOG.info("Tag Select All by cid " + cid);

			} else { // Select By Tag
				Beacon b = beaconService.findOneByMacaddr(macaddr);
				beaconList.add(b);
				LOG.info("Tag Based MQTT Topic By MacAddr " + macaddr);
			}

			if (beaconList != null && beaconList.size() > 0) {
				for (Beacon b : beaconList) {
					b.setDebug(debugflag);
					b.setModifiedOn(new Date());
					b.setModifiedBy(whoami(request, response));
					list.add(b);

					macaddr = b.getMacaddr();

					tagJson.put("tag_uid", macaddr);
					tagJson.put("debug", debugflag);
					tagArray.add(tagJson);

				}
				beaconService.save(list);
			}

			message = MessageFormat.format(template, new Object[] { opcode, tagArray });
			mqttPublisher.publish("{" + message + "}", cid);

		} catch (Exception e) {
			LOG.info("Tag Debug enable error " + e);
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/simulateTagDetails", method = RequestMethod.POST)
	public boolean simulateTagDetails(HttpServletRequest request,HttpServletResponse response) {
		LOG.info("simulated");
		boolean result = false;
		String cid = request.getParameter("cid");
		String simulation = request.getParameter("simulation");
		String simulateVia = "";
		int maxCount = 0;
		if (simulation.equals("enable")) {
			simulateVia = request.getParameter("simulateVia");
			int tagCount = Integer.valueOf(request.getParameter("tagCount"));
			int deviceCount = Integer.valueOf(request.getParameter("deviceCount"));
			maxCount = Integer.valueOf(request.getParameter("maxCount"));
			beaconService.simulateTags(cid, tagCount);
			beaconDeviceService.simulateDevices(cid, deviceCount);
			List<BeaconAssociation> oldData = beaconAssociationService.findByCid(cid);
			if(oldData != null && oldData.size()>0) {
				beaconAssociationService.deleteAssociatedList(oldData);
			}
			beaconAssociationService.BeaconAssociation();
		}
		result = customerUtils.setSimulation(cid, simulation, simulateVia, maxCount);
		LOG.info("simulation is enabled for cid " + cid);
		return result;
	}
}

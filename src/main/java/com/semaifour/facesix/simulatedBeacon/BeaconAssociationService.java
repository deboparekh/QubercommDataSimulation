package com.semaifour.facesix.simulatedBeacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class BeaconAssociationService {
	private static String classname = BeaconAssociationService.class.getName();

	Logger LOG = LoggerFactory.getLogger(classname);

	@Autowired(required = false)
	private BeaconAssociationRepository repository;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private BeaconService beaconService;
	
	@Autowired
	private BeaconDeviceService beaconDeviceService;

	public void save(BeaconAssociation associatedBeacon) {
		repository.save(associatedBeacon);
	}
	
	public void delete(BeaconAssociation associatedBeacon) {
		repository.delete(associatedBeacon);
	}
	
	public void deleteAssociatedList(List<BeaconAssociation> associatedBeacon) {
		repository.delete(associatedBeacon);
	}

	public BeaconAssociation findByCidAndMacaddr(String cid,String macaddr) {
		return repository.findByCidAndMacaddr(cid,macaddr);
	}
	
	@Scheduled(fixedDelay=600000)
	public void BeaconAssociation() {
		String simulation = "enable";
		String cx_state = "ACTIVE";
		String deviceType = "receiver";
		String cid = null;
		Boolean logs = null;
		
		List<String> solution = Arrays.asList("GatewayFinder","GeoFinder");
		List<Customer> customerList =customerService.findBySimulationSolutionAndState(simulation,solution,cx_state);
		//testing
		customerList = customerService.findOneById("5a65cd7ddb9a525c12dd035e");
		List<String> cidList = new ArrayList<String>();
		List<Beacon> beaconList = null;
		List<BeaconDevice> deviceList = null;
		
		Map<String,Boolean> enableLogs = new HashMap<String,Boolean>();
		
		BeaconAssociation associatedBeacon = null;
		
		int num_of_devices = 0;
		
		Random rand = new Random();
		
		JSONParser parse = new JSONParser();

		for (Customer cx : customerList) {
			try {
				cid = cx.getId();
				logs = cx.getLogs() == null || cx.getLogs().equals("false") ? false : true;
				cidList.add(cid);
				enableLogs.put(cid, logs);
				beaconList = beaconService.getSavedBeaconByCid(cid);
				deviceList = beaconDeviceService.findByCidAndType(cid, deviceType);
				if (beaconList == null || beaconList.isEmpty() || deviceList == null || deviceList.isEmpty()) {
					continue;
				}

				num_of_devices = deviceList.size();
				for (Beacon b : beaconList) {
					
					int index = rand.nextInt(num_of_devices);
					BeaconDevice chosenDevice = deviceList.get(index);
					String sid = chosenDevice.getSid();
					String spid = chosenDevice.getSpid();
					String pixelResult = chosenDevice.getPixelresult();
					String macaddr = b.getMacaddr();
					
					org.json.simple.JSONObject result = (org.json.simple.JSONObject) parse.parse(pixelResult);
					org.json.simple.JSONArray latAndLon = (org.json.simple.JSONArray) result.get("result");
					
					result = (org.json.simple.JSONObject)latAndLon.get(0);
					String lat = (String)result.get("latitude");
					String lon = (String)result.get("longitude");
					
					associatedBeacon = findByCidAndMacaddr(cid,macaddr);
					
					if (associatedBeacon == null) {
						associatedBeacon = new BeaconAssociation();
						associatedBeacon.setMacaddr(macaddr);
					}
					associatedBeacon.setCid(cid);
					associatedBeacon.setSid(sid);
					associatedBeacon.setSpid(spid);
					associatedBeacon.setLat(lat);
					associatedBeacon.setUid(chosenDevice.getUid());
					associatedBeacon.setLon(lon);
					save(associatedBeacon);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public List<BeaconAssociation> findByCid(String cid) {
		return repository.findByCid(cid);
	}
	public List<BeaconAssociation> findBySid(String sid) {
		return repository.findBySpid(sid);
	}
	public List<BeaconAssociation> findBySpid(String spid) {
		return repository.findBySpid(spid);
	}
	public List<BeaconAssociation> findByUid(String uid) {
		return repository.findByUid(QueryParser.escape(uid));
	}
}

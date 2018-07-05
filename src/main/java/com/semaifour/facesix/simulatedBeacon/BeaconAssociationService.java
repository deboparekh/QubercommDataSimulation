package com.semaifour.facesix.simulatedBeacon;

import java.util.Arrays;
import java.util.List;
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
import com.semaifour.facesix.beacon.rest.GeoFinderRestController;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.site.PortionService;

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
	
	@Autowired
	private GeoFinderRestController geoFinderRestController;
	
	@Autowired
	PortionService portionService;

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
	
	@Scheduled(fixedDelay=1000)
	public void BeaconAssociation() {
		String simulation = "enable";
		String cx_state = "ACTIVE";
		
		List<String> solution = Arrays.asList("GatewayFinder","GeoFinder");
		List<Customer> customerList =customerService.findBySimulationSolutionAndState(simulation,solution,cx_state);
		/*//testing
		customerList = customerService.findOneById("5a65cd7ddb9a525c12dd035e");*/

		for (Customer cx : customerList) {
			associateBeaconForCx(cx);
		}
		
	}

	public void associateBeaconForCx(Customer cx) {
		
//		Boolean logs = null;
		try {
			
			List<Beacon> beaconList = null;
			Random rand = new Random();
			
			String deviceType = "receiver";
			final String cid = cx.getId();
			
//			logs = cx.getLogs() == null || cx.getLogs().equals("false") ? false : true;
			/*List<String> macaddrs = Arrays.asList("TE:ST:00:75:50:64","TE:ST:00:27:40:01");

			beaconList = beaconService.findByMacaddrs(macaddrs);*/


			beaconList = beaconService.getSavedBeaconByCid(cid);
			final List<BeaconDevice> deviceList = beaconDeviceService.findByCidAndType(cid, deviceType);
			if (beaconList == null || beaconList.isEmpty() || deviceList == null || deviceList.isEmpty()) {
				return;
			}

			final int num_of_devices = deviceList.size();
			beaconList.parallelStream().forEach(b -> {
				try {
					JSONParser parse = new JSONParser();
					BeaconAssociation associatedBeacon = null;

					String macaddr = b.getMacaddr();

					associatedBeacon = findByCidAndMacaddr(cid, macaddr);

					if (associatedBeacon == null) {
						associatedBeacon = new BeaconAssociation();
						associatedBeacon.setMacaddr(macaddr);
					}

					int index = rand.nextInt(num_of_devices);
					BeaconDevice chosenDevice = deviceList.get(index);
					String pixelResult = chosenDevice.getPixelresult();
					String devId = chosenDevice.getUid();
					String associatedUid = associatedBeacon.getUid();

					if (associatedUid != null || pixelResult == null) {
						while (devId.equals(associatedUid) || pixelResult == null) {
							index = rand.nextInt(num_of_devices);
							chosenDevice = deviceList.get(index);
							pixelResult = chosenDevice.getPixelresult();
							devId = chosenDevice.getUid();
						}
					}

					String sid = chosenDevice.getSid();
					String spid = chosenDevice.getSpid();
					org.json.simple.JSONObject result = (org.json.simple.JSONObject) parse.parse(pixelResult);
					org.json.simple.JSONArray latAndLon = (org.json.simple.JSONArray) result.get("result");

					result = (org.json.simple.JSONObject) latAndLon.get(0);
					String lat = (String) result.get("latitude");
					String lon = (String) result.get("longitude");

					associatedBeacon.setCid(cid);
					associatedBeacon.setSid(sid);
					associatedBeacon.setSpid(spid);
					associatedBeacon.setLat(lat);
					associatedBeacon.setUid(chosenDevice.getUid());
					associatedBeacon.setLon(lon);
					save(associatedBeacon);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
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

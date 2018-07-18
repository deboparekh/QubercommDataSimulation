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
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;

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
		// testing
		List<Customer> customerList =  customerService.findOneById("5a65cd7ddb9a525c12dd035e");//customerService.findBySimulationSolutionAndState(simulation,solution,cx_state);

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
			List<String> macaddrs = Arrays.asList("TE:ST:00:86:86:71","TE:ST:00:22:37:78","TE:ST:00:64:89:98");

			beaconList = beaconService.findByMacaddrs(macaddrs);


			//beaconList = beaconService.getSavedBeaconByCid(cid);
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
					String lat = null;
					String lon = null;
					String sid = null;
					String spid = null;
					String devId = null;

					associatedBeacon = findByCidAndMacaddr(cid, macaddr);

					if (associatedBeacon == null || !associatedBeacon.getMacaddr().equals("TE:ST:00:86:86:71")) {
						if(associatedBeacon == null) {
							associatedBeacon = new BeaconAssociation();
							associatedBeacon.setMacaddr(macaddr);
						}
						int index = rand.nextInt(num_of_devices);
						BeaconDevice chosenDevice = deviceList.get(index);
						String pixelResult = chosenDevice.getPixelresult();
						devId = chosenDevice.getUid();
						String associatedUid = associatedBeacon.getUid();

						if (associatedUid != null || pixelResult == null) {
							while (devId.equals(associatedUid) || pixelResult == null) {
								index = rand.nextInt(num_of_devices);
								chosenDevice = deviceList.get(index);
								pixelResult = chosenDevice.getPixelresult();
								devId = chosenDevice.getUid();
							}
						}

						sid = chosenDevice.getSid();
						spid = chosenDevice.getSpid();
						
						org.json.simple.JSONObject result = (org.json.simple.JSONObject) parse.parse(pixelResult);
						org.json.simple.JSONArray latAndLon = (org.json.simple.JSONArray) result.get("result");

						result = (org.json.simple.JSONObject) latAndLon.get(0);
						lat = (String) result.get("latitude");
						lon = (String) result.get("longitude");
					}  else {
						
						devId = associatedBeacon.getUid();
						sid = associatedBeacon.getSid();
						spid = associatedBeacon.getSpid();
						int height = associatedBeacon.getHeight();
						int width = associatedBeacon.getWidth();
						if(height == 0 && width == 0) {
							Portion p = portionService.findById(spid);
							height = p.getHeight();
							width = p.getWidth();
							associatedBeacon.setHeight(height);
							associatedBeacon.setWidth(width);
						}
						String x = associatedBeacon.getX();
						String y = associatedBeacon.getY();
						
						if (x == null || y == null) {
							x = "0";
							y = "0";
						} else {
							int xpos = Integer.valueOf(x);
							int ypos = Integer.valueOf(y);
							if (xpos < (width - 40)) {
								xpos += 40;
							} else if (xpos <= width && ypos < (height - 40)) {
								ypos += 40;
							} else {
								xpos = 0;
								ypos = 0;
							}
							x = String.valueOf(xpos);
							y = String.valueOf(ypos);
						}
						JSONObject result = geoFinderRestController.simulatePixels2Coordinate(spid, x, y);
						if(result != null && !result.isEmpty()) {
							lat = result.getString("latitude");
							lon = result.getString("longitude");
						} else  {
							lat = associatedBeacon.getLat();
							lon = associatedBeacon.getLon();
						}
						associatedBeacon.setX(x);
						associatedBeacon.setY(y);
					}

					associatedBeacon.setCid(cid);
					associatedBeacon.setSid(sid);
					associatedBeacon.setSpid(spid);
					associatedBeacon.setLat(lat);
					associatedBeacon.setUid(devId);
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

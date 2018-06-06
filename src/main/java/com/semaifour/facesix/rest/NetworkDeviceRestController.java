package com.semaifour.facesix.rest;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.beacon.rest.BLENetworkDeviceRestController;
import com.semaifour.facesix.beacon.rest.FinderReport;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.ClientDevice;
import com.semaifour.facesix.data.elasticsearch.device.ClientDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDevice;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.probe.oui.ProbeOUI;
import com.semaifour.facesix.probe.oui.ProbeOUIService;
import com.semaifour.facesix.session.SessionCache;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

/**
 * 
 * Rest Device Controller handles all rest calls for network configuration
 * 
 * @author mjs
 *
 */
@RestController
@RequestMapping("/rest/site/portion/networkdevice")
public class NetworkDeviceRestController extends WebController {

	static Logger LOG = LoggerFactory.getLogger(NetworkDeviceRestController.class.getName());
	static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
	static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
	static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
	static boolean ThreadTobeStarted = false;
	DateFormat parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@Autowired
	NetworkDeviceService networkDeviceService;

	@Autowired
	ClientDeviceService clientDeviceService;

	@Autowired
	SiteService siteService;

	@Autowired
	PortionService portionService;

	@Autowired
	FSqlRestController fsqlRestController;

	@Autowired
	EntityRestController entity;

	@Autowired
	DeviceService devService;

	@Autowired
	SessionCache sessionCache;

	@Autowired
	CustomerUtils CustomerUtils;

	@Autowired
	BeaconService beaconService;

	@Autowired
	FinderReport trilaterationReport;

	@Autowired
	BeaconDeviceService beacondeviceservice;

	@Autowired
	ProbeOUIService probeOUIService;

	@Autowired
	CustomerService customerService;

	@Autowired
	NetworkConfRestController networkConfRestController;

	@Autowired
	BLENetworkDeviceRestController bleNetworkDeviceRestController;

	@Autowired
	ElasticService elasticService;

	private String indexname = "facesix*";

	String device_history_event = "device-history-event";

	private int if_count = 0;

	private int num_snr = 0;
	private int num_svi = 0;
	private int num_swi = 0;
	private int num_flr = 0;
	private int num_ap = 0;
	private int blk_count = 0;

	List<Map<String, Object>> peer_txrxlist = null;
	Map<String, Object> vap_map = null;
	Map<String, Object> vap5g_map = null;

	@PostConstruct
	public void init() {
		peer_txrxlist = new ArrayList<Map<String, Object>>();
		vap_map = new HashMap<String, Object>();
		vap5g_map = new HashMap<String, Object>();

		indexname = _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
		device_history_event = _CCC.properties.getProperty("facesix.device.event.history.table", device_history_event);
	}

	/*
	 * 
	 * Builds an array condition condition + vap_mac:[ list[0].getUid(),
	 * list[i].getUid() ]
	 * 
	 */
	public static String buildArrayCondition(List<NetworkDevice> list, String fieldname) {

		// LOG.info("buildArrayCondition" + list.size());
		int apCount = 0;
		if (list.size() > 0) {
			StringBuilder sb = new StringBuilder(fieldname).append(":(");
			boolean isFirst = true;
			for (NetworkDevice nd : list) {
				if (nd.getTypefs().equals("ap")) {
					apCount++;
					if (isFirst) {
						isFirst = false;
					} else {
						sb.append(" OR ");
					}
					sb.append("\"").append(nd.getUid()).append("\"");
				}
			}
			sb.append(")");

			// LOG.info ("NUM AP " + apCount);
			if (apCount > 0) {
				return sb.toString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	public static String buildDeviceArrayCondition(List<Device> list, String fieldname) {
		if (list.size() > 0) {
			StringBuilder sb = new StringBuilder(fieldname).append(":(");
			boolean isFirst = true;
			for (Device beacon : list) {
				if (isFirst) {
					isFirst = false;
				} else {
					sb.append(" OR ");
				}
				sb.append("\"").append(beacon.getUid()).append("\"");
			}
			sb.append(")");
			return sb.toString();
		} else {
			return "";
		}
	}

	@RequestMapping(value = "/venuelist", method = RequestMethod.GET)
	public int checkedout(@RequestParam(value = "type", required = true) String type,
			@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = true) String sid) {

		int device_count = 0;

		if (type.equals("1")) {
			List<Portion> fsobjects = portionService.findBySiteId(sid);
			if (fsobjects.size() > 0) {
				// LOG.info("Size " + fsobjects.size());
				device_count = fsobjects.size();
			}
		}

		if (type.equals("4")) {
			List<NetworkDevice> dev = networkDeviceService.findBySid(sid);
			if (dev.size() > 0) {
				for (NetworkDevice nd : dev) {
					if (cid != null) {
						if (CustomerUtils.Gateway(cid) || CustomerUtils.Heatmap(cid)) {
							if (nd.getTypefs().equals("ap") && nd.getStatus().equals("active")) {
								device_count++;
							}
						} else if (CustomerUtils.GeoFinder(cid)) {
							if ((nd.getTypefs().equals("sensor") && nd.getStatus().equals("active"))
									|| (nd.parent.equals("ble") && nd.getStatus().equals("active"))) {
								device_count++;
							}

						} else {
							if ((nd.getTypefs().equals("ap") && nd.getStatus().equals("active"))
									|| (nd.getTypefs().equals("sensor") && nd.getStatus().equals("active"))
									|| (nd.parent.equals("ble") && nd.getStatus().equals("active"))) {
								device_count++;
							}
						}
					} else {
						if (nd.getTypefs().equals("ap") && nd.getStatus().equals("active")) {
							device_count++;
						}
					}

				}
			}

		}

		if (type.equals("2") || type.equals("3")) {
			List<NetworkDevice> devices = null;
			devices = networkDeviceService.findBySid(sid);
			if (devices.size() > 0) {

				for (NetworkDevice nd : devices) {

					String role = nd.getRole();

					if (type.equals("2")) {
						if (CustomerUtils.Gateway(cid) || CustomerUtils.Heatmap(cid)) {
							if (nd.getTypefs().equals("ap") && (role != null && !role.equals("ap"))) {
								device_count++;
							}
						} else if (CustomerUtils.GeoFinder(cid)) {
							if (nd.getTypefs().equals("sensor") || nd.getParent().equals("ble")) {
								device_count++;
							}
						} else {
							if (nd.getTypefs().equals("ap")) {
								device_count++;
							}
						}
					} else {
						if (CustomerUtils.Gateway(cid) || CustomerUtils.Heatmap(cid)) {
							if (nd.getTypefs().equals("ap") && (role == null || role.equals("ap"))) {
								device_count++;
							}

						} else if (CustomerUtils.GeoFinder(cid)) {
							if (nd.getTypefs().equals("sensor") || nd.getParent().equals("ble")) {
								device_count = device_count + nd.getCheckedoutTag();
							}

						} else {
							if (nd.getTypefs().equals("sensor") || nd.getParent().equals("ble")) {
								device_count++;
							}

						}

					}

				}
			}
		}

		if (CustomerUtils.trilateration(cid)) {
			if (type.equals("3")) {
				String status = "checkedout";
				String state = "inactive";

				List<Beacon> total_beacon = beaconService.getSavedBeaconBySidAndStatus(sid, status);
				List<Beacon> inactive_beacon = beaconService.getSavedBeaconByCidSidStateAndStatus(cid, sid, state,
						status);
				int total_checkedout_tags = total_beacon == null ? 0 : total_beacon.size();
				int inactive_tag_count = inactive_beacon == null ? 0 : inactive_beacon.size();
				device_count = total_checkedout_tags - inactive_tag_count;
			}
		}

		return device_count;
	}

	@RequestMapping(value = "/peercount", method = RequestMethod.GET)
	public int peercount(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid) {

		int device_count = 0;

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);

		if (devices == null) {
			return 0;
		}

		List<NetworkDevice> mlist = Collections.unmodifiableList(devices);
		List<NetworkDevice> list = new ArrayList<NetworkDevice>(mlist);
		Collections.sort(list);

		if (list.size() > 0) {

			for (NetworkDevice nd : list) {
				if (cid != null) {
					if (CustomerUtils.Gateway(cid)) {
						if (nd.getTypefs().equals("ap") && nd.getStatus().equals("active")) {
							device_count++;
						}
					} else if (CustomerUtils.GeoFinder(cid)) {
						if ((nd.getTypefs().equals("sensor") && nd.getStatus().equals("active"))
								|| (nd.parent.equals("ble") && nd.getStatus().equals("active"))) {
							device_count++;
						}

					} else {
						if ((nd.getTypefs().equals("ap") && nd.getStatus().equals("active"))
								|| (nd.getTypefs().equals("sensor") && nd.getStatus().equals("active"))
								|| (nd.parent.equals("ble") && nd.getStatus().equals("active"))) {
							device_count++;
						}
					}
				} else {
					if (nd.getTypefs().equals("ap") && nd.getStatus().equals("active")) {
						device_count++;
					}
				}

			}
		}

		return device_count;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/climap", method = RequestMethod.GET)
	public JSONObject climap(@RequestParam(value = "sid", required = true) String sid) {

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();

		dev_array = new JSONArray();
		JSONArray dev_array1 = new JSONArray();
		JSONArray dev_array2 = new JSONArray();

		dev_array1.add(0, "2G");
		// dev_array1.add (1, cnt24);
		dev_array2.add(0, "5G");
		// dev_array2.add (1, cnt5);

		dev_array.add(0, dev_array1);
		dev_array.add(1, dev_array2);

		devlist.put("typeOfDevices", dev_array);

		// LOG.info("typeOfDevices" +devlist.toString());

		return devlist;
	}

	public String size(String place) {
		String size = "10";
		if (place != null && place.equals("htmlchart")) {
			size = "2000";
		}
		return size;
	}

	@RequestMapping(value = "/rxtx", method = RequestMethod.GET)
	public List<Map<String, Object>> rxtx(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "time", required = false, defaultValue = "5m") String time,
			@RequestParam(value = "place", required = false) String place, HttpServletRequest request,
			HttpServletResponse response) {
		String fsql = null;

		String size = size(place);

		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == true) {
				fsql = "index=qubercomm_*,sort=timestamp desc,size=" + size + ",query=";
			} else {
				fsql = "index=" + indexname + ",sort=timestamp desc,size=" + size + ",query=timestamp:>now-" + time
						+ " AND ";
			}
		} catch (Exception e) {
			fsql = "index=" + indexname + ",sort=timestamp desc,size=5,query=timestamp:>now-" + time + " AND ";
		}

		String fsql5g = "";
		String fsql2g = "";
		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		/*
		 * LOGIC MOVED TO DeviceStatusMonitorService.java [mahi]
		 * 
		 * if (ThreadTobeStarted == false) { ThreadTobeStarted = true; TimerTask
		 * timerTask = new DeviceKeepAlive(); Timer timer = new Timer(true);
		 * timer.scheduleAtFixedRate(timerTask, 0, 1800*1000); }
		 */

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);
		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		List<Map<String, Object>> logs5g = EMPTY_LIST_MAP;

		if (devices != null && uid == null) {
			String uidbuilder = buildArrayCondition(devices, "uid");
			if (uidbuilder.length() > 0) {
				fsql = fsql + uidbuilder;
				fsql2g = fsql
						+ " AND radio_type:\"2.4Ghz\"|value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA)|table";
				fsql5g = fsql
						+ " AND radio_type:\"5Ghz\"|value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA)|table";

				logs = fsqlRestController.query(fsql2g);
				logs5g = fsqlRestController.query(fsql5g);
				// LOG.info("FSQ111L 5G UID ==> " + fsql5g);
				// LOG.info("FSQ111L 2G UID ==> " + fsql2g);
				logs.addAll(logs5g);
			} else {
				// LOG.info("Oops No infrastructure available ");
			}

		} else if (uid != null) {
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql2g = fsql
					+ " AND radio_type:\"2.4Ghz\"|value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA)|table";
			fsql5g = fsql
					+ " AND radio_type:\"5Ghz\"|value(_vap_rx_bytes,Rx,NA);value(_vap_tx_bytes,Tx,NA);value(timestamp,time,NA)|table";
			// LOG.info("FSQL 5G UID ==> " + fsql5g);
			// LOG.info("FSQL 2G UID ==> " + fsql2g);
			logs = fsqlRestController.query(fsql2g);
			logs5g = fsqlRestController.query(fsql5g);
			logs.addAll(logs5g);
		}

		// LOG.info("RXTX ==> " + logs);
		return logs;

	}

	@RequestMapping(value = "/venueagg", method = RequestMethod.GET)
	public List<Map<String, Object>> venueagg(@RequestParam(value = "sid", required = true) String sid,
			HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = null;
		List<Map<String, Object>> rxtx = null;
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();

		List<NetworkDevice> ulist = networkDeviceService.findBySid(sid);
		List<NetworkDevice> mlist = Collections.unmodifiableList(ulist);
		List<NetworkDevice> list = new ArrayList<NetworkDevice>(mlist);

		Collections.sort(list);
		String flrName;
		String prev_spid = "";

		try {
			for (NetworkDevice nd : list) {

				if (prev_spid.equals(nd.spid) == false) {
					rxtx = rxtxagg(null, nd.spid, null, null, "120", "20", "2G5G", request, response);
					Portion port = portionService.findById(nd.spid);

					if (port != null) {
						flrName = port.getUid();
					} else {
						flrName = "Floor";
					}

					if (rxtx.size() > 0) {
						map = rxtx.get(0);
						map.put("Floor", flrName);
						if (ret.contains(map)) {
							// nothing to do
						} else {
							ret.add(map);
						}

					}

					prev_spid = nd.spid;
				}
			}
		} catch (Exception e) {

		}

		// LOG.info("VENUE MAP STR" + ret.toString());
		return ret;
	}

	@RequestMapping(value = "/flraggr", method = RequestMethod.GET)
	public List<Map<String, Object>> flraggr(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "time", required = false, defaultValue = "120") String time,
			HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = null;
		List<Map<String, Object>> rxtx = null;
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		if (sid == null) {
			rxtx = rxtxagg(null, spid, null, null, time, "20", "2G", request, response);
		} else {
			rxtx = rxtxagg(sid, null, null, null, time, "20", "2G", request, response);
		}

		// LOG.info("RXTX RES" + rxtx);
		if (rxtx.size() > 0) {
			map = rxtx.get(0);
			map.put("Radio", "2G");
			ret.add(map);
		}
		if (sid == null) {
			rxtx = rxtxagg(null, spid, null, null, time, "20", "5G", request, response);
		} else {
			rxtx = rxtxagg(sid, null, null, null, time, "20", "5G", request, response);
		}
		// LOG.info("RXTX RES11111111" + rxtx);
		if (rxtx.size() > 0) {
			map = rxtx.get(0);
			map.put("Radio", "5G");
			ret.add(map);
		}

		// LOG.info("FLOOR MAP STR" + ret.toString());
		return ret;
	}

	@RequestMapping(value = "/peeraggr", method = RequestMethod.GET)
	public List<Map<String, Object>> peeraggr(@RequestParam(value = "uid", required = true) String uid,
			@RequestParam(value = "time", required = false, defaultValue = "120") String time) {

		Map<String, Object> map = null;
		if (peer_txrxlist.size() > 0) {
			map = peer_txrxlist.get(0);
			String id = (String) map.get("uid");
			if (uid.equals(id)) {
				return peer_txrxlist;
			}
		}

		return EMPTY_LIST_MAP;
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/rxtxagg", method = RequestMethod.GET)
	public List<Map<String, Object>> rxtxagg(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "time", required = false, defaultValue = "5") String time,
			@RequestParam(value = "interval", required = false, defaultValue = "2") String interval,
			@RequestParam(value = "radio", required = false, defaultValue = "2G5G") String radio,
			HttpServletRequest request, HttpServletResponse response) {
		String esql = "";
		int count = 0;
		time = time + "m";
		interval = interval + "m";
		if (time.equals("5")) {
			interval = "1m";
		} else if (time.equals("15")) {
			interval = "2m";
		} else if (time.equals("30")) {
			interval = "5m";
		} else if (time.equals("60")) {
			interval = "10m";
		} else if (time.equals("120")) {
			interval = "20m";
		}

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);
		// LOG.info("Device" + devices);
		if (devices != null && uid == null) {
			String uidBuidler = buildArrayCondition(devices, "uid");
			// LOG.info("dddd" + uidBuidler);
			if (uidBuidler.length() > 0) {
				esql = esql + uidBuidler;
			} else {
				return EMPTY_LIST_MAP;
			}
			// esql = esql + buildArrayCondition(devices, "uid");

		} else if (uid != null) {
			esql = esql + "uid:\"" + uid + "\"";
		}

		if (radio.equals("2G")) {
			esql = esql + "AND radio_type:\"2.4Ghz\"";
		} else if (radio.equals("5G")) {
			esql = esql + "AND radio_type:\"5Ghz\"";
		} else {
			esql = esql + "AND radio_type:(\"5Ghz\" OR \"2.4Ghz\")";
		}

		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == false) {
				esql = esql + " AND timestamp:>now-" + time;
			}
		} catch (Exception e) {
			esql = esql + " AND timestamp:>now-" + time;
		}

		// LOG.info("ESQL" + esql);

		if (esql != null) {
			QueryBuilder builder = QueryBuilders.queryStringQuery(esql);
			SearchQuery sq = new NativeSearchQueryBuilder().withQuery(builder)
					.withSort(new FieldSortBuilder("timestamp").order(SortOrder.DESC))
					.addAggregation(AggregationBuilders.dateHistogram("bucket").field("timestamp")
							.interval(new DateHistogramInterval(interval)).minDocCount(1)
							.subAggregation(AggregationBuilders.min("min_vap_rx_bytes").field("_vap_rx_bytes"))
							.subAggregation(AggregationBuilders.max("max_vap_rx_bytes").field("_vap_rx_bytes"))
							.subAggregation(AggregationBuilders.avg("avg_vap_rx_bytes").field("_vap_rx_bytes"))
							.subAggregation(AggregationBuilders.min("min_vap_tx_bytes").field("_vap_tx_bytes"))
							.subAggregation(AggregationBuilders.max("max_vap_tx_bytes").field("_vap_tx_bytes"))
							.subAggregation(AggregationBuilders.avg("avg_vap_tx_bytes").field("_vap_tx_bytes")))
					.build();
			sq.addIndices(indexname);
			// sq.addTypes("message");
			sq.setPageable(new PageRequest(0, 1));

			Histogram histogram = _CCC.elasticsearchTemplate.query(sq, new ResultsExtractor<Histogram>() {
				@Override
				public Histogram extract(SearchResponse response) {
					return response.getAggregations().get("bucket");
				}
			});

			List<Map<String, Object>> rxtx = new ArrayList<Map<String, Object>>();
			Map<String, Object> map = null;
			for (Histogram.Bucket entry : histogram.getBuckets()) {
				count++;
				map = new HashMap();
				map.put("time", entry.getKey().toString());
				List<Aggregation> aggs = entry.getAggregations().asList();
				for (Aggregation agg : aggs) {
					String name = agg.getName();
					map.put(agg.getName(), ((SingleValue) agg).value());

				}

				rxtx.add(map);

				if (count >= 10) {
					break;
				}

			}
			// LOG.info("RXTX AFF==>" + rxtx);
			return rxtx;
		} else {
			return EMPTY_LIST_MAP;
		}
	}

	@RequestMapping(value = "/netflow", method = RequestMethod.GET)
	public List<Map<String, Object>> netflow(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "time", required = false, defaultValue = "5") String time, HttpServletRequest request,
			HttpServletResponse response) {
		String fsql = null;

		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == true) {
				fsql = "index=qubercomm_*,sort=timestamp desc,size=10,query=";
			} else {
				fsql = "index=" + indexname + ",sort=timestamp desc,size=10,query=timestamp:>now-" + time + "m"
						+ " AND ";
			}
		} catch (Exception e) {
			fsql = "index=" + indexname + ",sort=timestamp desc,size=10,query=timestamp:>now-" + time + "m" + " AND ";
		}

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);

		if (devices != null && uid == null) {
			String uidbuilder = buildArrayCondition(devices, "uid");
			if (uidbuilder.length() > 0) {
				fsql = fsql + uidbuilder;
				fsql = fsql
						+ " AND web_stats:\"Qubercloud Manager\"|value(num_social,social,NA);value(num_chat,chat,NA);value(num_ecomm,ecom,NA);value(num_http,web,NA);value(timestamp,time,NA)|table";
				return fsqlRestController.query(fsql);
			}
		} else if (uid != null) {
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql = fsql
					+ " AND web_stats:\"Qubercloud Manager\"|value(num_social,social,NA);value(num_chat,chat,NA);value(num_ecomm,ecom,NA);value(num_http,web,NA);value(timestamp,time,NA)|table";
			return fsqlRestController.query(fsql);
		}

		return EMPTY_LIST_MAP;
	}

	@RequestMapping(value = "/netagg", method = RequestMethod.GET)
	public List<Map<String, Object>> netagg(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "time", required = false, defaultValue = "30") String time,
			@RequestParam(value = "interval", required = false, defaultValue = "5") String interval,
			HttpServletRequest request, HttpServletResponse response) {
		String esql = "";
		int count = 0;
		time = time + "m";
		interval = interval + "m";
		if (time.equals("5")) {
			interval = "1m";
		} else if (time.equals("15")) {
			interval = "2m";
		} else if (time.equals("30")) {
			interval = "5m";
		} else if (time.equals("60")) {
			interval = "10m";
		} else if (time.equals("120")) {
			interval = "20m";
		}

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);

		if (devices != null && uid == null) {
			String uidBuilder = buildArrayCondition(devices, "uid");
			if (uidBuilder.length() > 0) {
				esql = esql + uidBuilder;
			} else {
				return EMPTY_LIST_MAP;
			}
			// esql = esql + buildArrayCondition(devices, "uid");

			esql = esql + "AND web_stats:\"Qubercloud Manager\"";
		} else if (uid != null) {
			esql = esql + "uid:\"" + uid + "\"";
			esql = esql + "AND web_stats:\"Qubercloud Manager\"";
		}

		try {
			if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == false) {
				esql = esql + " AND timestamp:>now-" + time;
			}
		} catch (Exception e) {
			esql = esql + " AND timestamp:>now-" + time;
		}

		if (esql != null) {
			QueryBuilder builder = QueryBuilders.queryStringQuery(esql);
			SearchQuery sq = new NativeSearchQueryBuilder().withQuery(builder)
					.withSort(new FieldSortBuilder("timestamp").order(SortOrder.DESC))
					.addAggregation(AggregationBuilders.dateHistogram("bucket").field("timestamp")
							.interval(new DateHistogramInterval(interval)).minDocCount(1)
							.subAggregation(AggregationBuilders.min("min_num_social").field("web_social_count"))
							.subAggregation(AggregationBuilders.max("max_num_social").field("web_social_count"))
							.subAggregation(AggregationBuilders.avg("avg_num_social").field("web_social_count"))
							.subAggregation(AggregationBuilders.min("min_num_chat").field("web_chat_count"))
							.subAggregation(AggregationBuilders.max("max_num_chat").field("web_chat_count"))
							.subAggregation(AggregationBuilders.avg("avg_num_chat").field("web_chat_count"))
							.subAggregation(AggregationBuilders.min("min_num_ecomm").field("web_ecomm_count"))
							.subAggregation(AggregationBuilders.max("max_num_ecomm").field("web_ecomm_count"))
							.subAggregation(AggregationBuilders.avg("avg_num_ecomm").field("web_ecomm_count"))
							.subAggregation(AggregationBuilders.min("min_num_http").field("web_http_count"))
							.subAggregation(AggregationBuilders.max("max_num_http").field("web_http_count"))
							.subAggregation(AggregationBuilders.avg("avg_num_http").field("web_http_count")))
					.build();
			sq.addIndices(indexname);
			// sq.addTypes("message");
			sq.setPageable(new PageRequest(0, 1));

			Histogram histogram = _CCC.elasticsearchTemplate.query(sq, new ResultsExtractor<Histogram>() {
				@Override
				public Histogram extract(SearchResponse response) {
					return response.getAggregations().get("bucket");
				}
			});

			List<Map<String, Object>> net = new ArrayList<Map<String, Object>>();
			Map<String, Object> map = null;
			for (Histogram.Bucket entry : histogram.getBuckets()) {
				count++;
				map = new HashMap();
				map.put("time", entry.getKey());
				List<Aggregation> aggs = entry.getAggregations().asList();
				for (Aggregation agg : aggs) {
					String name = agg.getName();
					map.put(agg.getName(), ((SingleValue) agg).value());

				}
				net.add(map);
				if (count >= 10) {
					break;
				}

			}
			return net;
		} else {
			return EMPTY_LIST_MAP;
		}
	}

	@RequestMapping(value = { "status", "status/all" }, method = RequestMethod.GET)
	public List<NetworkDevice> status(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration,
			HttpServletRequest request, HttpServletResponse response) {
		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}
		return getstatus(sid, spid, swid, uid, duration, "all", request, response);
	}

	@RequestMapping(value = "status/active", method = RequestMethod.GET)
	public List<NetworkDevice> statusactive(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration,
			HttpServletRequest request, HttpServletResponse response) {
		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}
		return getstatus(sid, spid, swid, null, duration, "active", request, response);
	}

	@RequestMapping(value = "status/inactive", method = RequestMethod.GET)
	public List<NetworkDevice> statusinactive(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration,
			HttpServletRequest request, HttpServletResponse response) {
		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}
		return getstatus(sid, spid, swid, null, duration, "inactive", request, response);
	}

	public List<NetworkDevice> getstatus(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration,
			@PathVariable("status") String status, HttpServletRequest request, HttpServletResponse response) {

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devies = networkDeviceService.findBy(spid, sid, swid);

		if (devies != null && devies.size() > 0 && uid == null) {
			String fsql = null; // "index=" + indexname +
								// ",query=timestamp:>now-" + duration + " AND
								// ";

			try {
				if (((Boolean) sessionCache.getAttribute(request.getSession(), "demo")) == true) {
					fsql = "index=qubercomm_*,sort=timestamp desc,size=10,query=";
				} else {
					fsql = "index=" + indexname + ",query=timestamp:>now-" + duration + " AND ";
				}
			} catch (Exception e) {
				fsql = "index=" + indexname + ",query=timestamp:>now-" + duration + " AND ";
			}

			String uidBuilder = buildArrayCondition(devies, "vap_mac")
					+ "|bucket(vap_mac,uid,NA);value(timestamp,time,vindex=0);|table";

			if (uidBuilder.length() > 0) {
				fsql = fsql + uidBuilder;
			} else {
				return devies;
			}

			// fsql = fsql + buildArrayCondition(devies, "vap_mac") +
			// "|bucket(vap_mac,uid,NA);value(timestamp,time,vindex=0);|table";

			Map<String, Map<String, Object>> logs = fsqlRestController.queryMap(fsql, "uid");
			List<NetworkDevice> actives = new ArrayList<NetworkDevice>();
			List<NetworkDevice> inactives = new ArrayList<NetworkDevice>();

			for (NetworkDevice dv : devies) {
				Map<String, Object> m = logs.get(dv.getUid());
				if (m != null) {
					dv.setStatus("ACTIVE");
					actives.add(dv);
				} else {
					dv.setStatus("INACTIVE");
					inactives.add(dv);
				}
			}

			if ("ACTIVE".equalsIgnoreCase(status)) {
				devies = actives;
			} else if ("INACTIVE".equalsIgnoreCase(status)) {
				devies = inactives;
			}

		}
		return devies;
	}

	@RequestMapping(value = "/alerts", method = RequestMethod.GET)
	public List<String> alerts(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "30m") String duration,
			HttpServletRequest request, HttpServletResponse response) {

		String str = null;
		List<BeaconDevice> beaconDevice = null;
		List<Device> device = null;
		ArrayList<String> alert = new ArrayList<String>();

		final String state = "inactive";

		if (cid == null || cid.isEmpty()) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}

		if (CustomerUtils.Gateway(cid) || CustomerUtils.GatewayFinder(cid)) {

			if (sid != null) {
				device = getDeviceService().findBySidAndState(sid, state);
			} else if (spid != null) {
				device = getDeviceService().findBySpidAndState(spid, state);
			}

			for (Device d : device) {
				uid = d.getUid();
				str = "AP (Mac id: " + uid.toUpperCase() + ") Status : " + "inactive";
				alert.add(str);
			}
		}

		if (CustomerUtils.GeoFinder(cid) || CustomerUtils.GatewayFinder(cid)) {
			if (sid != null) {
				beaconDevice = beacondeviceservice.findBySidAndState(sid, state);
			} else if (spid != null) {
				beaconDevice = beacondeviceservice.findBySpidAndState(spid, state);
			}

			for (BeaconDevice beaconDev : beaconDevice) {
				uid = beaconDev.getUid();
				str = beaconDev.getType().toUpperCase() + " (Mac id :" + uid + " ) Status : inactive";
				alert.add(str);
			}
		}
		return alert;
	}

	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	public List<String> summary(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "30m") String duration,
			HttpServletRequest request, HttpServletResponse response) {

		ArrayList<String> summary = new ArrayList<String>();
		String str = "Number of Floors = " + num_flr;

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);

		if (devices != null && devices.size() > 0) {

			str = "Number of Floors =====================>  " + num_flr;
			summary.add(str);

			str = "Number of Servers ====================>  " + num_svi;
			summary.add(str);

			str = "Number of Switches ===================>  " + num_swi;
			summary.add(str);

			str = "Number of Access Points ===============>  " + num_ap;
			summary.add(str);

			str = "Number of Sensors ====================> " + num_snr;
			summary.add(str);
		}

		return summary;
	}

	@RequestMapping(value = "/activity", method = RequestMethod.GET)
	public List<Map<String, Object>> activity(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "24h") String duration) {

		JSONObject newJObject = null;
		JSONParser parser = new JSONParser();
		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		// String fsql = "index=" + indexname + ",sort=timestamp
		// desc,size=25,query=log_type:\"log\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";
		String fsql = "index=" + indexname + ",sort=timestamp desc,query=doctype:\"syslog\" AND timestamp:>now-"
				+ duration + " |value(message,snapshot,NA);value(timestamp,time,NA);|table";

		logs = fsqlRestController.query(fsql);

		return logs;
	}

	@RequestMapping(value = "/loginfo", method = RequestMethod.GET)
	public List<Map<String, Object>> loginfo(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "30m") String duration) {

		JSONObject newJObject = null;
		JSONParser parser = new JSONParser();
		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		String fsql = "index=" + indexname
				+ ",sort=timestamp desc,size=25,query=log_type:\"log\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";
		// String fsql = "index=" + indexname + ",sort=timestamp
		// desc,size=25,query=doctype:\"syslog\"|value(message,snapshot,NA);value(timestamp,time,NA);|table";

		logs = fsqlRestController.query(fsql);

		return logs;
	}

	public String recordSize(String place, String duration) {

		String size = "1";

		if (duration == null) {
			duration = "4h";
		}

		if (place != null && place.equals("htmlchart")) {
			if (duration.equals("4h")) {
				size = "10";
			} else if (duration.equals("6h")) {
				size = "20";
			} else if (duration.equals("8h")) {
				size = "30";
			} else if (duration.equals("12h")) {
				size = "40";
			} else {
				size = "50";
			}
		}
		return size;
	}

	@RequestMapping(value = "/getcpu", method = RequestMethod.GET)
	public List<Map<String, Object>> getcpu(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "1m") String duration,
			@RequestParam(value = "place", required = false) String place) {

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		String size = recordSize(place, duration);

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);
		String fsql = "index=" + indexname + ",sort=timestamp desc,size=" + size
				+ ",query=cpu_stats:\"Qubercloud Manager\" AND timestamp:>now-" + duration + " AND ";

		if (devices != null && devices.size() > 0 && uid == null) {

			String uidBuilder = buildArrayCondition(devices, "uid")
					+ "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";
			if (uidBuilder.length() > 0) {
				fsql = fsql + uidBuilder;
			} else {
				return EMPTY_LIST_MAP;
			}

			// fsql = fsql + buildArrayCondition(devices, "uid") +
			// "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";

			return fsqlRestController.query(fsql);
		} else if (uid != null) {
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql = fsql + "|value(uid,uid,NA);value(cpu_percentage,cpu,NA);value(timestamp,time,NA);|table";

			return fsqlRestController.query(fsql);

		} else {
			return EMPTY_LIST_MAP;
		}
	}

	@RequestMapping(value = "/gw_deviceupstate", method = RequestMethod.GET)
	public List<Map<String, Object>> DeviceUpTime(@RequestParam(value = "uid", required = true) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "30m") String duration) {

		List<Map<String, Object>> res = EMPTY_LIST_MAP;

		uid = uid.toLowerCase();

		String fsql = " index=" + indexname + ",sort=timestamp desc,size=1,query=cpu_stats:\"Qubercloud Manager\""
				+ " AND timestamp:>now-" + duration + " AND uid:\"" + uid + "\"|value(uid,uid,NA);"
				+ " value(cpu_percentage,cpu,NA);value(timestamp,time,NA);"
				+ " value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);"
				+ " value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);|table";

		res = fsqlRestController.query(fsql);

		return res;
	}

	@RequestMapping(value = "/getmem", method = RequestMethod.GET)
	public List<Map<String, Object>> getmem(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "1m") String duration,
			@RequestParam(value = "place", required = false) String place) {

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, swid);

		String size = recordSize(place, duration);

		String fsql = "index=" + indexname + ",sort=timestamp desc,size=" + size
				+ ",query=cpu_stats:\"Qubercloud Manager\" AND timestamp:>now-" + duration + " AND ";

		if (devices != null && devices.size() > 0 && uid == null) {

			String uidBuilder = buildArrayCondition(devices, "uid")
					+ "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";
			if (uidBuilder.length() > 0) {
				fsql = fsql + uidBuilder;
			} else {
				return EMPTY_LIST_MAP;
			}

			// fsql = fsql + buildArrayCondition(devices, "uid") +
			// "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";

			return fsqlRestController.query(fsql);
		} else if (uid != null) {
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql = fsql + "|value(uid,uid,NA);value(ram_percentage,mem,NA);value(timestamp,time,NA);|table";

			return fsqlRestController.query(fsql);
		} else {
			return EMPTY_LIST_MAP;
		}
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @RequestMapping(value = "/getpeers", method = RequestMethod.GET) public
	 * JSONObject getpeers(@RequestParam(value="sid", required=false) String
	 * sid,
	 * 
	 * @RequestParam(value="spid", required=false) String spid,
	 * 
	 * @RequestParam(value="swid", required=false) String swid,
	 * 
	 * @RequestParam(value="uid", required=false) String uid,
	 * 
	 * @RequestParam(value="duration", required=false, defaultValue="15s")
	 * String duration,
	 * 
	 * @RequestParam(value="report", required=false) String report) throws
	 * IOException {
	 * 
	 * JSONArray dev_array = new JSONArray();
	 * 
	 * JSONObject devlist = new JSONObject(); if (swid != null) { swid =
	 * swid.replaceAll("[^a-zA-Z0-9]", ""); }
	 * 
	 * String uidstr = ""; String vap_2g = ""; String vap_5g = ""; Device dv =
	 * null;
	 * 
	 * int vap_2g_cnt = 1; int vap_5g_cnt = 1;
	 * 
	 * List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid,
	 * swid);
	 * 
	 * if (devices != null && uid == null) {
	 * 
	 * for (NetworkDevice nd : devices) {
	 * 
	 * if (nd.getTypefs() == null) { continue; } uidstr = nd.getUid();
	 * 
	 * if (nd.getTypefs().equals("ap")) {
	 * 
	 * dv = getDeviceService().findOneByUid(uidstr);
	 * 
	 * if (dv != null) { vap_2g = dv.getVap2GCount(); vap_5g =
	 * dv.getVap5GCount();
	 * 
	 * if (vap_2g != null) { vap_2g_cnt = Integer.parseInt(vap_2g); }
	 * 
	 * if (vap_5g != null) { vap_5g_cnt = Integer.parseInt(vap_5g); } }
	 * 
	 * //LOG.info("vap_2g_cnt " + vap_2g_cnt + "vap_5g_cnt" + vap_5g_cnt );
	 * 
	 * exec_fsql_getpeer (uidstr, vap_2g_cnt, vap_5g_cnt,
	 * dev_array,duration,report); } } } else { exec_fsql_getpeer (uid,
	 * vap_2g_cnt, vap_5g_cnt, dev_array,duration,report); }
	 * 
	 * devlist.put("devicesConnected", dev_array);
	 * 
	 * LOG.info("Device Connected" +devlist.toString());
	 * 
	 * return devlist; }
	 */

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getpeers", method = RequestMethod.GET)
	public JSONObject getpeers(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "15s") String duration,
			@RequestParam(value = "report", required = false) String report) throws IOException {

		JSONArray dev_array = new JSONArray();
		JSONObject devlist = new JSONObject();

		if (swid != null) {
			swid = swid.replaceAll("[^a-zA-Z0-9]", "");
		}

		List<NetworkDevice> networkDevice = null;
		Device dv = null;

		if (report == null || report.isEmpty()) {

			if (uid != null) {
				dv = getDeviceService().findOneByUid(uid);
			} else {
				networkDevice = networkDeviceService.findBy(spid, sid, swid);
			}

			if (dv != null) {

				String peerList = dv.getStatconnectedClientsList();
				if (peerList != null && !peerList.isEmpty()) {
					net.sf.json.JSONArray array = net.sf.json.JSONArray.fromObject(peerList);
					dev_array.addAll(array);
				}
				// LOG.info("getpeers uid processing ");
			} else {

				int android = 0;
				int windows = 0;
				int ios = 0;
				int speaker = 0;
				int printer = 0;
				int others = 0;
				long totCount = 0;
				int _2G = 0;
				long _5G = 0;
				int blocked_Count = 0;

				JSONArray client = new JSONArray();

				for (NetworkDevice nd : networkDevice) {

					String fsType = nd.getTypefs();

					if ("ap".equalsIgnoreCase(fsType)) {

						String clientStr = nd.getConnectedClientsList();

						if (clientStr != null && !clientStr.isEmpty()) {
							net.sf.json.JSONArray array = net.sf.json.JSONArray.fromObject(clientStr);
							client.addAll(array);
						}

						android += nd.getProbeAndroid();
						windows += nd.getProbeWindows();
						ios += nd.getProbeIos();
						speaker += nd.getProbespeaker();
						printer += nd.getProbeprinter();
						others += nd.getProbeOthers();

						_2G += nd.get_2GCount();
						_5G += nd.get_5GCount();

						totCount += nd.assocate2G5GCount;

						String ndUid = nd.getUid();
						List<ClientDevice> clientDevices = clientDeviceService.findByUid(ndUid);

						if (clientDevices != null && clientDevices.size() > 0) {
							blocked_Count += clientDevices.size();
						}

					}
				}
				if (client != null && client.size() > 0) {

					JSONArray _2Gdev_array = new JSONArray();
					JSONArray _5Gdev_array = new JSONArray();
					JSONArray ios_array = new JSONArray();
					JSONArray android_array = new JSONArray();
					JSONArray windows_array = new JSONArray();
					JSONArray speaker_array = new JSONArray();
					JSONArray printer_array = new JSONArray();
					JSONArray others_array = new JSONArray();
					JSONArray total_array = new JSONArray();
					JSONArray Active_array = new JSONArray();

					_2Gdev_array.add(0, "2G");
					_2Gdev_array.add(1, _2G);

					_5Gdev_array.add(0, "5G");
					_5Gdev_array.add(1, _5G);

					total_array.add(0, "Total");
					total_array.add(1, totCount);

					Active_array.add(0, "Active");
					Active_array.add(1, totCount);

					ios_array.add(0, "ios");
					ios_array.add(1, ios);

					android_array.add(0, "android");
					android_array.add(1, android);

					windows_array.add(0, "windows");
					windows_array.add(1, windows);

					speaker_array.add(0, "speaker");
					speaker_array.add(1, speaker);

					printer_array.add(0, "printer");
					printer_array.add(1, printer);

					others_array.add(0, "others");
					others_array.add(1, others);

					dev_array.add(0, client);
					dev_array.add(1, ios_array);
					dev_array.add(2, android_array);
					dev_array.add(3, windows_array);
					dev_array.add(4, speaker_array);
					dev_array.add(5, printer_array);
					dev_array.add(6, others_array);

					dev_array.add(7, total_array);
					dev_array.add(8, _2Gdev_array);
					dev_array.add(9, _5Gdev_array);
					dev_array.add(10, Active_array);

					JSONArray blocked_client = new JSONArray();
					blocked_client.add(0, "Block");
					blocked_client.add(1, blocked_Count);

					dev_array.add(11, blocked_client);
				}
			}

		} else {

			// LOG.info("getpeers report processing ");
			int vap_2g_cnt = 1;
			int vap_5g_cnt = 1;

			dv = getDeviceService().findOneByUid(uid);
			if (dv != null) {

				String vap_2g = dv.getVap2gcount();
				String vap_5g = dv.getVap5gcount();

				if (vap_2g != null) {
					vap_2g_cnt = Integer.parseInt(vap_2g);
				}

				if (vap_5g != null) {
					vap_5g_cnt = Integer.parseInt(vap_5g);
				}

				exec_fsql_getpeer(uid, vap_2g_cnt, vap_5g_cnt, dev_array, duration, report);
			}

		}

		devlist.put("devicesConnected", dev_array);

		// LOG.info("Device Connected" +devlist.toString());

		return devlist;
	}

	@SuppressWarnings("unchecked")
	private boolean exec_fsql_getpeer(String uid, int vap2g, int vap5g, JSONArray dev_array, String duration,
			String report) throws IOException {

		// String fsql_24g = "index=" + indexname + ",sort=timestamp desc,
		// size=1,query=uid:\""+ uid + "\" AND intf_avail:>0 AND
		// radio_type:\"2.4Ghz\"|value(message,snapshot, NA)|table" ;
		// String fsql_5g = "index=" + indexname + ",sort=timestamp desc,
		// size=1,query=uid:\""+ uid + "\" AND intf_avail:>0 AND
		// radio_type:\"5Ghz\"|value(message,snapshot, NA)|table" ;

		String fsql = "";
		String fsql_5g = "";
		int i = 0;

		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		List<Map<String, Object>> qlogs = EMPTY_LIST_MAP;

		vap_map.clear();
		vap5g_map.clear();

		if (duration == null || duration.isEmpty()) {
			duration = "15s";
		}

		for (i = 0; i < vap2g; i++) {

			/*
			 * fsql = "index=" + indexname +
			 * ",sort=timestamp desc, size=1,query=timestamp:>now-"+duration+""
			 * + " AND "; fsql = fsql + "uid:\"" + uid + "\""; fsql = fsql +
			 * " AND vap_id:\"" + i + "\""; fsql = fsql +
			 * " AND radio_type:\"2.4Ghz\"|value(message,snapshot, NA)|table";
			 */

			fsql = "index=" + indexname + ",sort=timestamp desc,";

			if (report == null) {
				fsql = fsql + "size=1,";
			}

			fsql = fsql + "query=timestamp:>now-" + duration + "" + " AND ";
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql = fsql + " AND vap_id:\"" + i + "\"";
			fsql = fsql + " AND radio_type:\"2.4Ghz\"|value(message,snapshot, NA)|table";

			// LOG.info("FSQL PEER QUERY" + fsql);
			logs = fsqlRestController.query(fsql);

			if (logs != EMPTY_LIST_MAP) {
				addPeers(uid, logs, dev_array);
			}
		}

		for (i = 0; i < vap5g; i++) {

			/*
			 * fsql_5g = "index=" + indexname +
			 * ",sort=timestamp desc, size=1,query=timestamp:>now-"+duration+""
			 * + " AND "; fsql_5g = fsql_5g + "uid:\"" + uid + "\""; fsql_5g =
			 * fsql_5g + " AND vap_id:\"" + i + "\""; fsql_5g = fsql_5g +
			 * " AND radio_type:\"5Ghz\"|value(message,snapshot, NA)|table";
			 */

			fsql_5g = "index=" + indexname + ",sort=timestamp desc,";

			if (report == null) {
				fsql_5g = fsql_5g + "size=1,";
			}

			fsql_5g = fsql_5g + "query=timestamp:>now-" + duration + "" + " AND ";
			fsql_5g = fsql_5g + "uid:\"" + uid + "\"";
			fsql_5g = fsql_5g + " AND vap_id:\"" + i + "\"";
			fsql_5g = fsql_5g + " AND radio_type:\"5Ghz\"|value(message,snapshot, NA)|table";

			// LOG.info("FSQL PEER QUERY" + fsql_5g);

			qlogs = fsqlRestController.query(fsql_5g);

			if (qlogs != EMPTY_LIST_MAP) {
				addPeers(uid, qlogs, dev_array);
			}
		}

		if (!dev_array.isEmpty() && dev_array.size() > 0) {

			int blocked_Count = 0;
			List<ClientDevice> client = clientDeviceService.findByUid(uid);

			if (client != null) {
				blocked_Count = client.size();
			}

			JSONArray blocked_client = new JSONArray();
			blocked_client.add(0, "Block");
			blocked_client.add(1, blocked_Count);

			dev_array.add(11, blocked_client);

		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getvaps", method = RequestMethod.GET)
	public JSONObject getvaps(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = new JSONArray();
		JSONObject dev = null;
		JSONObject devlist = new JSONObject();

		int num_intf = 3;
		String vap_2g = "1";
		String vap_5g = "1";
		int count_2g = 0;
		int count_5g = 0;
		List<NetworkDevice> devices = networkDeviceService.findBy(spid, sid, null);

		if (devices != null && uid == null) {
			for (NetworkDevice nd : devices) {

				Device dv = getDeviceService().findOneByUid(nd.getUid());
				if (dv != null) {
					vap_2g = dv.getVap2gcount();
					if (vap_2g == null) {
						vap_2g = "1";
					}
					vap_5g = dv.getVap5gcount();
					if (vap_5g == null) {
						vap_5g = "1";
					}
				}

				count_2g += Integer.parseInt(vap_2g);
				count_5g += Integer.parseInt(vap_5g);
			}
		}

		JSONArray dev_array1 = new JSONArray();
		JSONArray dev_array2 = new JSONArray();

		dev_array1.add(0, "2GVAP");
		dev_array1.add(1, count_2g);
		dev_array2.add(0, "5GVAP");
		dev_array2.add(1, count_5g);

		dev_array.add(0, dev_array1);
		dev_array.add(1, dev_array2);

		devlist.put("ActiveVaps", dev_array);

		// LOG.info("ActiveVaps" +devlist.toString());

		return devlist;

	}

	/*
	 * connectedInterfaces": [ {"device":"WLAN","status":"enabled"},
	 * {"device":"XBEE","status":"disabled"},
	 * {"device":"PLC","status":"enabled"}, {"device":"BLE","status":"disabled"}
	 * ],
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getintf", method = RequestMethod.GET)
	public JSONObject getintf(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = new JSONArray();
		JSONObject dev = null;
		JSONObject devlist = new JSONObject();
		int num_intf = 4;
		String vap_2g = null;
		String vap_5g = null;

		Device device = getDeviceService().findOneByUid(uid);

		if (device != null) {
			vap_2g = device.getVap2gcount();
			vap_5g = device.getVap5gcount();

			// LOG.info("vap_2g>>>" + vap_2g);
			// LOG.info("vap_5g>>>" + vap_5g);
		}

		// LOG.info("vap_2g" + vap_2g);
		// LOG.info("vap_5g" + vap_5g);

		for (int i = 0; i < num_intf; i++) {
			dev = new JSONObject();
			switch (i) {
			case 0:
				dev.put("device", "wlan2g");
				if (vap_2g != null) {
					dev.put("status", "enabled");
					dev.put("vapcount", vap_2g);
				} else {
					dev.put("status", "disabled");
					dev.put("vapcount", "1");
				}
				break;
			case 1:
				dev.put("device", "wlan5g");
				if (vap_5g != null) {
					dev.put("status", "enabled");
					dev.put("vapcount", vap_5g);
				} else {
					dev.put("status", "disabled");
					dev.put("vapcount", "1");
				}
				break;
			case 2:
				dev.put("device", "ble");
				dev.put("status", "disabled");
				dev.put("vapcount", "1");
				break;
			case 3:
				dev.put("device", "xbee");
				dev.put("status", "disabled");
				dev.put("vapcount", "1");
				break;

			default:
				break;
			}

			dev_array.add(dev);
		}
		devlist.put("connectedInterfaces", dev_array);

		// LOG.info("connectedInterfaces" +devlist.toString());

		return devlist;

	}

	/*
	 * "devicesConnected": [ ["2G", 7], ["5G", 12] ],
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getdevcon", method = RequestMethod.GET)
	public JSONObject getdevcon(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();
		dev_array = new JSONArray();

		Device dv = getDeviceService().findOneByUid(uid);

		if (dv != null) {

			JSONArray dev_array1 = new JSONArray();
			JSONArray dev_array2 = new JSONArray();
			JSONArray dev_array3 = new JSONArray();
			JSONArray dev_array4 = new JSONArray();

			dev_array1.add(0, "Mac");
			dev_array1.add(1, dv.getIos());
			dev_array2.add(0, "Android");
			dev_array2.add(1, dv.getAndroid());
			dev_array3.add(0, "Win");
			dev_array3.add(1, dv.getWindows());
			dev_array4.add(0, "Others");
			dev_array4.add(1, dv.getOthers());

			dev_array.add(0, dev_array1);
			dev_array.add(1, dev_array2);
			dev_array.add(2, dev_array3);
			dev_array.add(3, dev_array4);

		}

		devlist.put("devicesConnected", dev_array);

		// LOG.info("Connected Clients" +devlist.toString());

		return devlist;

	}

	/*
	 * "devicesConnected": [ ["2G", 7], ["5G", 12] ],
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getdevtype", method = RequestMethod.GET)
	public JSONObject getdevtype(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();
		dev_array = new JSONArray();

		Device dv = getDeviceService().findOneByUid(uid);

		if (dv != null) {

			JSONArray dev_array1 = new JSONArray();
			JSONArray dev_array2 = new JSONArray();

			dev_array2.add(0, "2G");
			dev_array2.add(1, dv.getPeer2gcount());
			dev_array1.add(0, "5G");
			dev_array1.add(1, dv.getPeer5gcount());

			dev_array.add(0, dev_array2);
			dev_array.add(1, dev_array1);
		}

		devlist.put("typeOfDevices", dev_array);

		// LOG.info("typeOfDevices" +devlist.toString());

		return devlist;
	}

	/*
	 * "devicesConnected": [ ["2G", 7], ["5G", 12] ],
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getstacount", method = RequestMethod.GET)
	public JSONObject getstacount(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();
		dev_array = new JSONArray();

		Device dv = getDeviceService().findOneByUid(uid);

		if (dv != null) {

			JSONArray dev_array1 = new JSONArray();
			JSONArray dev_array2 = new JSONArray();

			dev_array2.add(0, "Active");
			dev_array2.add(1, dv.getPeercount());
			dev_array1.add(0, "Block");
			dev_array1.add(1, blk_count);

			dev_array.add(0, dev_array2);
			dev_array.add(1, dev_array1);
		}

		devlist.put("typeOfDevices", dev_array);

		// LOG.info("typeOfDevices" +devlist.toString());

		return devlist;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "rawtypes" })
	private boolean addPeers(String uid, List<Map<String, Object>> logs, JSONArray dev_array) throws IOException {

		JSONObject dev = null;
		String url = "http://api.macvendors.com/";

		Iterator<Map<String, Object>> iterator = logs.iterator();
		JSONArray dev_array0 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(0);
		JSONArray dev_array1 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(1);
		JSONArray dev_array2 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(2);
		JSONArray dev_array3 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(3);
		// JSONArray router_array = dev_array.isEmpty() ? new JSONArray() :
		// (JSONArray) dev_array.get(4);
		JSONArray speaker_array = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(4);
		JSONArray printer_array = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(5);
		JSONArray dev_array4 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(6);
		JSONArray dev_array5 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(7);
		JSONArray dev_array6 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(8);
		JSONArray dev_array7 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(9);
		JSONArray dev_array8 = dev_array.isEmpty() ? new JSONArray() : (JSONArray) dev_array.get(10);

		int total = 0;
		int android = 0;
		int windows = 0;
		int ios = 0;
		int router = 0;
		int speaker = 0;
		int printer = 0;
		int others = 0;
		int count_2G = 1;
		int count_5G = 1;
		int total_2G = 0;
		int total_5G = 0;

		Map<String, String> hmap = new ConcurrentHashMap<String, String>();
		while (iterator.hasNext()) {

			TreeMap<String, Object> me = new TreeMap<String, Object>(iterator.next());

			String JStr = (String) me.values().toArray()[0];

			JSONObject newJObject = null;
			JSONParser parser = new JSONParser();

			try {
				newJObject = (JSONObject) parser.parse(JStr);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String radio_type = (String) newJObject.get("radio_type");
			String peer_count = (String) newJObject.get("client_count").toString();
			String intf_count = (String) newJObject.get("radio_intf");
			String avail_int = intf_count.trim();
			String sta_cnt = peer_count.trim();

			if_count = Integer.parseInt(avail_int);

			String vap_mac = (String) newJObject.get("vap_mac");
			String vap_ssid = (String) newJObject.get("vap_ssid");
			JSONArray slideContent = (JSONArray) newJObject.get("peer_list");

			// LOG.info("Peer Count " + peer_count);

			if (slideContent == null) {
				return false;
			}
			Iterator i = slideContent.iterator();
			peer_txrxlist.clear();

			while (i.hasNext()) {

				JSONObject slide = (JSONObject) i.next();
				String peer_mac = (String) slide.get("peer_mac");
				String pid = uid.replaceAll("[^a-zA-Z0-9]", "");
				peer_mac = peer_mac.replaceAll("[^a-zA-Z0-9]", "");

				ClientDevice cdev = clientDeviceService.findByPeermac(peer_mac);

				try {
					if (cdev.getUid() != null) {
						if (cdev.getUid().equals(uid)) {
							continue;
						}
					}
				} catch (Exception e) {
				}

				dev = new JSONObject();
				String peer_Tx = (String) slide.get("_peer_tx_bytes").toString();
				String peer_Rx = (String) slide.get("_peer_rx_bytes").toString();
				String p_mac = (String) slide.get("peer_mac");

				if (!hmap.containsKey(p_mac)) {

					dev.put("mac_address", p_mac);
					dev.put("ap", vap_mac);
					dev.put("ip_address", slide.get("ip"));
					dev.put("radio", radio_type);
					dev.put("rssi", slide.get("peer_rssi"));
					dev.put("time", slide.get("peer_conntime"));
					dev.put("ssid", vap_ssid);
					dev.put("tx", peer_Tx);
					dev.put("rx", peer_Rx);

					String probeMac = (String) slide.get("peer_mac");
					ArrayList<Integer> probeDevices = probeCount(probeMac, dev);

					if (probeDevices.size() > 0) {
						ios = ios += probeDevices.get(0);
						android = android += probeDevices.get(1);
						windows = windows += probeDevices.get(2);
						speaker = speaker += probeDevices.get(3);
						printer = printer += probeDevices.get(4);
						others = others += probeDevices.get(5);

					}
					hmap.put(p_mac, radio_type);
					dev_array0.add(dev);
				}

				Map<String, Object> peer_map = new HashMap<String, Object>();

				peer_map.put("uid", uid);
				peer_map.put("peer_mac", p_mac);
				peer_map.put("uplink", peer_Tx);
				peer_map.put("downlink", peer_Rx);

				if (radio_type.equals("2.4Ghz")) {
					total_2G = count_2G++;
					if (vap_map.size() > 0) {
						if (vap_map.containsKey(vap_mac)) {
							int value = (Integer) vap_map.getOrDefault(vap_mac, 0);
							vap_map.put(vap_mac, value + 1);
						} else {
							vap_map.put(vap_mac, 1);
						}
					} else {
						vap_map.put(vap_mac, 1);
					}
					vap_map.put("uid", uid);

				} else {
					total_5G = count_5G++;
					if (vap5g_map.size() > 0) {
						if (vap5g_map.containsKey(vap_mac)) {
							int value = (Integer) vap5g_map.getOrDefault(vap_mac, 0);
							vap5g_map.put(vap_mac, value + 1);
						} else {
							vap5g_map.put(vap_mac, 1);
						}
					} else {
						vap5g_map.put(vap_mac, 1);
					}
					vap5g_map.put("uid", uid);
				}

				if (peer_txrxlist.contains(peer_map)) {
					// nothing to do
				} else {
					peer_txrxlist.add(peer_map);
				}

			}

			int sta_count = 0;
			sta_count = total_2G + total_5G;
			addStat(dev_array1, "Mac", ios);
			addStat(dev_array2, "Android", android);
			addStat(dev_array3, "Win", windows);

			// addStat(router_array, "Router", router);
			addStat(speaker_array, "Speaker", speaker);
			addStat(printer_array, "Printer or Scanner", printer);
			addStat(dev_array4, "Others", others);

			total = ios + android + windows + router + speaker + printer + others;

			addStat(dev_array5, "Total", total);
			addStat(dev_array6, "2G", total_2G);
			addStat(dev_array7, "5G", total_5G);
			addStat(dev_array8, "Active", sta_count);

			dev_array.add(0, dev_array0);

			dev_array.add(1, dev_array1);
			dev_array.add(2, dev_array2);
			dev_array.add(3, dev_array3);
			// dev_array.add(4,router_array);
			dev_array.add(4, speaker_array);
			dev_array.add(5, printer_array);
			dev_array.add(6, dev_array4);

			dev_array.add(7, dev_array5);
			dev_array.add(8, dev_array6);
			dev_array.add(9, dev_array7);
			dev_array.add(10, dev_array8);
		}

		return true;
	}

	public ArrayList<Integer> probeCount(String peer_mac, JSONObject dev) {

		int ios = 0;
		int android = 0;
		int windows = 0;
		int router = 0;
		int speaker = 0;
		int printer = 0;
		int other = 0;

		String inputLine = "Unknown Vendor";
		String probeMac = peer_mac;

		probeMac = probeMac.substring(0, 8).toUpperCase();

		ProbeOUI oui = null;
		oui = probeOUIService.findOneByUid(probeMac);

		if (oui != null) {
			inputLine = oui.getVendorName();
		}
		inputLine = inputLine.toLowerCase();

		String vendorType = inputLine;
		vendorType = vendorType += "...";

		dev.put("devtype", vendorType.toUpperCase());

		ArrayList<Integer> count = new ArrayList<Integer>();

		if (inputLine.contains("apple")) {
			ios++;
			dev.put("client_type", "mac");
		} else if (inputLine.contains("lenovo") || inputLine.contains("asustek") || inputLine.contains("oppo")
				|| inputLine.contains("vivo") || inputLine.contains("lgelectr") || inputLine.contains("sonymobi")
				|| inputLine.contains("motorola") || inputLine.contains("google") || inputLine.contains("xiaomi")
				|| inputLine.contains("oneplus") || inputLine.contains("samsung") || inputLine.contains("htc")
				|| inputLine.contains("gioneeco") || inputLine.contains("zte") || inputLine.contains("huawei")
				|| inputLine.contains("chiunmai")) {
			android++;
			dev.put("client_type", "android");
		} /*
			 * else if (inputLine.contains("cisco") ||
			 * inputLine.contains("ruckus") || inputLine.contains("juniper") ||
			 * inputLine.contains("d-link") || inputLine.contains("tp-link") ||
			 * inputLine.contains("compex") || inputLine.contains("ubiquiti") ||
			 * inputLine.contains("netgear") || inputLine.contains("eero") ||
			 * inputLine.contains("merunetw") || inputLine.contains("plume") ||
			 * inputLine.contains("buffalo") || inputLine.contains("mojo") ||
			 * inputLine.contains("compal") || inputLine.contains("aruba")) {
			 * dev.put("client_type", "router"); router++; }
			 */ else if (inputLine.contains("bose") || inputLine.contains("jbl")) {
			speaker++;
			dev.put("client_type", "speaker");
		} else if (inputLine.contains("canon") || inputLine.contains("roku") || inputLine.contains("nintendo")
				|| inputLine.contains("hp") || inputLine.contains("hewlett")) {
			printer++;
			dev.put("client_type", "printer");
		} else if (inputLine.contains("microsof")) {
			windows++;
			dev.put("client_type", "windows");
		} else {
			other++;
			dev.put("client_type", "laptop");
		}

		count.add(0, ios);
		count.add(1, android);
		count.add(2, windows);
		count.add(3, speaker);
		count.add(4, printer);
		count.add(5, other);

		return count;
	}

	/**
	 * 
	 * Given stat value is set or summed up at the src vector at index 1
	 * 
	 * @param src
	 * @param stat
	 */
	void addStat(JSONArray src, String type, int stat) {
		if (src.isEmpty()) {
			src.add(0, type);
			src.add(1, stat);
		} else {
			stat = (Integer) src.get(1) + stat;
			src.set(1, stat);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/tcpudpconn", method = RequestMethod.GET)
	public JSONObject tcpudpconn(@RequestParam(value = "uid", required = true) String uid) {

		int tcp = 0;
		int udp = 0;

		List<Map<String, Object>> conn = EMPTY_LIST_MAP;

		String fsql = "index=" + indexname + ",sort=timestamp desc,size=1,query=timestamp:>now-" + "1m" + " AND ";

		fsql = fsql + "uid:\"" + uid + "\"";
		fsql = fsql
				+ "AND web_stats:\"Qubercloud Manager\"|value(num_tcp,tcp,NA);value(num_udp,udp,NA);value(timestamp,time,NA)|table";
		conn = fsqlRestController.query(fsql);

		Iterator<Map<String, Object>> iterator = conn.iterator();

		while (iterator.hasNext()) {

			TreeMap<String, Object> me = new TreeMap<String, Object>(iterator.next());

			String JStr = (String) me.values().toArray()[0];

			String tcp_str = (String) me.values().toArray()[0];
			String udp_str = (String) me.values().toArray()[2];
			tcp = Integer.parseInt(tcp_str);
			udp = Integer.parseInt(udp_str);
		}

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();
		dev_array = new JSONArray();
		JSONArray dev_array1 = new JSONArray();
		JSONArray dev_array2 = new JSONArray();
		JSONArray dev_array3 = new JSONArray();
		JSONArray dev_array4 = new JSONArray();
		JSONArray dev_array5 = new JSONArray();

		dev_array1.add(0, "TCP");
		dev_array1.add(1, tcp);
		dev_array2.add(0, "UDP");
		dev_array2.add(1, udp);

		dev_array.add(0, dev_array1);
		dev_array.add(1, dev_array2);

		devlist.put("tcpudpconnections", dev_array);

		// LOG.info("tcpudpconnections" +devlist.toString());

		return devlist;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getblkpeers", method = RequestMethod.GET)
	public JSONObject getblkpeers(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = new JSONArray();
		JSONObject devlist = new JSONObject();
		JSONObject dev = null;
		String uidstr = "";

		blk_count = 0;
		Iterable<ClientDevice> devices = clientDeviceService.findByUid(uid);

		if (devices != null && uid != null) {
			for (ClientDevice nd : devices) {
				dev = new JSONObject();
				dev.put("mac_address", nd.getMac());
				dev.put("ap", nd.getUid());
				dev.put("ip_address", nd.getIp());
				dev.put("radio", nd.getRadio());
				dev.put("rssi", nd.getRssi());
				dev.put("time", nd.getConn());
				dev.put("devtype", nd.getDevname());
				dev.put("ssid", nd.getSsid());
				dev.put("rx", nd.getRx());
				dev.put("tx", nd.getTx());

				uidstr = nd.getTypefs();

				if (uidstr.toLowerCase().contains("apple")) {
					dev.put("client_type", "mac");
				} else if (uidstr.toLowerCase().contains("android")) {
					dev.put("client_type", "android");
				} else if (uidstr.toLowerCase().contains("windows")) {
					dev.put("client_type", "windows");
				} else {
					dev.put("client_type", "laptop");
				}

				dev_array.add(dev);
				blk_count++;

			}
		}

		devlist.put("blockedClients", dev_array);
		// OG.info("JSN.Blocked :" + devlist.toString());

		return devlist;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getacl", method = RequestMethod.GET)
	public JSONObject getacl(@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "swid", required = false) String swid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "duration", required = false, defaultValue = "5m") String duration)
			throws IOException {

		JSONArray dev_array = new JSONArray();
		JSONObject devlist = new JSONObject();
		JSONObject dev = null;
		String uidstr = "";

		Iterable<ClientDevice> devices = null;
		ClientDevice cdev = null;

		if (uid != null) {
			uid = uid.replaceAll("[^a-zA-Z0-9]", "");
			cdev = clientDeviceService.findOneByUid(uid);
		}
		// LOG.info("ACL UID " +uid);

		devices = clientDeviceService.findAll();

		if (devices != null) {
			for (ClientDevice nd : devices) {
				dev = new JSONObject();
				dev.put("mac_address", nd.getMac());
				dev.put("uid", nd.getUid());
				dev.put("devtype", nd.getDevname());
				dev.put("acl", nd.getAcl());
				dev.put("pid", nd.getPid().toUpperCase());
				dev.put("ssid", nd.getSsid());
				uidstr = nd.getTypefs();

				if (uidstr != null) {
					if (uidstr.toLowerCase().contains("apple")) {
						dev.put("client_type", "mac");
					} else if (uidstr.toLowerCase().contains("android")) {
						dev.put("client_type", "android");
					} else if (uidstr.toLowerCase().contains("windows")) {
						dev.put("client_type", "windows");
					} else {
						dev.put("client_type", "laptop");
					}
				} else {
					dev.put("client_type", "laptop");
				}

				dev_array.add(dev);
			}
		}

		devlist.put("aclClients", dev_array);
		// LOG.info("JSN.ACLPeers :" + devlist.toString());

		return devlist;
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET)
	public void report(@RequestParam(value = "cid", required = false) String cid, HttpServletRequest request,
			HttpServletResponse response) {

		if (CustomerUtils.trilateration(cid)) {
			trilaterationReport.pdf(request, response);
		} else {
			LOG.info("=========Entry Exit Solution ==============");
		}
	}

	@RequestMapping(value = "/pdf", method = RequestMethod.GET)
	public String pdf(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		String pdfFileName = "./uploads/qubercloud.pdf";// "c:\\temp\\qubexport.pdf";
		String logoFileName = "./uploads/logo-home.png";// "c:\\temp\\logo-home.png";

		FileOutputStream os = null;
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;

		if (SessionUtil.isAuthorized(request.getSession())) {

			Document document = new Document();
			try {
				@SuppressWarnings("unused")
				PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFileName));
				document.open();
				Paragraph paragraph = new Paragraph();
				Image image2 = Image.getInstance(logoFileName);
				image2.scaleAbsoluteHeight(25f);// scaleAbsolute(50f, 50f);
				image2.scaleAbsoluteWidth(100f);
				paragraph.add(image2);
				paragraph.setAlignment(Element.ALIGN_LEFT);
				paragraph.add("Qubercloud Venue Report Summary");
				paragraph.setAlignment(Element.ALIGN_CENTER);

				addEmptyLine(paragraph, 1);

				// Will create: Report generated by: _name, _date
				paragraph.add(new Paragraph(
						"Report generated by: " + System.getProperty("user.name") + ", " + new Date(), smallBold));
				addEmptyLine(paragraph, 3);
				document.add(paragraph);

				document.newPage();

				addContent(document);

				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());
				fileInputStream = new FileInputStream(pdfFile);
				responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				responseOutputStream.close();
				fileInputStream.close();
				os.close();
			}
			// return pdfFileName;
		}
		return pdfFileName;
	}

	@RequestMapping(value = "/csv", method = RequestMethod.GET)
	public String csv(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {

		String csvFileName = "./uploads/devlog.csv";
		OutputStream out = null;

		if (SessionUtil.isAuthorized(request.getSession())) {
			try {
				Iterable<NetworkDevice> devices = networkDeviceService.findAll();
				List<NetworkDevice> list = new ArrayList<NetworkDevice>();
				ArrayList<Object> Arraylist = new ArrayList<Object>();
				for (NetworkDevice id : devices) {
					list.add(id);
				}
				Collections.sort(list);
				Site site = null;
				if (list != null) {
					for (NetworkDevice nd : list) {
						site = siteService.findById(nd.sid);
						Portion port = portionService.findById(nd.spid);
						if (nd.getTypefs().equals("ap")) {
							Arraylist.add(port.getUid());
							Arraylist.add(nd.svid);
							Arraylist.add(nd.swid);
							Arraylist.add(nd.getUid());
							Arraylist.add(nd.getStatus());
							String uid = nd.getUid();
							JSONObject JStr = getpeers(null, null, null, uid, null, null);
							JSONArray slideContent = (JSONArray) JStr.get("devicesConnected");
							JSONArray devicesConnected = null;
							if (slideContent.get(0) != null) {
								devicesConnected = (JSONArray) slideContent.get(0);
							}
							Iterator i = devicesConnected.iterator();
							while (i.hasNext()) {
								JSONObject slide = (JSONObject) i.next();
								Arraylist.add(slide.get("mac_address"));
								Arraylist.add(slide.get("ap"));
								Arraylist.add(slide.get("devtype"));
								Arraylist.add(slide.get("client_type"));
								Arraylist.add(slide.get("time"));
							}

						}
					}
				}

				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=" + csvFileName);
				out = response.getOutputStream();
				out.write(Arraylist.toString().getBytes());
				// return csvFileName;

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				out.flush();
				out.close();
			}
		}

		return csvFileName;
	}

	@RequestMapping(value = "/export", method = RequestMethod.GET)
	public String export(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException {
		String pdfFileName = "./uploads/qubexport.pdf";
		String logoFileName = "./uploads/logo-home.png";

		OutputStream responseOutputStream = null;
		FileInputStream fileInputStream = null;
		FileOutputStream os = null;

		if (SessionUtil.isAuthorized(request.getSession())) {

			Document document = new Document();
			try {
				os = new FileOutputStream(pdfFileName);
				@SuppressWarnings("unused")
				PdfWriter pdfWriter = PdfWriter.getInstance(document, os);
				document.open();
				Paragraph paragraph = new Paragraph();
				Image image2 = Image.getInstance(logoFileName);
				image2.scaleAbsoluteHeight(25f);// scaleAbsolute(50f, 50f);
				image2.scaleAbsoluteWidth(100f);
				paragraph.add(image2);
				paragraph.setAlignment(Element.ALIGN_LEFT);
				paragraph.add("Qubercloud Log Summary");
				paragraph.setAlignment(Element.ALIGN_CENTER);

				addEmptyLine(paragraph, 1);

				// Will create: Report generated by: _name, _date
				paragraph.add(new Paragraph(
						"Report generated by: " + System.getProperty("user.name") + ", " + new Date(), smallBold));
				addEmptyLine(paragraph, 3);
				document.add(paragraph);

				document.newPage();

				addlogContent(document);

				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());
				fileInputStream = new FileInputStream(pdfFile);
				responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				responseOutputStream.close();
				fileInputStream.close();
				os.close();
			}
			// return pdfFileName;
		}
		return pdfFileName;
	}

	private void addContent(Document document) throws DocumentException, IOException, ParseException {
		Anchor anchor = new Anchor("Qubercloud Venue Summary", catFont);
		anchor.setName("Qubercloud Venue Summary");

		// Second parameter is the number of the chapter
		Chapter catPart = new Chapter(new Paragraph(anchor), 2);

		Paragraph subPara = new Paragraph("Venue Details", subFont);
		addEmptyLine(subPara, 2);

		Section subCatPart = catPart.addSection(subPara);

		// add a table
		createTable(subCatPart, document);

		// now add all this to the document
		document.add(catPart);

	}

	private void addlogContent(Document document) throws DocumentException, IOException, ParseException {
		Anchor anchor = new Anchor("Qubercloud LOG Summary", catFont);
		anchor.setName("Qubercloud LOG Summary");

		// Second parameter is the number of the chapter
		Chapter catPart = new Chapter(new Paragraph(anchor), 1);

		Paragraph subPara = new Paragraph("Qubercloud LOGS", subFont);
		addEmptyLine(subPara, 1);

		Section subCatPart = catPart.addSection(subPara);

		// add a table
		createlogTable(subCatPart, document);

		// now add all this to the document
		document.add(catPart);

	}

	private void createlogTable(Section subCatPart, Document document)
			throws IOException, ParseException, DocumentException {
		PdfPTable table = new PdfPTable(3);

		PdfPCell c1 = new PdfPCell(new Phrase("UID"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("LEVEL"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("LOG"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		table.setHeaderRows(1);

		JSONObject newJObject = null;
		JSONParser parser = new JSONParser();

		List<Map<String, Object>> logs = activity(null, null, null, null, null);

		if (logs != null) {

			Iterator<Map<String, Object>> iterator = logs.iterator();

			while (iterator.hasNext()) {

				TreeMap<String, Object> me = new TreeMap<String, Object>(iterator.next());

				String JStr = (String) me.values().toArray()[0];

				try {
					newJObject = (JSONObject) parser.parse(JStr);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONArray slideContent = (JSONArray) newJObject.get("log_list");

				if (slideContent != null) {
					Iterator i = slideContent.iterator();
					while (i.hasNext()) {

						String uid = (String) newJObject.get("uid");
						table.addCell(uid);

						JSONObject slide = (JSONObject) i.next();
						String debug_level = (String) slide.get("debug_level");
						table.addCell(debug_level);

						String debug_log = (String) slide.get("debug_log");
						table.addCell(debug_log);
					}
				}

			}
		}

		subCatPart.add(table);
	}

	private void createTable(Section subCatPart, Document document)
			throws IOException, ParseException, DocumentException {
		PdfPTable table = new PdfPTable(5);
		PdfPTable ap_table = new PdfPTable(5);

		PdfPCell c1 = new PdfPCell(new Phrase("Venue#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Server#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Switch#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("AP#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Status#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		table.setHeaderRows(1);

		c1 = new PdfPCell(new Phrase("Client"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		ap_table.addCell(c1);

		c1 = new PdfPCell(new Phrase("VAP"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		ap_table.addCell(c1);

		c1 = new PdfPCell(new Phrase("OEM"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		ap_table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Device"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		ap_table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Venue#"));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		ap_table.addCell(c1);

		ap_table.setHeaderRows(1);

		Iterable<NetworkDevice> devices = networkDeviceService.findAll();
		List<NetworkDevice> list = new ArrayList<NetworkDevice>();

		for (NetworkDevice id : devices) {
			list.add(id);
		}
		Collections.sort(list);

		Site site = null;

		if (list != null) {

			for (NetworkDevice nd : list) {
				site = siteService.findById(nd.sid);

				Portion port = portionService.findById(nd.spid);

				if (nd.getTypefs().equals("ap")) {

					if (port != null) {
						table.addCell(port.getUid());
					} else {
						continue;
					}

					table.addCell(nd.svid);
					table.addCell(nd.swid);
					table.addCell(nd.getUid());
					table.addCell(nd.getStatus());

					String uid = nd.getUid();
					JSONObject JStr = getpeers(null, null, null, uid, null, null);

					JSONArray slideContent = (JSONArray) JStr.get("devicesConnected");
					JSONArray devicesConnected = null;

					if (!slideContent.isEmpty()) {
						if (slideContent.get(0) != null) {
							devicesConnected = (JSONArray) slideContent.get(0);
						}
					}

					if (devicesConnected != null) {
						Iterator i = devicesConnected.iterator();

						while (i.hasNext()) {

							JSONObject slide = (JSONObject) i.next();
							String mac_address = (String) slide.get("mac_address");
							String ip_address = (String) slide.get("ap");
							String dev_type = (String) slide.get("devtype");
							String client_type = (String) slide.get("client_type");
							// String ttl = (String) slide.get("time");
							ap_table.addCell(mac_address);
							ap_table.addCell(ip_address);
							ap_table.addCell(dev_type);
							ap_table.addCell(client_type);
							ap_table.addCell(port.getUid());

						}
					}

				}
			}
		}

		Paragraph Para;
		String venue = "Venue ";
		if (site != null) {
			venue = venue + site.getUid();
			Para = new Paragraph(venue);
		} else {
			Para = new Paragraph("Venue Report");
		}

		subCatPart.add(Para);
		addEmptyLine(Para, 2);

		subCatPart.add(table);

		document.newPage();

		Anchor anchor = new Anchor("Qubercloud Client Summary", catFont);
		anchor.setName("Qubercloud Connected Device(s) Summary");

		// Second parameter is the number of the chapter
		Chapter catPart = new Chapter(new Paragraph(anchor), 1);

		Paragraph subPara = new Paragraph("Client Details", subFont);
		addEmptyLine(subPara, 1);

		Section subCatPart1 = catPart.addSection(subPara);

		Paragraph ap_Para = new Paragraph("Client Report Summary");
		subCatPart1.add(ap_Para);
		addEmptyLine(ap_Para, 1);
		subCatPart1.add(ap_table);
		document.add(catPart);
	}

	@RequestMapping(value = "/imgcapture", method = RequestMethod.GET)
	public String imgcapture(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String imgFileName = "./uploads/screenshot.jpg";

		OutputStream responseOutputStream = null;
		FileInputStream fileInputStream = null;

		if (SessionUtil.isAuthorized(request.getSession())) {
			try {

				System.setProperty("java.awt.headless", "false");
				Toolkit tool = Toolkit.getDefaultToolkit();
				Dimension d = tool.getScreenSize();
				Rectangle rect = new Rectangle(d);
				Robot robot = new Robot();
				File f = new File(imgFileName);
				BufferedImage img = robot.createScreenCapture(rect);

				ImageIO.write(img, "jpeg", f);

				File jpegFile = new File(imgFileName);
				response.setContentType("application/jpeg");
				response.setHeader("Content-Disposition", "attachment; filename=" + imgFileName);
				response.setContentLength((int) jpegFile.length());

				fileInputStream = new FileInputStream(jpegFile);
				responseOutputStream = response.getOutputStream();
				int bytes;

				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				responseOutputStream.close();
				fileInputStream.close();
			}
			// return imgFileName;
		}

		return imgFileName;
	}

	@RequestMapping(value = "/gw_alert", method = RequestMethod.GET)
	public JSONObject alert(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "pdfgen", required = false) Boolean pdfgen) throws IOException {

		JSONObject devlist = new JSONObject();
		JSONArray dev_array = new JSONArray();

		try {

			JSONObject dev = null;
			Iterable<Device> device = null;
			String state = "inactive";
			device = getDeviceService().findByCidAndState(cid, state);

			Customer cx = customerService.findById(cid);
			TimeZone totimezone = CustomerUtils.FetchTimeZone(cx.getTimezone());
			format.setTimeZone(totimezone);

			if (device != null) {
				for (Device d : device) {

					String spid = d.getSpid() == null ? "-" : d.getSpid();
					Portion portion = portionService.findById(spid);
					String portionName = portion == null ? "NA" : portion.getUid();
					String lastSeen = d.getLastseen() == null ? "NA" : d.getLastseen();

					String fileStatus = d.getDevCrashDumpUploadStatus() == null ? "NA"
							: d.getDevCrashDumpUploadStatus();
					String fileName = d.getDevCrashdumpFileName() == null ? "NA" : d.getDevCrashdumpFileName();
					String uid = d.getUid();

					String crashState = "enabled";
					if (fileStatus.equals("NA") || fileStatus.isEmpty() || !fileStatus.equals("0")) {
						crashState = "disabled";
					}

					dev = new JSONObject();

					dev.put("macaddr", uid);
					dev.put("state", d.getState().toUpperCase());
					dev.put("alias", d.getAlias().toUpperCase());
					dev.put("portionname", portionName.toUpperCase());
					dev.put("timestamp", lastSeen);
					dev.put("filestatus", fileStatus);
					dev.put("fileName", fileName);
					dev.put("crashState", crashState);
					dev_array.add(dev);
				}

				if (dev_array == null || dev_array.size() == 0) {
					if (pdfgen != null && pdfgen) {
						return null;
					}
					dev_array = defaultDatas(dev_array);
				}

				devlist.put("inactive_list", dev_array);
				// LOG.info("inactive---------"+dev_array.size());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return devlist;
	}

	public JSONArray defaultDatas(JSONArray dev_array) {
		JSONObject dev = new JSONObject();
		dev.put("macaddr", "-");
		dev.put("floorname", "NA");
		dev.put("alias", "NA");
		dev.put("portionname", "NA");
		dev.put("sitename", "NA");
		dev.put("state", "-");
		dev.put("status", "Unknown");
		dev.put("timestamp", "NA");
		dev_array.add(dev);
		return dev_array;
	}

	@RequestMapping(value = "/GW_Device_crash_info", method = RequestMethod.GET)
	public JSONArray getDevice_crash_info(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "time", required = true) String time) {
		try {

			final String opcode = "device_crash_info";
			final String type = "device_crash";

			List<Map<String, Object>> logs = EMPTY_LIST_MAP;

			if (time == null || time.isEmpty()) {
				time = "10d";
			}
			String size = "500";

			String fsql = "index=" + device_history_event + ",size=" + size + ",type=" + type + ",query=timestamp:>now-"
					+ time + " AND opcode:" + opcode + " AND ";

			List<Device> devices = getDeviceService().findByCid(cid);
			if (devices != null) {
				String uidbuilder = buildDeviceArrayCondition(devices, "uid");
				fsql = fsql + uidbuilder;
			}
			fsql += " |value(uid,uid, NA);" + " value(crash_timestamp,crash_timestamp,NA);"
					+ " value(cid,cid,NA);value(daemon_info,daemon_info,NA);"
					+ " value(version,version,NA);value(filename,filename,NA);value(upload_state,upload_state,NA);"
					+ " value(timestamp,time,NA);|table";

			logs = fsqlRestController.query(fsql);

			JSONObject object = null;
			JSONArray array = new JSONArray();

			Device device = null;

			if (logs != null) {
				Iterator<Map<String, Object>> iterator = logs.iterator();
				while (iterator.hasNext()) {
					Map<String, Object> map = iterator.next();

					object = new JSONObject();

					String devUid = (String) map.get("uid");
					int crashTime = (int) map.get("crash_timestamp");
					String filename = (String) map.getOrDefault("filename", "NA");
					String upload_status = (String) map.get("upload_state");

					String alias = "NA";

					device = getDeviceService().findOneByUid(devUid);
					if (device != null && device.getUid().equalsIgnoreCase(devUid)) {
						alias = device.getAlias();
					}

					object.put("uid", devUid);
					object.put("crashTime", crashTime);
					object.put("filename", filename);
					object.put("alias", alias);
					object.put("status_code", upload_status);
					array.add(object);

				}

			}
			return array;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private DeviceService getDeviceService() {
		if (devService == null) {
			devService = Application.context.getBean(DeviceService.class);
		}
		return devService;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/steerStatsDashboard", method = RequestMethod.GET)
	public JSONObject steer_sta_stats(@RequestParam(value = "cid", required = true) String cid,
			@RequestParam(value = "sid", required = true) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid,
			@RequestParam(value = "clientMac", required = false) String clientMac, HttpServletRequest req,
			HttpServletResponse res) throws IOException {

		JSONObject jsonObject = new JSONObject();
		HashMap<String, String> stationMap = new HashMap<String, String>();

		if (SessionUtil.isAuthorized(req.getSession())) {

			try {

				List<Device> devices = null;

				if (uid != null) {
					devices = getDeviceService().findByUid(uid);
				} else if (spid != null) {
					devices = getDeviceService().findBySpid(spid);
				} else if (sid != null) {
					devices = getDeviceService().findBySid(sid);
				} else {
					devices = getDeviceService().findByCid(cid);
				}

				int deployedAp = 0;
				int peerCount = 0;

				double badRSSI = 0;
				double txFailure = 0;
				double peerRetry = 0;

				double totLBFailure = 0;
				double totBBFailure = 0;

				double totLBSuccess = 0;
				double totBBSuccess = 0;

				double totLBAttempts = 0;
				double totBBAttempts = 0;

				double totBBFailurePercent = 0;
				double totLBFailurePercent = 0;

				double totBBSuccessPercent = 0;
				double totLBSuccessPercent = 0;

				int badClients = 0;
				int goodClients = 0;
				int LBClients = 0;
				int BBClients = 0;

				int _2G = 0;
				int _5G = 0;

				DecimalFormat decimalFormat = new DecimalFormat("#.##");

				JSONObject loadsteerjson = null;
				JSONObject bandsteerjson = null;
				JSONObject rootjson = new JSONObject();
				JSONObject detailsjson = null;

				JSONArray detailsArray = null;
				JSONArray rootarray = new JSONArray();

				if (devices != null) {

					deployedAp = devices.size();

					for (Device dev : devices) {

						peerCount += dev.getPeercount();
						_2G += dev.getPeer2gcount();
						_5G += dev.getPeer5gcount();

						int lbAttempts = 0;
						int lbFailure = 0;
						int lbSuccess = 0;

						int bbAttempts = 0;
						int bbFailure = 0;
						int bbSuccess = 0;

						double dev_TotBBFailurePercent = 0;
						double dev_TotLBFailurePercent = 0;

						double dev_TotBBSuccessPercent = 0;
						double dev_TotLBSuccessPercent = 0;

						String devUid = dev.getUid().toUpperCase();

						JSONObject netGearSts = dev.getSteerClientList();
						String radioDetails = dev.getPeerList();
						String alias = dev.getAlias().toUpperCase();

						if (radioDetails != null && !radioDetails.isEmpty()) {

							net.sf.json.JSONArray stsDetails = net.sf.json.JSONArray.fromObject(radioDetails);
							Iterator<net.sf.json.JSONObject> peerList = stsDetails.iterator();

							while (peerList.hasNext()) {

								net.sf.json.JSONObject peerObject = peerList.next();

								String rssi = (String) peerObject.getOrDefault("rssi", "0");
								String peer_retry = (String) peerObject.getOrDefault("peer_retry", "0");
								String peer_tx_fail = (String) peerObject.getOrDefault("peer_tx_fail", "0");

								boolean badclient = false;

								int intRssi = Math.abs(Integer.parseInt(rssi));
								int intpeer_retry = Math.abs(Integer.parseInt(peer_retry));
								int intpeer_tx_fail = Math.abs(Integer.parseInt(peer_tx_fail));

								if (intRssi > 80 || intRssi <= 20) {
									badclient = true;
									badRSSI++;
								}
								if (intpeer_retry > 15) {
									badclient = true;
									peerRetry++;
								}
								if (intpeer_tx_fail > 15) {
									badclient = true;
									txFailure++;
								}

								if (badclient) {
									badClients++;
								} else {
									goodClients++;
								}
							}
						}

						if (netGearSts != null) {

							if (netGearSts.containsKey("bb")) {
								HashMap<String, Object> bb = (HashMap<String, Object>) netGearSts.get("bb");
								if (bb != null && !bb.isEmpty()) {
									bbSuccess = (int) bb.get("sucess");
									bbAttempts = (int) bb.get("attempts");
									bbFailure = (int) bb.get("failure");
									BBClients++;
								}
							}

							if (netGearSts.containsKey("lb")) {
								HashMap<String, Object> lb = (HashMap<String, Object>) netGearSts.get("lb");
								if (lb != null && !lb.isEmpty()) {
									lbSuccess = (int) lb.get("sucess");
									lbAttempts = (int) lb.get("attempts");
									lbFailure = (int) lb.get("failure");
									LBClients++;
								}
							}
						}
						totBBSuccess += bbSuccess;
						totLBSuccess += lbSuccess;

						totBBFailure += bbFailure;
						totLBFailure += lbFailure;

						totBBAttempts += bbAttempts;
						totLBAttempts += lbAttempts;

						if (!stationMap.containsKey(devUid)) {

							if (bbAttempts != 0) {
								dev_TotBBFailurePercent = (bbFailure * 100.0) / bbAttempts;
								dev_TotBBSuccessPercent = (bbSuccess * 100.0) / bbAttempts;
							}

							if (lbAttempts != 0) {
								dev_TotLBFailurePercent = (lbFailure * 100.0) / lbAttempts;
								dev_TotLBSuccessPercent = (lbSuccess * 100.0) / lbAttempts;
							}

							int statClient = dev.getPeercount();
							if (devUid.equalsIgnoreCase("40:A5:EF:78:D3:20")) {
								statClient = 1;
							}
							loadsteerjson = new JSONObject();
							loadsteerjson.put("name", "Load Balance");
							loadsteerjson.put("load_sucess", decimalFormat.format(dev_TotLBSuccessPercent));
							loadsteerjson.put("load_failure", decimalFormat.format(dev_TotLBFailurePercent));
							loadsteerjson.put("band_sucess", decimalFormat.format(dev_TotBBSuccessPercent));
							loadsteerjson.put("band_failure", decimalFormat.format(dev_TotBBFailurePercent));
							loadsteerjson.put("uid", devUid);
							loadsteerjson.put("alias", alias);
							loadsteerjson.put("stationCount", statClient);
							loadsteerjson.put("size", 10);

							bandsteerjson = new JSONObject();
							bandsteerjson.put("name", "Band Balance");
							bandsteerjson.put("load_sucess", decimalFormat.format(dev_TotLBSuccessPercent));
							bandsteerjson.put("load_failure", decimalFormat.format(dev_TotLBFailurePercent));
							bandsteerjson.put("band_sucess", decimalFormat.format(dev_TotBBSuccessPercent));
							bandsteerjson.put("band_failure", decimalFormat.format(dev_TotBBFailurePercent));
							bandsteerjson.put("uid", devUid);
							bandsteerjson.put("alias", alias);
							bandsteerjson.put("stationCount", statClient);
							bandsteerjson.put("size", 10);

							detailsArray = new JSONArray();
							detailsArray.add(loadsteerjson);
							detailsArray.add(bandsteerjson);

							detailsjson = new JSONObject();

							detailsjson.put("children", detailsArray);
							detailsjson.put("alias", alias);
							detailsjson.put("name", alias);
							detailsjson.put("uid", devUid);
							detailsjson.put("stationCount", dev.getPeercount());

							detailsjson.put("load_sucess", decimalFormat.format(dev_TotLBSuccessPercent));
							detailsjson.put("load_failure", decimalFormat.format(dev_TotLBFailurePercent));
							detailsjson.put("band_sucess", decimalFormat.format(dev_TotBBSuccessPercent));
							detailsjson.put("band_failure", decimalFormat.format(dev_TotBBFailurePercent));

							rootarray.add(detailsjson);
						} else {
							stationMap.put("uid", devUid);
							LOG.info("Duplicate UID");
						}
					}
					if (peerCount != 0) {
						badRSSI = (badRSSI * 100) / peerCount;
						txFailure = (txFailure * 100) / peerCount;
						peerRetry = (peerRetry * 100) / peerCount;
					}

					if (totBBAttempts != 0) {
						totBBFailurePercent = (totBBFailure * 100.0) / totBBAttempts;
						totBBSuccessPercent = (totBBSuccess * 100.0) / totBBAttempts;
					}

					if (totLBAttempts != 0) {
						totLBFailurePercent = (totLBFailure * 100.0) / totLBAttempts;
						totLBSuccessPercent = (totLBSuccess * 100.0) / totLBAttempts;
					}

				}

				jsonObject.put("twoG", _2G);
				jsonObject.put("fiveG", _5G);

				jsonObject.put("LBClients", LBClients);
				jsonObject.put("BBClients", BBClients);

				jsonObject.put("goodClient", goodClients);
				jsonObject.put("badClient", badClients);

				jsonObject.put("deployedAp", deployedAp);
				jsonObject.put("peerCount", peerCount);

				jsonObject.put("badRSSI", decimalFormat.format(badRSSI));
				jsonObject.put("txFailure", decimalFormat.format(txFailure));
				jsonObject.put("peerRetry", decimalFormat.format(peerRetry));

				jsonObject.put("totBBFailurePercent", decimalFormat.format(totBBFailurePercent));
				jsonObject.put("totLBFailurePercent", decimalFormat.format(totLBFailurePercent));
				jsonObject.put("totBBSuccessPercent", decimalFormat.format(totBBSuccessPercent));
				jsonObject.put("totLBSuccessPercent", decimalFormat.format(totLBSuccessPercent));

				rootjson.put("children", rootarray);
				jsonObject.put("stationDetails", rootjson);

				// LOG.info("jsonObject " +jsonObject);

			} catch (Exception e) {
				e.printStackTrace();
				LOG.info("While steerStatsDashboard processing error ", e);
			}
		}

		return jsonObject;
	}

	public JSONObject getBasicClientDetails(String uid, int vap2G, int vap5G, String duration) {

		List<Map<String, Object>> twoG_Details = null;
		List<Map<String, Object>> fiveG_Details = null;

		String query = null;

		JSONObject basicDetails = new JSONObject();

		for (int i = 0; i < vap2G; i++) {
			query = "index=" + indexname + ",sort=timestamp desc,size=1,query=timestamp:>now-" + duration + "" + " AND "
					+ "uid:\"" + uid + "\" AND vap_id:\"" + i
					+ "\" AND radio_type:\"2.4Ghz\"|value(message,message, NA)|table,sort=Date:desc;";

			twoG_Details = fsqlRestController.query(query);
			basicDetails = getRadioValues(twoG_Details, basicDetails);
		}

		for (int i = 0; i < vap5G; i++) {
			query = "index=" + indexname + ",sort=timestamp desc,size=1,query=timestamp:>now-" + duration + "" + " AND "
					+ "uid:\"" + uid + "\" AND vap_id:\"" + i
					+ "\" AND radio_type:\"5Ghz\"|value(message,message, NA)|table,sort=Date:desc;";

			fiveG_Details = fsqlRestController.query(query);
			basicDetails = getRadioValues(fiveG_Details, basicDetails);
		}

		return basicDetails;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getRadioValues(List<Map<String, Object>> details, JSONObject basicDetails) {

		JSONObject message = null;
		JSONArray peerList = null;
		Map<String, Object> info = null;

		String mac;
		String tx;
		String rx;
		String rssi;
		String jsonStr;
		String radiotype;
		String ssid;
		String ap;
		String time;

		boolean _11k;
		boolean _11v;
		boolean _11r;

		String peer_retry;
		String peer_tx_fail;

		Iterator<JSONObject> iter = null;
		JSONObject peer = null;
		JSONObject values = null;

		JSONParser jsonparser = new JSONParser();

		if (details != null && details.size() > 0) {

			try {

				info = details.get(0);
				jsonStr = (String) info.get("message");

				message = (JSONObject) jsonparser.parse(jsonStr);
				radiotype = (String) message.get("radio_type");
				ssid = (String) message.get("vap_ssid");
				ap = (String) message.get("vap_mac");
				peerList = (JSONArray) message.get("peer_list");

				if (peerList != null && peerList.size() > 0) {

					iter = peerList.iterator();

					while (iter.hasNext()) {

						peer = iter.next();
						mac = (String) peer.get("peer_mac");

						tx = peer.get("_peer_tx_bytes").toString();
						rx = peer.get("_peer_rx_bytes").toString();
						rssi = peer.get("peer_rssi").toString();

						time = peer.get("peer_conntime").toString();
						peer_retry = peer.get("peer_retry").toString();
						peer_tx_fail = peer.get("peer_tx_fail").toString();

						_11k = (boolean) peer.getOrDefault("11k", false);
						_11v = (boolean) peer.getOrDefault("11v", false);
						_11r = (boolean) peer.getOrDefault("11r", false);

						values = new JSONObject();

						values.put("mac_address", mac);
						values.put("ap", ap);
						values.put("radio", radiotype);
						values.put("rssi", rssi);
						values.put("time", time);
						values.put("ssid", ssid);
						values.put("tx", tx);
						values.put("rx", rx);
						values.put("k11", _11k);
						values.put("v11", _11v);
						values.put("r11", _11r);
						values.put("peer_retry", peer_retry);
						values.put("peer_tx_fail", peer_tx_fail);

						// FOR HEATMAP SOLUTION

						values.put("associated", "Yes");
						values.put("channel", "NA");
						values.put("signal", "NA");

						probeCount(mac, values);

						basicDetails.put(mac, values);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return basicDetails;
	}

	@SuppressWarnings("unchecked")
	public org.json.simple.JSONObject getSteerDetails(String uid) {

		JSONObject details = new JSONObject();

		try {

			final String lb = "loadbalance";
			final String bb = "bandbalance";

			String duration = "12h";

			JSONObject LBSteerDetails = getLBDetails(uid, lb, duration);
			JSONObject BBSteerDetails = getLBDetails(uid, bb, duration);

			details.put("lb", LBSteerDetails);
			details.put("bb", BBSteerDetails);

			return details;

		} catch (Exception e) {
			LOG.error("while LB or BB Processing errors " + e);
			e.printStackTrace();
		}
		return details;
	}

	private JSONObject getLBDetails(String uid, String steer_type, String duration) {

		JSONObject metrics = new JSONObject();

		int attempts = 0;
		int sucess = 0;
		int failure = 0;

		try {

			String fsql = "index=" + device_history_event + ",type=steering-details,size=1,sort=timestamp desc, "
					+ " query=timestamp:>now-" + duration + " AND opcode:" + steer_type + " " + " AND uid:\"" + uid
					+ "\"|value(uid,uid,NA); " + " value(steer_sta_list,steer_sta_list,NA);value(metrics,metrics,NA);"
					+ " value(timestamp,timestamp,NA)|table,sort=Date:desc;";

			List<Map<String, Object>> steer_stats = fsqlRestController.query(fsql);

			if (steer_stats != null && steer_stats.size() > 0) {

				Iterator<Map<String, Object>> map = steer_stats.iterator();

				/*
				 * LOG.info("uid  " + uid); LOG.info("steer_stats  " +
				 * steer_stats);
				 */

				while (map.hasNext()) {

					HashMap<String, Object> map1 = (HashMap<String, Object>) map.next();

					if (map1.containsKey("metrics")) {
						HashMap<String, Object> map2 = (HashMap<String, Object>) map1.get("metrics");
						attempts += (int) map2.get("attempts");
						sucess += (int) map2.get("sucess");
						failure += (int) map2.get("failure");
					}
				}

				metrics.put("attempts", attempts);
				metrics.put("sucess", sucess);
				metrics.put("failure", failure);

				return metrics;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/netGear")
	public boolean netGear(@RequestBody Map<String, Object> map) {

		Thread t = new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {

				try {

					if (map == null || map.isEmpty()) {
						return;
					}

					String uid = (String) map.get("uid");
					String steer_type = (String) map.get("opcode");

					// LOG.info(" UID: " +uid + " OPCODE:" +steer_type );

					Device device = getDeviceService().findOneByUid(uid);

					if (device == null) {
						LOG.info("Device Not Found " + uid);
						return;
					}

					String cid = device.getCid();
					boolean enablelog = false;

					Customer cx = customerService.findById(cid);
					if (cx.getLogs() != null && cx.getLogs().equals("true")) {
						enablelog = true;
					}

					int sucess = 0;
					int failure = 0;
					int attempts = 0;

					if (map.containsKey("steer_sta_list")) {

						List<HashMap<String, Object>> steer_stat_list = (List<HashMap<String, Object>>) map
								.get("steer_sta_list");
						Iterator<HashMap<String, Object>> it = steer_stat_list.iterator();

						while (it.hasNext()) {

							HashMap<String, Object> steerMap = it.next();

							if (steerMap.containsKey("steer_bss_list")) {

								int attempt = (int) steerMap.get("attempts");
								attempts += attempt;

								List<HashMap<String, Object>> steer_bss_list = (List<HashMap<String, Object>>) steerMap
										.get("steer_bss_list");
								Iterator<HashMap<String, Object>> bss_list_it = steer_bss_list.iterator();

								while (bss_list_it.hasNext()) {
									HashMap<String, Object> map = bss_list_it.next();
									if (map.containsKey("success")) {
										int sucs = (int) map.get("success");
										if (sucs <= 0) {
											failure++;
										} else {
											sucess++;
										}
									}
								}
							}
						}

						int peerCount = device.getPeercount();

						JSONObject metrics = new JSONObject();
						metrics.put("attempts", attempts);
						metrics.put("sucess", sucess);
						metrics.put("failure", failure);
						metrics.put("peercount", peerCount);

						final String type = "steering-details";
						String opcode = "bandbalance";

						if (steer_type.equals("loadbalance")) {
							opcode = "loadbalance";
						}

						HashMap<String, Object> jsonMap = new HashMap<String, Object>();
						jsonMap.put("opcode", opcode);
						jsonMap.put("uid", uid);
						jsonMap.put("cid", cid);

						if (device.getSid() != null) {
							jsonMap.put("sid", device.getSid());
						}
						if (device.getSpid() != null) {
							jsonMap.put("spid", device.getSpid());
						}

						jsonMap.put("metrics", metrics);
						jsonMap.put("steer_sta_list", steer_stat_list);

						CustomerUtils.logs(enablelog, this.getClass().getName(),
								"=========" + "NETGER====== " + opcode + "=======Message POST==========" + jsonMap);

						elasticService.post(device_history_event, type, jsonMap);

					}

				} catch (Exception e) {
					LOG.error(" While NetGear Details Processing error " + e);
				}
			}
		});

		t.start();

		return true;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deviceCounts", method = RequestMethod.GET)
	public JSONObject activeClientsCount(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid,
			@RequestParam(value = "uid", required = false) String uid) throws IOException {

		JSONArray dev_array = null;
		JSONObject devlist = new JSONObject();
		dev_array = new JSONArray();

		List<NetworkDevice> devices = null;

		if (uid != null) {
			String uuid = uid.replaceAll("[^a-zA-Z0-9]", "");
			devices = networkDeviceService.findByUuid(uuid);
		} else if (spid != null) {
			devices = networkDeviceService.findBySpid(spid);
		} else if (sid != null) {
			devices = networkDeviceService.findBySid(sid);
		} else {
			devices = networkDeviceService.findByCid(cid);
		}

		int deployedDevices = 0;
		int _2G = 0;
		int _5G = 0;
		int total = 0;

		if (devices != null) {
			for (NetworkDevice device : devices) {
				if (device.getTypefs() != null && device.getTypefs().equalsIgnoreCase("ap")) {

					_2G += device.get_2GCount();
					_5G += device.get_5GCount();

					deployedDevices++;

				} else if (device.getBleType() != null) {

					String bleType = device.getBleType();

					if (bleType.equalsIgnoreCase("scanner") || bleType.equalsIgnoreCase("server")
							|| bleType.equalsIgnoreCase("reciver"))

						deployedDevices++;
				}
			}
		}

		JSONArray _5G_array = new JSONArray();
		JSONArray _2G_array = new JSONArray();
		JSONArray tot_array = new JSONArray();
		JSONArray devCount_array = new JSONArray();

		_2G_array.add(0, "2G");
		_2G_array.add(1, _2G);

		_5G_array.add(0, "5G");
		_5G_array.add(1, _5G);

		total = _2G + _5G;

		tot_array.add(0, "Total");
		tot_array.add(1, total);

		devCount_array.add(0, "DeployedDevices");
		devCount_array.add(1, deployedDevices);

		dev_array.add(0, _2G_array);
		dev_array.add(1, _5G_array);
		dev_array.add(2, tot_array);
		dev_array.add(3, devCount_array);

		devlist.put("devicesCounts", dev_array);

		return devlist;
	}

	/*
	 * @RequestMapping(value = "/test", method = RequestMethod.GET) public
	 * static String steerTest() { try {
	 * 
	 * JSONParser parser = new JSONParser(); Object obj = parser.parse(new
	 * FileReader("filepath")); return obj.toString(); } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * return null; }
	 */

}

package com.semaifour.facesix.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
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
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.data.account.UserAccount;
import com.semaifour.facesix.data.account.UserAccountService;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDevice;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;
import com.semaifour.facesix.data.site.Site;
import com.semaifour.facesix.data.site.SiteService;
import com.semaifour.facesix.util.CustomerUtils;
import com.semaifour.facesix.util.HeaderFooterPageEvent;
import com.semaifour.facesix.util.SessionUtil;
import com.semaifour.facesix.web.WebController;

@RestController
@RequestMapping("/rest/gatewayreport")
public class GatewayReport extends WebController {

	static Logger LOG = LoggerFactory.getLogger(GatewayReport.class.getName());

	static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
	static Font catFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
	static Font redFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
	static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
	static Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

	DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	TimeZone timezone = null;

	@Autowired
	NetworkDeviceRestController networkDeviceRestController;

	@Autowired
	NetworkDeviceService networkDeviceService;

	@Autowired
	SiteService siteService;

	@Autowired
	PortionService portionService;

	@Autowired
	DeviceService devService;

	@Autowired
	CustomerService customerservice;

	@Autowired
	UserAccountService userAccountService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	CustomerUtils customerUtils;

	@Autowired
	DeviceService deviceservice;

	@Autowired
	DeviceRestController deviceRestController;

	private String indexname = "facesix*";

	@Autowired
	FSqlRestController fsqlRestController;

	@PostConstruct
	public void init() {
		indexname = _CCC.properties.getProperty("elasticsearch.indexnamepattern", "facesix*");
	}

	@RequestMapping(value = "/format", method = RequestMethod.GET)
	public String format(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "venuename", required = false) String sid,
			@RequestParam(value = "floorname", required = false) String spid,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "filtertype", required = true) String filtertype,
			@RequestParam(value = "time", required = false) String days,
			@RequestParam(value = "fileformat", required = true) String fileformat,
			@RequestParam(value = "devStatus", required = false) String devStatus, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ParseException {

		String result = "";

		if (fileformat.equals("pdf")) {
			result = pdf(cid, sid, spid, location, macaddr, filtertype, days, devStatus, request, response);
		} else {
			result = csv(cid, sid, spid, location, macaddr, filtertype, days, devStatus, request, response);
		}

		return result;
	}

	public String pdf(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "venuename", required = false) String sid,
			@RequestParam(value = "floorname", required = false) String spid,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "filtertype", required = true) String filtertype,
			@RequestParam(value = "time", required = false) String days,
			@RequestParam(value = "devStatus", required = false) String devStatus, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ParseException {

		String pdfFileName = "./uploads/qubercloud.pdf";
		String logoFileName = "./uploads/logo-home.png";

		// LOG.info("PDF func: cid "+cid+" sid "+sid+" spid "+spid + " macaddr
		// "+macaddr+" time "+days);

		// String pdfFileName = "Report.pdf";
		// String logoFileName = "/home/qubercomm/Desktop/pdf/logo.png";

		FileOutputStream os = null;
		FileInputStream fileInputStream = null;
		OutputStream responseOutputStream = null;
		Customer customer = null;

		if (SessionUtil.isAuthorized(request.getSession())) {

			Document document = new Document(PageSize.A4, 36, 36, 90, 55);
			try {

				if (cid == null) {
					cid = SessionUtil.getCurrentCustomer(request.getSession());
					if (cid == null) {
						return null;
					}
				}

				String currentuser = SessionUtil.currentUser(request.getSession());
				UserAccount cur_user = userAccountService.findOneByEmail(currentuser);
				String userName = cur_user.getFname() + " " + cur_user.getLname();

				customer = customerservice.findById(cid);
				logoFileName = customer.getLogofile() == null ? logoFileName : customer.getLogofile();
				String customerName = customer.getCustomerName();

				Path path = Paths.get(logoFileName);

				if (!Files.exists(path)) {
					logoFileName = "./uploads/logo-home.png";
				}
				timezone = customerUtils.FetchTimeZone(customer.getTimezone());
				format.setTimeZone(timezone);

				os = new FileOutputStream(pdfFileName);
				PdfWriter writer = PdfWriter.getInstance(document, os);
				HeaderFooterPageEvent event = new HeaderFooterPageEvent(customerName, userName, logoFileName,
						format.format(new Date()));
				writer.setPageEvent(event);
				document.open();

				addContent(cid, sid, spid, location, macaddr, filtertype, days, devStatus, document);

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

	private void addContent(String cid, String sid, String spid, String location, String macaddr, String filtertype,
			String days, String devStatus, Document document) throws DocumentException, IOException, ParseException {

		// LOG.info("Add Content cid "+cid+" sid "+sid+" spid "+spid + " macaddr
		// "+macaddr+" time "+days);
		Paragraph subCatPart = new Paragraph();
		document.add(subCatPart);
		// add a table
		createTable(cid, sid, spid, location, macaddr, filtertype, days, devStatus, subCatPart, document);

	}

	@SuppressWarnings("unchecked")
	private void createTable(String cid, String sid, String spid, String location, String macaddr, String filtertype,
			String days, String devStatus, Paragraph subCatPart, Document document)
			throws IOException, ParseException, DocumentException {

		// LOG.info("CreateTable filtertype" + filtertype+" cid "+cid+" sid
		// "+sid+" spid "+spid + " macaddr "+macaddr+ "location"+ location + "
		// time "+days);

		if (filtertype.equals("deviceInfo")) {

			Paragraph content = new Paragraph("Device Information", subFont);
			PdfPTable table = new PdfPTable(9);
			table.setWidthPercentage(100);

			PdfPCell c1 = new PdfPCell(new Phrase("UID", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setColspan(2);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("FLOOR", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("LOCATION", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("DEVICE UPTIME", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("APP UPTIME", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("STATE", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("LAST SEEN", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setColspan(2);
			table.addCell(c1);

			table.setHeaderRows(1);
			JSONArray processedDetail = null;

			if (sid != null && (sid.equals("all") || sid.equals("undefined"))) {
				sid = "";
			}
			if (spid != null && (spid.equals("all") || spid.equals("undefined"))) {
				spid = "";
			}

			processedDetail = deviceInfo(cid, sid, spid, null, null);
			Iterator<JSONObject> iterProcessedDetail = processedDetail.iterator();
			JSONObject json = null;

			while (iterProcessedDetail.hasNext()) {

				json = iterProcessedDetail.next();

				String uid = json.get("uid").toString();
				String floorname = json.get("floorname").toString();
				String locationname = json.get("locationname").toString();
				String deviceUptime = json.get("deviceUptime").toString();
				String appUptime = json.get("appUptime").toString();
				String state = json.get("state").toString();
				String lastseen = json.get("lastseen").toString();

				c1 = new PdfPCell(new Phrase(uid, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setColspan(2);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(floorname, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(locationname, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(deviceUptime, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(appUptime, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(state, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(c1);

				c1 = new PdfPCell(new Phrase(lastseen, redFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				c1.setColspan(2);
				table.addCell(c1);

			}
			content.add(table);
			subCatPart.add(content);
			document.add(subCatPart);

		} else {

			Map<String, String> portionmap = new HashMap<String, String>();
			Map<String, String> venuemap = new HashMap<String, String>();

			Iterable<Device> devices = null;
			boolean includeVenueNameHeader = true;

			switch (filtertype) {
			case "default":

				devices = deviceservice.findByCid(cid);
				break;

			case "venue":

				if (sid.isEmpty() || sid.equals("all"))
					devices = deviceservice.findByCid(cid);
				else
					devices = deviceservice.findBySid(sid);
				break;

			case "floor":

				if (sid.isEmpty() || sid.equals("all"))
					devices = deviceservice.findByCid(cid);
				else if (spid.isEmpty() || spid.equals("all"))
					devices = deviceservice.findBySid(sid);
				else {
					devices = deviceservice.findBySpid(spid);
					includeVenueNameHeader = false;
				}
				break;

			case "location":

				if (sid.isEmpty() || sid.equals("all"))
					devices = deviceservice.findByCid(cid);
				else if (spid.isEmpty() || spid.equals("all"))
					devices = deviceservice.findBySid(sid);
				else if (location.isEmpty() || location.equals("all")) {
					devices = deviceservice.findBySpid(spid);
					includeVenueNameHeader = false;
				} else {
					devices = deviceservice.findByUid(location);
					includeVenueNameHeader = false;
				}
				break;

			case "devStatus":
				devices = getDeviceByCidAndStatus(cid, devStatus);
			}

			PdfPTable table = new PdfPTable(4);
			PdfPTable ap_table = null;

			if (includeVenueNameHeader) {
				ap_table = new PdfPTable(6);
			} else {
				ap_table = new PdfPTable(5);
			}
			table.setWidthPercentage(100);
			ap_table.setWidthPercentage(100);

			PdfPCell c1 = new PdfPCell(new Phrase("VENUE", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("FLOOR", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("LOCATION", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			c1 = new PdfPCell(new Phrase("STATUS", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(c1);

			table.setHeaderRows(1);

			c1 = new PdfPCell(new Phrase("SSID", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			ap_table.addCell(c1);

			c1 = new PdfPCell(new Phrase("CLIENT NAME", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			ap_table.addCell(c1);

			c1 = new PdfPCell(new Phrase("CLIENT TYPE", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			ap_table.addCell(c1);

			c1 = new PdfPCell(new Phrase("CLIENT ID", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			c1.setColspan(1);
			ap_table.addCell(c1);

			if (includeVenueNameHeader) {
				c1 = new PdfPCell(new Phrase("FLOOR", headerFont));
				c1.setHorizontalAlignment(Element.ALIGN_CENTER);
				ap_table.addCell(c1);
			}

			c1 = new PdfPCell(new Phrase("LOCATION", headerFont));
			c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			ap_table.addCell(c1);
			ap_table.setHeaderRows(1);

			Site site = null;
			List<Device> list = new ArrayList<Device>();

			for (Device id : devices) {
				list.add(id);
			}

			if (days == null)
				days = "12h";

			if (list != null) {

				String dev_sid = "NA";
				String dev_spid = "NA";
				String venuename = "NA";
				String floorname = "NA";
				String locationname = "NA";

				for (Device device : list) {

					dev_sid = device.getSid() == null ? "NA" : device.getSid();
					dev_spid = device.getSpid() == null ? "NA" : device.getSpid();
					venuename = "NA";
					floorname = "NA";
					locationname = "NA";

					if (venuemap.containsKey(dev_sid)) {
						venuename = venuemap.get(dev_sid);
						c1 = new PdfPCell(new Phrase(venuename, redFont));
						c1.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(c1);
					} else {
						site = siteService.findById(dev_sid);
						if (site != null) {
							venuename = site.getUid().toUpperCase();
						}
						c1 = new PdfPCell(new Phrase(venuename, redFont));
						c1.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(c1);
						venuemap.put(dev_sid, venuename);
					}

					if (portionmap.containsKey(dev_spid)) {
						floorname = portionmap.get(dev_spid);
						c1 = new PdfPCell(new Phrase(floorname, redFont));
						c1.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(c1);
					} else {
						Portion port = portionService.findById(dev_spid);
						if (port != null) {
							floorname = port.getUid().toUpperCase();
						}
						portionmap.put(dev_sid, floorname);
						c1 = new PdfPCell(new Phrase(floorname, redFont));
						c1.setHorizontalAlignment(Element.ALIGN_CENTER);
						table.addCell(c1);
					}

					String uid = device.getUid();
					locationname = device.getAlias().toUpperCase();

					c1 = new PdfPCell(new Phrase(locationname, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(device.getStatus().toUpperCase(), redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					JSONObject JStr = networkDeviceRestController.getpeers(null, null, null, uid, days, "report");
					JSONArray slideContent = (JSONArray) JStr.get("devicesConnected");
					JSONArray devicesConnected = null;

					if (slideContent != null && slideContent.size() > 0) {
						if (slideContent.get(0) != null) {
							devicesConnected = (JSONArray) slideContent.get(0);
						}
					}

					if (devicesConnected != null) {
						Iterator i = devicesConnected.iterator();

						while (i.hasNext()) {

							JSONObject slide = (JSONObject) i.next();
							String mac_address = (String) slide.get("mac_address");
							String SSID = (String) slide.get("ssid");
							String dev_type = (String) slide.get("devtype");
							String client_type = (String) slide.get("client_type");

							c1 = new PdfPCell(new Phrase(SSID, redFont));
							c1.setHorizontalAlignment(Element.ALIGN_CENTER);
							ap_table.addCell(c1);

							c1 = new PdfPCell(new Phrase(dev_type, redFont));
							c1.setHorizontalAlignment(Element.ALIGN_CENTER);
							ap_table.addCell(c1);

							c1 = new PdfPCell(new Phrase(client_type.toUpperCase(), redFont));
							c1.setHorizontalAlignment(Element.ALIGN_CENTER);
							ap_table.addCell(c1);

							c1 = new PdfPCell(new Phrase(mac_address.toUpperCase(), redFont));
							c1.setHorizontalAlignment(Element.ALIGN_CENTER);
							ap_table.addCell(c1);

							if (includeVenueNameHeader) {
								c1 = new PdfPCell(new Phrase(floorname, redFont));
								c1.setHorizontalAlignment(Element.ALIGN_CENTER);
								ap_table.addCell(c1);
							}

							c1 = new PdfPCell(new Phrase(locationname, redFont));
							c1.setHorizontalAlignment(Element.ALIGN_CENTER);
							ap_table.addCell(c1);
						}
					}
				}
			}

			if (table.size() > 1) {
				String venue = "Device  Report Summary";
				Paragraph Para = new Paragraph(venue, subFont);
				addEmptyLine(Para, 1);
				Para.add(table);
				addEmptyLine(Para, 3);
				document.add(Para);
			}

			if (ap_table.size() > 1) {
				Paragraph subPara = new Paragraph("Client Report Summary", subFont);
				addEmptyLine(subPara, 1);
				if (!includeVenueNameHeader) {

					String floorname = "NA";
					if (portionmap.containsKey(spid)) {
						floorname = portionmap.get(spid);
					} else {
						Portion p = portionService.findById(spid);
						if (p != null) {
							floorname = p.getUid().toUpperCase();
						}
					}

					subPara.add(new Paragraph("FLOOR : " + floorname, smallBold));
				} else if (sid != null && !sid.isEmpty() && !sid.equals("all")) {
					Site s = siteService.findById(sid);
					String venuename = s.getUid().toUpperCase();
					subPara.add(new Paragraph("VENUE : " + venuename, smallBold));
				}
				addEmptyLine(subPara, 1);
				subPara.add(ap_table);
				addEmptyLine(subPara, 3);
				document.add(subPara);
			}

			if (ap_table.size() <= 1 && table.size() <= 1) {
				Paragraph Para = new Paragraph();
				Para = addNoDataToPDF(Para);
				document.add(Para);
			}
		}
	}

	private Iterable<Device> getDeviceByCidAndStatus(String cid, String devStatus) {

		Iterable<Device> device = null;

		if (devStatus.equals("all")) {
			device = deviceservice.findByCid(cid);
		} else {
			device = deviceservice.findByCidAndState(cid, devStatus);
		}

		return device;
	}

	public String csv(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "venuename", required = false) String sid,
			@RequestParam(value = "floorname", required = false) String spid,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "macaddr", required = false) String macaddr,
			@RequestParam(value = "filtertype", required = true) String filtertype,
			@RequestParam(value = "time", required = false) String days,
			@RequestParam(value = "devStatus", required = false) String devStatus, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		// LOG.info("Csvfunc filtertype" + filtertype+" cid "+cid+" sid "+sid+"
		// spid "+spid + " macaddr "+macaddr+ "location"+ location + " time
		// "+days);

		String csvFileName = "Report.csv";

		OutputStream out = null;
		Map<String, String> portionMap = new HashMap<String, String>();
		Map<String, String> venueMap = new HashMap<String, String>();

		try {
			if (SessionUtil.isAuthorized(request.getSession())) {
				String result = "";

				if (sid != null && (sid.equals("all") || sid.equals("undefined"))) {
					sid = "";
				}
				if (spid != null && (spid.equals("all") || spid.equals("undefined"))) {
					spid = "";
				}

				if (filtertype.equals("deviceInfo")) {

					result = "UID,FLOOR,LOCATION,DEVICE UPTIME,APP UPTIME,STATE,LAST SEEN\n";
					JSONArray processedDetail = deviceInfo(cid, sid, spid, request, response);
					Iterator<JSONObject> iterProcessedDetail = processedDetail.iterator();
					JSONObject json = null;
					while (iterProcessedDetail.hasNext()) {
						json = iterProcessedDetail.next();
						String uid = json.get("uid").toString();
						String floorname = json.get("floorname").toString();
						String locationname = json.get("locationname").toString();
						String deviceUptime = json.get("deviceUptime").toString();
						String appUptime = json.get("appUptime").toString();
						String state = json.get("state").toString();
						String lastseen = json.get("lastseen").toString();
						result += uid + "," + floorname + "," + locationname + "," + deviceUptime + "," + appUptime
								+ "," + state + "," + lastseen + "\n";
					}
				} else {

					boolean includeVenueNameHeader = true;
					Iterable<Device> devices = null;

					switch (filtertype) {
					case "default":

						devices = deviceservice.findByCid(cid);
						break;

					case "venue":

						if (sid.isEmpty() || sid.equals("all"))
							devices = deviceservice.findByCid(cid);
						else
							devices = deviceservice.findBySid(sid);
						break;

					case "floor":

						if (sid.isEmpty() || sid.equals("all"))
							devices = deviceservice.findByCid(cid);
						else if (spid.isEmpty() || spid.equals("all"))
							devices = deviceservice.findBySid(sid);
						else {
							devices = deviceservice.findBySpid(spid);
							includeVenueNameHeader = false;
						}
						break;

					case "location":

						if (sid.isEmpty() || sid.equals("all"))
							devices = deviceservice.findByCid(cid);
						else if (spid.isEmpty() || spid.equals("all"))
							devices = deviceservice.findBySid(sid);
						else if (location.isEmpty() || location.equals("all")) {
							devices = deviceservice.findBySpid(spid);
							includeVenueNameHeader = false;
						} else {
							devices = deviceservice.findByUid(location);
							includeVenueNameHeader = false;
						}
						break;

					case "devStatus":
						devices = getDeviceByCidAndStatus(cid, devStatus);
					}

					String Deviceheader = "VENUE,FLOOR,LOCATION,STATUS\n";
					String Clientheader = "SSID,CLIENT NAME,CLIENT TYPE,CLIENT ID,";
					if (includeVenueNameHeader) {
						Clientheader += "FLOOR,";
					}
					Clientheader += "LOCATION\n";
					String Devicelist = "";
					String Clientlist = "";

					List<Device> list = new ArrayList<Device>();

					for (Device id : devices) {
						list.add(id);
					}

					// LOG.info("List=" +list.toString());

					if (days == null)
						days = "12h";

					if (list != null) {

						String Device = "";
						String Client = "";

						String dev_sid = "NA";
						String dev_spid = "NA";
						String Venue = "NA";
						String Floor = "NA";

						for (Device dev : list) {

							dev_sid = dev.getSid() == null ? "NA" : dev.getSid();
							dev_spid = dev.getSpid() == null ? "NA" : dev.getSpid();

							if (venueMap.containsKey(dev_sid)) {
								Venue = venueMap.get(dev_sid);
							} else {
								Site site = siteService.findById(dev_sid);
								if (site != null) {
									Venue = site.getUid().toUpperCase();
								}
								venueMap.put(dev_sid, Venue);
							}

							if (portionMap.containsKey(dev_spid)) {
								Floor = portionMap.get(dev_spid);
							} else {
								Portion port = portionService.findById(dev_spid);
								if (port != null) {
									Floor = port.getUid().toUpperCase();
								}
							}

							String dev_uid = dev.getUid();
							String Status = dev.getStatus().toUpperCase();
							String devName = dev.getAlias().toUpperCase();

							Device = Venue + "," + Floor + "," + devName + "," + Status + "," + "\n";
							Devicelist = Devicelist.concat(Device);

							JSONObject JStr = networkDeviceRestController.getpeers(null, null, null, dev_uid, days,
									"report");

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
									String SSID = (String) slide.get("ssid");
									String dev_type = (String) slide.get("devtype");
									String client_type = (String) slide.get("client_type");
									dev_type = dev_type.replace(",", "");

									Client = SSID + "," + dev_type + "," + client_type.toUpperCase() + ","
											+ mac_address.toUpperCase() + ",";

									if (includeVenueNameHeader) {
										Client += Floor;
									}
									Client += "," + devName + "\n";

									Clientlist = Clientlist.concat(Client);

								}
							}
						}

						result = result.concat("Device Report Summary");
						result = result.concat("\n");
						result = result.concat(Deviceheader);
						result = result.concat(Devicelist);
						result = result.concat("\n\n");
						result = result.concat("Client Report Summary");
						result = result.concat("\n");
						if (includeVenueNameHeader) {
							Venue = "NA";
							if (venueMap.containsKey(sid)) {
								Venue = venueMap.get(sid);
							} else {
								Site s = siteService.findById(sid);
								if (s != null) {
									Venue = s.getUid().toUpperCase();
								}
							}
							result = result.concat("VENUE : " + Venue + "\n");
						} else {
							Floor = "NA";
							if (portionMap.containsKey(spid)) {
								Floor = portionMap.get(spid);
							} else {
								Portion p = portionService.findById(spid);
								if (p != null) {
									Floor = p.getUid().toUpperCase();
								}
							}
							result = result.concat("FLOOR : " + Floor + "\n");
						}
						result = result.concat(Clientheader);
						result = result.concat(Clientlist);
						result = result.concat("\n");

						result = result.concat("\n");

					}
				}
				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=" + csvFileName);
				out = response.getOutputStream();
				out.write(result.getBytes());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}

		return csvFileName;
	}

	@RequestMapping(value = "/gw_alertpdf", method = RequestMethod.GET)
	public String tagalertpdf(@RequestParam(value = "cid", required = false) String cid, HttpServletRequest request,
			HttpServletResponse response) {

		// String pdfFileName = "GatewayAlertReport.pdf";
		// String logoFileName = "/home/qubercomm/Desktop/pdf/logo.png";

		String pdfFileName = "./uploads/alert.pdf";
		String logoFileName = "./uploads/logo-home.png";

		if (SessionUtil.isAuthorized(request.getSession())) {

			Document document = new Document(PageSize.A4, 36, 36, 90, 55);
			try {

				if (cid == null) {
					cid = SessionUtil.getCurrentCustomer(request.getSession());
				}
				Customer cx = customerservice.findByUId(cid);
				String cx_name = cx.getCustomerName();
				timezone = customerUtils.FetchTimeZone(cx.getTimezone());// cx.getTimezone()
				format.setTimeZone(timezone);

				String currentuser = SessionUtil.currentUser(request.getSession());
				UserAccount cur_user = userAccountService.findOneByEmail(currentuser);
				String userName = cur_user.getFname() + " " + cur_user.getLname();

				logoFileName = cx.getLogofile() == null ? logoFileName : cx.getLogofile();
				Path path = Paths.get(logoFileName);

				if (!Files.exists(path)) {
					logoFileName = "./uploads/logo-home.png";
				}

				FileOutputStream os = new FileOutputStream(pdfFileName);
				PdfWriter writer = PdfWriter.getInstance(document, os);
				HeaderFooterPageEvent event = new HeaderFooterPageEvent(cx_name, userName, logoFileName,
						format.format(new Date()));
				writer.setPageEvent(event);
				document.open();

				addContent(document, cid, cx_name);
				document.close();

				File pdfFile = new File(pdfFileName);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + pdfFileName);
				response.setContentLength((int) pdfFile.length());

				FileInputStream fileInputStream = new FileInputStream(pdfFile);
				OutputStream responseOutputStream = response.getOutputStream();
				int bytes;
				while ((bytes = fileInputStream.read()) != -1) {
					responseOutputStream.write(bytes);
				}

				responseOutputStream.close();
				fileInputStream.close();
				os.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return pdfFileName;
	}

	private void addContent(Document document, String cid, String customerName) {
		try {

			Paragraph subCatPart = new Paragraph();

			// add a table
			createTable(subCatPart, document, cid, customerName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createTable(Paragraph subCatPart, Document document, String cid, String customerName)
			throws DocumentException {

		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(100);

		PdfPCell c1 = new PdfPCell(new Phrase("UID", headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setColspan(2);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Alias", headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Floor Name", headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Status", headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(c1);

		c1 = new PdfPCell(new Phrase("Last Active", headerFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setColspan(2);
		table.addCell(c1);

		table.setHeaderRows(1);

		try {
			Boolean generatepdf = true;
			JSONObject deviceslist = networkDeviceRestController.alert(cid, generatepdf);

			if (deviceslist != null && !deviceslist.isEmpty()) {

				JSONArray array = (JSONArray) deviceslist.get("inactive_list");
				Iterator<JSONObject> iterator = array.iterator();

				while (iterator.hasNext()) {
					JSONObject rep = iterator.next();

					String macaddr = (String) rep.get("macaddr");
					String alias = (String) rep.get("alias");
					String floor = (String) rep.get("portionname");
					String status = (String) rep.get("state");
					String lastactive = (String) rep.get("timestamp");

					c1 = new PdfPCell(new Phrase(macaddr, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					c1.setColspan(2);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(alias, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(floor, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(status, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(c1);

					c1 = new PdfPCell(new Phrase(lastactive, redFont));
					c1.setHorizontalAlignment(Element.ALIGN_CENTER);
					c1.setColspan(2);
					table.addCell(c1);
				}
				subCatPart = new Paragraph("Device Alerts ", subFont);
				addEmptyLine(subCatPart, 1);
				subCatPart.add(table);
				document.add(subCatPart);
			} else {
				subCatPart = addNoDataToPDF(subCatPart);
				document.add(subCatPart);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void emailTrigger(String uid) {

		String pdfFileName = "alert.pdf";
		String logoFileName = "./uploads/logo-home.png";

		UserAccount users = userAccountService.findOneByUid(uid);

		if (users != null && users.isMailalert() != null && users.isMailalert().equals("true")) {

			String cid = users.getCustomerId();
			String email = users.getEmail();

			try {

				JSONObject deviceslist = networkDeviceRestController.alert(cid, null);
				JSONArray inactiveDevices = (JSONArray) deviceslist.get("inactive_list");

				JSONObject inactivetag = (JSONObject) inactiveDevices.get(0);
				String inactivemac = inactivetag.get("macaddr").toString();

				if (inactivemac.equals("-")) {
					LOG.info("=====NO DEVICE ALTER FOUND=====");
					return;
				}

				Document document = new Document(PageSize.A4, 36, 36, 90, 55);
				LOG.info("Email Alerts enabled user " + uid);

				Customer cx = customerservice.findById(cid);
				String cx_name = cx.getCustomerName();
				String userName = users.getFname() + " " + users.getLname();
				timezone = customerUtils.FetchTimeZone(cx.getTimezone());// cx.getTimezone()
				format.setTimeZone(timezone);

				logoFileName = cx.getLogofile() == null ? logoFileName : cx.getLogofile();
				Path path = Paths.get(logoFileName);

				if (!Files.exists(path)) {
					logoFileName = "./uploads/logo-home.png";
				}

				File file = new File(pdfFileName);
				FileOutputStream os = new FileOutputStream(file);
				PdfWriter writer = PdfWriter.getInstance(document, os);
				HeaderFooterPageEvent event = new HeaderFooterPageEvent(cx_name, userName, logoFileName,
						format.format(new Date()));
				writer.setPageEvent(event);
				document.open();

				addContent(document, cid, cx_name);
				document.close();
				os.close();

				String body = "Dear " + cx_name + ",\n\n You have a new Alert Message!!!\n"
						+ "PFA detailed list of inactive devices.\n Please look in to this as a high priority.\n"
						+ "ALERTS - DEVICES @RISK, REQUIRES YOUR IMMEDIATE ATTENTION. \n";

				final String subject = "Alert Notification";

				customerUtils.customizeSupportEmail(cid, email, subject, body, file);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LOG.info("Email Alerts disabled user email " + uid);
		}
	}

	public void EmailTriggeringForAlerts(File pdfFile, String email) {

		StringBuilder mailBody = new StringBuilder();

		mailBody.append("Dear Customer,<br/>").append("You have a new Alert Message!!!<br/>")
				.append("PFA detailed list of inactive devices.<br/> Please look in to this as a high priority.<br/>")
				.append("ALERTS - DEVICES/TAGS @RISK, REQUIRE YOUR IMMEDIATE ATTENTION <br/>");

		LOG.info("email id " + email);
		LOG.info("mail body  " + mailBody);

		javaMailSender.send(new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws MessagingException {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setTo(email);
				message.setSubject("Qubercomm Notification");
				message.setText(mailBody.toString(), true);
				message.addAttachment("alert.pdf", pdfFile);
			}
		});
	}

	@RequestMapping(value = "/gw_alertcsv", method = RequestMethod.GET)
	public String gatewayalertcsv(@RequestParam(value = "cid", required = true) String cid, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String csvFileName = "./uploads/alert.csv";

		try {

			if (SessionUtil.isAuthorized(request.getSession())) {

				String result = "";
				String gatewayheader = "";

				gatewayheader = "UID,Floor Name ,Alias,Status,Last Active\n";

				JSONObject deviceslist = networkDeviceRestController.alert(cid, null);

				if (deviceslist != null && !deviceslist.isEmpty()) {

					result = result.concat("DEVICES ALERT");
					result = result.concat("\n");
					result = result.concat(gatewayheader);

					JSONArray array = (JSONArray) deviceslist.get("inactive_list");
					Iterator<JSONObject> i = array.iterator();

					String inactivedevices = null;

					while (i.hasNext()) {

						JSONObject rep = i.next();

						String macaddr = (String) rep.get("macaddr");
						String alias = (String) rep.get("alias");
						String floor = (String) rep.get("portionname");
						String status = (String) rep.get("state");
						String lastactive = (String) rep.get("state");

						inactivedevices = macaddr + "," + floor + "," + alias + "," + status + "," + lastactive + "\n";

						result = result.concat(inactivedevices);
					}
					result = result.concat("\n\n");
				}

				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; filename=" + csvFileName);
				OutputStream out = response.getOutputStream();
				out.write(result.getBytes());

				out.flush();
				out.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return csvFileName;
	}

	private Paragraph addNoDataToPDF(Paragraph paragraph) {
		addEmptyLine(paragraph, 5);
		PdfPTable table = new PdfPTable(1);
		table.setWidthPercentage(100);
		PdfPCell c1 = new PdfPCell(new Phrase("No Data....", redFont));
		c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		c1.setBorder(Rectangle.NO_BORDER);
		table.addCell(c1);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		paragraph.add(table);
		return paragraph;
	}

	@RequestMapping(value = "/locationlist", method = RequestMethod.GET)
	public JSONObject locationlist(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid, HttpServletRequest request,
			HttpServletResponse response) {
		if (cid == null) {
			cid = SessionUtil.getCurrentCustomer(request.getSession());
		}
		JSONObject json = null;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonList = new JSONObject();
		Iterable<Device> ndList = new ArrayList<Device>();

		if (sid.equalsIgnoreCase("all") || spid.equalsIgnoreCase("all")) {
			return jsonList;
		}

		ndList = deviceservice.findBySpid(spid);

		if (ndList != null) {
			for (Device d : ndList) {
				json = new JSONObject();
				json.put("id", d.getUid());
				json.put("name", d.getAlias());
				jsonArray.add(json);
			}
			jsonList.put("location", jsonArray);
		}
		return jsonList;

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/gw_htmlCharts", method = RequestMethod.GET)
	public JSONObject gw_htmlCharts(HttpServletRequest request, HttpServletResponse response) {

		if (SessionUtil.isAuthorized(request.getSession())) {

			String filterType = request.getParameter("filtertype");
			String cid = request.getParameter("cid");
			String sid = request.getParameter("venuename");
			String spid = request.getParameter("floorname");
			String location = request.getParameter("location");
			String time = request.getParameter("time");

			LOG.info(" filterType " + filterType + " cid " + cid + " sid " + sid + " spid " + spid);
			LOG.info(" location " + location + " time " + time);

			if (time == null || time.isEmpty()) {
				time = "24h";
			}

			String place = "htmlchart";
			List<Map<String, Object>> cpu = null;
			List<Map<String, Object>> mem = null;
			List<Map<String, Object>> rxtx = null;
			List<NetworkDevice> networkDevice = null;

			JSONObject detailsJson = new JSONObject();
			JSONArray clientDetails = null;

			switch (filterType) {

			case "default": {
				networkDevice = networkDeviceService.findByCid(cid);
				clientDetails = clientsDetails(networkDevice, time);
			}

				break;
			case "venue":

				if (sid != null && sid.equals("all")) {
					networkDevice = networkDeviceService.findByCid(cid);
					clientDetails = clientsDetails(networkDevice, time);
				} else {
					networkDevice = networkDeviceService.findBySid(sid);
					clientDetails = clientsDetails(networkDevice, time);
					rxtx = networkDeviceRestController.rxtx(sid, null, null, null, time, place, request, response);
				}
				break;

			case "floor":

				if (sid.equals("all")) {
					networkDevice = networkDeviceService.findByCid(cid);
				} else if (spid != null && spid.equals("all")) {
					networkDevice = networkDeviceService.findBySid(sid);
					rxtx = networkDeviceRestController.rxtx(sid, null, null, null, time, place, request, response);
				} else {
					networkDevice = networkDeviceService.findBySpid(spid);
					rxtx = networkDeviceRestController.rxtx(sid, null, null, null, time, place, request, response);
				}
				break;

			case "location":

				if (sid != null && sid.equals("all")) {
					networkDevice = networkDeviceService.findByCid(cid);
					clientDetails = clientsDetails(networkDevice, time);
				} else if (spid != null && spid.equals("all")) {
					networkDevice = networkDeviceService.findBySid(sid);
					clientDetails = clientsDetails(networkDevice, time);
					rxtx = networkDeviceRestController.rxtx(sid, null, null, null, time, place, request, response);
				} else if (location != null && location.equals("all")) {
					networkDevice = networkDeviceService.findBySpid(spid);
					clientDetails = clientsDetails(networkDevice, time);
					rxtx = networkDeviceRestController.rxtx(null, spid, null, null, time, place, request, response);
				} else {

					String uuid = location.replaceAll("[^a-zA-Z0-9]", "");
					networkDevice = networkDeviceService.findByUuid(uuid);

					if (networkDevice != null && networkDevice.size() > 0) {

						NetworkDevice dev = networkDevice.get(0);
						String uid = dev.getUid().toLowerCase();

						clientDetails = clientsDetails(networkDevice, time);
						cpu = networkDeviceRestController.getcpu(null, null, null, uid, time, place);
						mem = networkDeviceRestController.getmem(null, null, null, uid, time, place);
						rxtx = networkDeviceRestController.rxtx(null, null, null, uid, time, place, request, response);
					}

					detailsJson.put("cpu", cpu);
					detailsJson.put("mem", mem);

				}

				break;

			default:
				break;
			}

			detailsJson.put("clientDetails", clientDetails);
			detailsJson.put("rxtx", rxtx);

			// LOG.info("htmlChartDetail " +detailsJson);

			return detailsJson;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private JSONArray clientsDetails(List<NetworkDevice> networkDevice, String time) {

		JSONArray result = new JSONArray();
		JSONArray dev_array = null;
		JSONObject locationObject = null;
		String uidstr = "";
		String vap_2g = "";
		String vap_5g = "";
		Device dv = null;

		int vap_2g_cnt = 1;
		int vap_5g_cnt = 1;

		try {

			for (NetworkDevice device : networkDevice) {
				if (device.getTypefs() == null) {
					continue;
				}

				String uid = device.getUid().toLowerCase();
				String fsType = device.getTypefs();

				if (fsType.equals("ap")) {

					dv = deviceservice.findOneByUid(uidstr);

					if (dv != null) {
						vap_2g = dv.getVap2gcount();
						vap_5g = dv.getVap5gcount();

						if (vap_2g != null) {
							vap_2g_cnt = Integer.parseInt(vap_2g);
						}

						if (vap_5g != null) {
							vap_5g_cnt = Integer.parseInt(vap_5g);
						}

					}
					dev_array = new JSONArray();
					exec_fsql_getpeer(uid, vap_2g_cnt, vap_5g_cnt, dev_array, time);

					String location = device.getAlias() + " (" + uid + ")";

					if (dev_array != null && dev_array.size() > 0) {
						locationObject = new JSONObject();

						locationObject.put("uid", location);
						locationObject.put("details", dev_array);

						result.add(locationObject);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("processing clientsDetails  error " + e);
		}

		return result;
	}

	private boolean exec_fsql_getpeer(String uid, int vap2g, int vap5g, JSONArray dev_array, String duration)
			throws IOException {

		String fsql = "";
		String fsql_5g = "";
		int i = 0;

		List<Map<String, Object>> logs = EMPTY_LIST_MAP;
		List<Map<String, Object>> qlogs = EMPTY_LIST_MAP;

		for (i = 0; i < vap2g; i++) {

			fsql = "index=" + indexname + ",sort=timestamp desc,";

			fsql = fsql + "query=timestamp:>now-" + duration + "" + " AND ";
			fsql = fsql + "uid:\"" + uid + "\"";
			fsql = fsql + " AND vap_id:\"" + i + "\"";
			fsql = fsql + " AND radio_type:\"2.4Ghz\"|value(message,snapshot, NA);value(timestamp,timestamp, NA)|table";

			// LOG.info("2G FSQL PEER QUERY" + fsql);
			logs = fsqlRestController.query(fsql);

			if (logs != EMPTY_LIST_MAP) {
				addPeers(uid, logs, dev_array);
			}
		}

		for (i = 0; i < vap5g; i++) {

			fsql_5g = "index=" + indexname + ",sort=timestamp desc,";

			fsql_5g = fsql_5g + "query=timestamp:>now-" + duration + "" + " AND ";
			fsql_5g = fsql_5g + "uid:\"" + uid + "\"";
			fsql_5g = fsql_5g + " AND vap_id:\"" + i + "\"";
			fsql_5g = fsql_5g
					+ " AND radio_type:\"5Ghz\"|value(message,snapshot, NA);value(timestamp,timestamp, NA)|table";

			// LOG.info("5G FSQL PEER QUERY" + fsql_5g);

			qlogs = fsqlRestController.query(fsql_5g);

			if (qlogs != EMPTY_LIST_MAP) {
				addPeers(uid, qlogs, dev_array);
			}
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "rawtypes" })
	private boolean addPeers(String uid, List<Map<String, Object>> logs, JSONArray dev_array) throws IOException {

		try {

			int recordCount = 0;
			JSONObject dev_obj = null;
			Iterator<Map<String, Object>> iterator = logs.iterator();

			if (logs != null) {
				recordCount = logs.size();
			}

			long prev_Count = 0;

			while (iterator.hasNext()) {

				TreeMap<String, Object> me = new TreeMap<String, Object>(iterator.next());

				long count_2G = 0;
				long count_5G = 0;

				String JStr = (String) me.values().toArray()[0];
				String timestamp = (String) me.values().toArray()[1];

				JSONObject newJObject = null;
				JSONParser parser = new JSONParser();

				newJObject = (JSONObject) parser.parse(JStr);

				String radio_type = (String) newJObject.get("radio_type");
				long client_count = (long) newJObject.getOrDefault("client_count", 0);

				if (radio_type.equals("2.4Ghz")) {
					count_2G = client_count;
				} else {
					count_5G = client_count;
				}

				if (count_2G != 0 || count_5G != 0) {
					dev_obj = new JSONObject();

					if (prev_Count != client_count) {

						if (count_2G != 0) {
							dev_obj.put("twoG", count_2G);
						}
						if (count_5G != 0) {
							dev_obj.put("fiveG", count_5G);
						}

						dev_obj.put("time", timestamp);
						dev_array.add(dev_obj);
						prev_Count = client_count;
					} else {
						continue;
					}

				}

			}

			if ((dev_array != null && !dev_array.isEmpty()) && dev_array.size() == 1) {

				Map<String, Object> last = logs.get(recordCount - 1);
				JSONParser parser = new JSONParser();

				String JStr = (String) last.get("snapshot");
				String timestamp = (String) last.get("timestamp");

				JSONObject newJObject = (JSONObject) parser.parse(JStr);
				String radio_type = (String) newJObject.get("radio_type");
				long client_count = (long) newJObject.getOrDefault("client_count", 0);

				long count_2G = 0;
				long count_5G = 0;

				if (radio_type.equals("2.4Ghz")) {
					count_2G = client_count;
				} else {
					count_5G = client_count;
				}

				if (count_2G != 0 || count_5G != 0) {

					dev_obj = new JSONObject();

					if (count_2G != 0) {
						dev_obj.put("twoG", count_2G);
					}
					if (count_5G != 0) {
						dev_obj.put("fiveG", count_5G);
					}
					dev_obj.put("time", timestamp);

					dev_array.add(dev_obj);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	@RequestMapping(value = "/deviceInfo", method = RequestMethod.GET)
	public JSONArray deviceInfo(@RequestParam(value = "cid", required = false) String cid,
			@RequestParam(value = "sid", required = false) String sid,
			@RequestParam(value = "spid", required = false) String spid, HttpServletRequest request,
			HttpServletResponse response) {

		JSONArray deviceInfo = new JSONArray();
		JSONObject deviceObj = null;
		List<Device> deviceList = null;
		Map<String, String> portionmap = new HashMap<String, String>();

		if (spid != null && !spid.isEmpty()) {
			deviceList = deviceservice.findBySpid(spid);
		} else if (sid != null && !sid.isEmpty()) {
			deviceList = deviceservice.findBySid(sid);
		} else {
			deviceList = deviceservice.findByCid(cid);
		}

		String duration = "30m";
		List<Map<String, Object>> uptimeInfo = null;
		if (deviceList != null && deviceList.size() > 0) {
			for (Device device : deviceList) {

				String uid = device.getUid();
				String deviceUptime = "0 Days:0 Hours:0 Minutes";
				String appUptime = "0 Days:0 Hours:0 Minutes";
				String locationname = device.getAlias().toUpperCase();
				String state = device.getState().toUpperCase();
				String lastseen = device.getLastseen() == null ? "NA" : device.getLastseen();
				String devspid = device.getSpid() == null ? "NA" : device.getSpid();
				String fileStatus = device.getDevCrashDumpUploadStatus() == null ? "NA"
						: device.getDevCrashDumpUploadStatus();
				String fileName = device.getDevCrashdumpFileName() == null ? "NA" : device.getDevCrashdumpFileName();

				String floorname = "NA";

				if (portionmap.containsKey(devspid)) {
					floorname = portionmap.get(devspid);
				} else {
					Portion p = portionService.findById(devspid);
					if (p != null) {
						floorname = p.getUid().toUpperCase();
					}
					portionmap.put(devspid, floorname);
				}

				/*
				 * String version = device.getVersion(); String build =
				 * device.getBuild(); String spid = device.getSpid(); Portion p
				 * = portionService.findById(spid);
				 */

				uid = uid.toLowerCase();

				String fsql = " index=" + indexname
						+ ",sort=timestamp desc,size=1,query=cpu_stats:\"Qubercloud Manager\"" + " AND timestamp:>now-"
						+ duration + " AND uid:\"" + uid + "\"|value(uid,uid,NA);"
						+ " value(cpu_percentage,cpu,NA);value(timestamp,time,NA);"
						+ " value(cpu_days,cpuDays,NA);value(cpu_hours,cpuHours,NA);value(cpu_minutes,cpuMinutes,NA);"
						+ " value(app_days,appDays,NA);value(app_hours,appHours,NA);value(app_minutes,appMinutes,NA);|table";

				uptimeInfo = fsqlRestController.query(fsql);

				if (uptimeInfo != null && uptimeInfo.size() > 0) {
					Map<String, Object> info = uptimeInfo.get(0);

					deviceUptime = info.getOrDefault("cpuDays", 0) + " Days: " + info.getOrDefault("cpuHours", 0)
							+ " Hours: " + info.getOrDefault("cpuMinutes", 0) + " Minutes";

					appUptime = info.getOrDefault("appDays", 0) + " Days: " + info.getOrDefault("appHours", 0)
							+ " Hours: " + info.getOrDefault("appMinutes", 0) + " Minutes";
				}

				deviceObj = new JSONObject();

				String crashState = "enabled";
				if (fileStatus.equals("NA") || fileStatus.isEmpty() || !fileStatus.equals("0")) {
					crashState = "disabled";
				}

				deviceObj.put("uid", uid);
				deviceObj.put("floorname", floorname);
				deviceObj.put("locationname", locationname);
				deviceObj.put("deviceUptime", deviceUptime);
				deviceObj.put("appUptime", appUptime);
				deviceObj.put("state", state);
				deviceObj.put("lastseen", lastseen);
				deviceObj.put("fileName", fileName);
				deviceObj.put("filestatus", fileStatus);
				deviceObj.put("crashState", crashState);
				/*
				 * deviceObj.put("version", version); deviceObj.put("build",
				 * build);
				 */

				deviceInfo.add(deviceObj);

			}
		}
		return deviceInfo;
	}
}

package com.semaifour.facesix.beacon;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.Beacon;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.ElasticService;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDevice;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.elasticsearch.device.Device;
import com.semaifour.facesix.data.elasticsearch.device.DeviceService;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDevice;
import com.semaifour.facesix.data.elasticsearch.device.NetworkDeviceService;
import com.semaifour.facesix.mqtt.DefaultMqttMessageReceiver;
import com.semaifour.facesix.mqtt.DeviceEventPublisher;
import com.semaifour.facesix.mqtt.Payload;

import net.sf.json.JSONObject;

public class ScannerMqttMessageHandler extends DefaultMqttMessageReceiver {

	Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BeaconDeviceService _beaconDeviceService;

	private BeaconService _beaconService;

	NetworkDeviceService _networkDeviceService;

	@Autowired
	DeviceService deviceService;

	@Override
	public boolean messageArrived(String topic, MqttMessage message) {
		return messageArrived(topic, message.toString());
	}

	@Override
	public boolean messageArrived(String topic, String message) {
		// LOG.debug("handling msqtt message at " + topic + " : " + message);
		BeaconDeviceService beaconDev = getBeaconDeviceService();
		BeaconService beaconSvc = beaconService();
		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(message);
			String op = String.valueOf(jsonObject.get("opcode"));

			/*LOG.info("Opcode===> " + op);
			LOG.info("MAP ===> " + jsonObject);*/

			
		} catch (Exception e) {
			LOG.error("Failed to process messageArrvied at " + topic + " : " + message, e);
			return false;
		}
		return false;
	}

	private boolean updateCrashDump(Map<String, Object> map) {
		return false;
	}

	private boolean binarySetting_upgrade(Map<String, Object> map) throws Exception {
		
		return false;
	}

	private boolean update_ip(Map<String, Object> map) {
		return false;
	}

	public boolean createBeaconDevice(Map<String, Object> map, boolean notify) {
		return false;
	}

	private boolean updateBeaconDevice(Map<String, Object> map) throws Exception {
		return false;
	}

	private boolean pingBeaconDevice(Map<String, Object> map) { // publish
		return false;
	}

	private boolean peer_update_BeaconDevice(Map<String, Object> map) throws Exception {
		return false;
	}

	@Override
	public String getName() {
		return "ScannerMqttMessageHandler";
	}

	public BeaconService beaconService() {

		try {
			if (_beaconService == null) {
				_beaconService = Application.context.getBean(BeaconService.class);
			}
		} catch (Exception e) {

		}

		try {
			if (_beaconService == null) {
				LOG.warn("Unable to load BeaconService, please check");
			}
		} catch (Exception e) {

		}

		return _beaconService;

	}

	private BeaconDeviceService getBeaconDeviceService() {

		try {
			if (_beaconDeviceService == null) {
				_beaconDeviceService = Application.context.getBean(BeaconDeviceService.class);
			}
		} catch (Exception e) {

		}

		try {
			if (_beaconDeviceService == null) {
				LOG.info("Unable to load BeaconDeviceService, please check");
			}
		} catch (Exception e) {

		}
		return _beaconDeviceService;
	}

	private DeviceEventPublisher _mqttPublisher;


}

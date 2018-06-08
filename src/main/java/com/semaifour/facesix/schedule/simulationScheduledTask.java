package com.semaifour.facesix.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;

import com.semaifour.facesix.account.Customer;
import com.semaifour.facesix.account.CustomerService;
import com.semaifour.facesix.beacon.data.BeaconService;
import com.semaifour.facesix.boot.Application;
import com.semaifour.facesix.data.elasticsearch.beacondevice.BeaconDeviceService;
import com.semaifour.facesix.data.site.Portion;
import com.semaifour.facesix.data.site.PortionService;

import scala.concurrent.forkjoin.ForkJoinPool;
import scala.concurrent.forkjoin.RecursiveTask;

public class simulationScheduledTask extends RecursiveTask<Integer> {

	CustomerService customerService;
	PortionService portionService;
	BeaconDeviceService beaconDeviceService;
	BeaconService beaconService;
	
	public final String simulation = "enable";
	public final String cx_state = "ACTIVE";
	List<String> solution 		= Arrays.asList("GatewayFinder","GeoFinder");
	
	ForkJoinPool forkJoinPool   = null;
	
	boolean logenabled = false;
	String spid = null;
	
	private void setSpid (String spid) {
		this.spid = spid;
	}
	
	private void setlog (boolean log) {
		this.logenabled = log;
	}
	
	@Scheduled (fixedDelay=100)
	public void simulationSchedule() throws InterruptedException {
		List<Customer> customerList = getCustomerService().findBySimulationSolutionAndState(simulation,solution,cx_state);
		List<String> cidList = new ArrayList<String>();
		Map<String,Boolean> enableLogs = new HashMap<String,Boolean>();
		String cid;
		Boolean logs;
		for (Customer cx : customerList) {
			cid = cx.getId();
			logs = cx.getLogs() == null || cx.getLogs().equals("false") ? false : true;
			cidList.add(cx.getId());
			enableLogs.put(cid, logs);
		}
		List<Portion> portionList = getPortionService().findByCids(cidList);
		for (Portion p : portionList) {
			simulationScheduledTask sst = new simulationScheduledTask();
			cid = p.getCid();
			sst.setSpid(p.getId());
			sst.setlog(enableLogs.get(cid));
			forkJoinPool.execute(sst);
		}
	}

	@Override
	protected Integer compute() {
		
		return null;
	}
	
	public CustomerService getCustomerService() {
		if(customerService == null){
			customerService = Application.context.getBean(CustomerService.class);
		}
		return customerService;
	}
	public PortionService getPortionService() {
		if(portionService == null){
			portionService = Application.context.getBean(PortionService.class);
		}
		return portionService;
	}

	public BeaconDeviceService getBeaconDeviceService() {
		if (beaconDeviceService == null) {
			beaconDeviceService = Application.context.getBean(BeaconDeviceService.class);
		}
		return beaconDeviceService;
	}

	public BeaconService getBeaconService() {
		if (beaconService == null) {
			beaconService = Application.context.getBean(BeaconService.class);
		}
		return beaconService;
	}
}

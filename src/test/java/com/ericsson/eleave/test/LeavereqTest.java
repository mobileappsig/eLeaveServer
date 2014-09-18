package com.ericsson.eleave.test;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class LeavereqTest {
	private static final Log logger = LogFactory.getLog("LeavereqTest.class");

	@Test
	public void LeaveTypeSuccess() {
		Form form = new Form();
		
		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Leave/leavetype");
		System.out.println(client.toString());
		Representation representation = client.post(form.getWebRepresentation());
		logger.info("LeaveTypeSuccess!");
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}

	@Test
	public void LeaveQuerySuccess() {
		Form form = new Form();
		form.add("CaseId","0000000013");
		
		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Leave/queryleave");
		System.out.println(client.toString());
		Representation representation = client.post(form.getWebRepresentation());
		logger.info("LeaveTypeSuccess!");
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}
	
	@Test
	public void LeaveNewSuccess() {
		Form form = new Form();
		String jsonReq = "{\"LeavaTypeId\":\"1\",\"EID\":\"10000001\",\"LeaveDays\":\"2.0\",\"StatusID\":\"2\","
				       + "\"IssuedDate\":\"2014-06-23 10:44:00.0\",\"LeaveDetail\":"
				       + "[{\"StartDay\":\"2014-07-18\",\"StopDay\":\"2014-07-18\",\"AmOrPm\":\"NULL\",\"HalfDayOrNot\":\"NULL\"},"
				       + "{\"StartDay\":\"2014-07-20\",\"StopDay\":\"2014-07-20\",\"AmOrPm\":\"NULL\",\"HalfDayOrNot\":\"NULL\"}]}";
		form.add("newRequest",jsonReq);
		
		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Leave/newleave");
		System.out.println(client.toString());
		Representation representation = client.post(form.getWebRepresentation());
		logger.info("LeaveNewSuccess!");
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}
	
	@Test
	public void LeaveUpdSuccess() {
		Form form = new Form();
		form.add("CaseId","0000000013");
		
		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Leave/approveleave");
		System.out.println(client.toString());
		Representation representation = client.post(form.getWebRepresentation());
		logger.info("LeaveTypeSuccess!");
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}
}

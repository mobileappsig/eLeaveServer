package com.ericsson.eleave.test;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class UsermgmtTest {
	private static final Log logger = LogFactory.getLog("UsermgmtTest.class");
	@Test
	public void UserLoginSuccess() {
		Form form = new Form();
		form.add("usereid","test");
		form.add("password","aaa");
		
		System.out.println("UserLoginSuccess");

		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Users/login");
		System.out.println(client.toString());
		logger.info("UserLoginSuccess!");
		Representation representation = client.post(form.getWebRepresentation());
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}

	@Test
	public void UserRegisterSuccess() {
		Form form = new Form();
		form.add("username","eleave1");
		form.add("password","eleave1");
		
		System.out.println("UserRegisterSuccess");

		ClientResource client = new ClientResource(
				"http://localhost:8080/eleaveAppServer/API/Users/register");
		System.out.println(client.toString());
		Representation representation = client.post(form.getWebRepresentation());
		try {
			System.out.println("result:"+representation.getText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Assert.fail("Get HHO RealTime data failed");
			e.printStackTrace();
		}
	}


}

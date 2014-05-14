package com.ericsson.eleave.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.ericsson.eleave.db.EleaveDB;

/**
 * 
 * @author Karen Li
 *
 */
public class Usermgmt extends ServerResource {
	private static final Log logger = LogFactory.getLog("Usermgmt.class");
	protected JSONObject jsonObj = new JSONObject();
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultset = null;
	
	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		try {
			connection = EleaveDB.getConnection();
			if (connection == null) {
				logger.info("can not connect to database");
			} else {
				statement = connection.createStatement();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Post
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public Representation post(Representation entity) {
		// System.out.println("Call post method");
		Form form = new Form(entity);
		String action = (String)getRequestAttributes().get("action");
		if ( action!= null) {

			try {
				if ("login".equals(action)) {
					userLogin(form);
				} else if ("register".equals(action)) {
					userRegister(form);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		Representation representation = new JsonRepresentation(jsonObj);
		return representation;
	}

	private void userLogin(Form form){
		int userid = -1;
		String userName = form.getFirstValue("username");
		String password = form.getFirstValue("password");
		String queryString = "SELECT userid, username, userpasswd from users WHERE username = '" + userName
				  + "' and userpasswd = '" + password +"'";
		try {
			logger.info("query user with:"+queryString);
			resultset = statement.executeQuery(queryString);
			if (resultset.next()) {
				jsonObj.put("state", 200);
				jsonObj.put("msg", userName + " login successful!");
				userid = resultset.getInt(1);
				jsonObj.put("userId", userid);
			} else {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User or password error!");				
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
	}

	private void userRegister(Form form){
		String userName = form.getFirstValue("username");
		String userpasswd = form.getFirstValue("password");
		String queryString = "SELECT userid,username,userpasswd from users WHERE username = '" + userName + "'";
		int userid=-1;
		
		try{
			if (userName.trim().length() == 0) {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User name cannot be empty!");
			} else if (userpasswd.trim().length() == 0) {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "password cannot be empty!");
			} else {
				logger.info("query user with:"+queryString);
				resultset = statement.executeQuery(queryString);
				if (resultset.next()) {
					jsonObj.put("state", 104);
					jsonObj.put("msg", "User has been registered!");
				} else {
					
					String insertString = "INSERT INTO users (username, userpasswd) values('"+userName+"','"+userpasswd+"');";
					logger.info("insert user with:"+insertString);
					statement.executeUpdate(insertString);
					resultset = statement.executeQuery("SELECT userid,username,userpasswd from users WHERE username = '" + userName + "'");
					if (resultset.next()) {
    					userid = resultset.getInt(1);
	    				jsonObj.put("state", 200);
		    			jsonObj.put("msg", userName + " registered successful!");
			    		jsonObj.put("userId", userid);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}	


}

package com.ericsson.eleave.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.json.JSONObject;

import com.ericsson.eleave.db.EleaveDB;
import com.ericsson.eleave.db.LeavePolicy;

/**
 * 
 * @author Karen Li
 *
 */
public class UserMgmt extends ServerResource {
	private static final Log logger = LogFactory.getLog("UserMgmt.class");
	protected JSONObject jsonObj = new JSONObject();
	private Connection connection = null;
	//private Statement statement = null;
	//private ResultSet resultset = null;
	
	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		try {
			connection = EleaveDB.getConnection();
			if (connection == null) {
				logger.info("can not connect to database");
			} else {
				//statement = connection.createStatement();
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
					//userRegister(form);
				} else if ("leaveinfo".equals(action)) {
					leaveInfo(form);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		Representation representation = new JsonRepresentation(jsonObj);
		return representation;
	}

	private void leaveInfo(Form form){
		String eId = form.getFirstValue("EID");

		try {
			jsonObj = LeavePolicy.getLeave(eId);
			if (jsonObj == null) {
				jsonObj = new JSONObject();
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User info doesn't exist!");				
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	
/*	private void userLogin(Form form){
		String username = "";
		String usereid = form.getFirstValue("usereid");
		String password = form.getFirstValue("password");
		String queryString = "SELECT eid, username, userpasswd from users WHERE eid = '" + usereid
				  + "' and userpasswd = '" + password +"'";
		try {
			logger.info("query user with:"+queryString);
			resultset = statement.executeQuery(queryString);
			if (resultset.next()) {
				jsonObj.put("state", 200);
				username = resultset.getString("username");
				jsonObj.put("msg", username+" login successful!");
			} else {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User or password error!");				
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
	}*/

	private void userLogin(Form form){
		String eid = form.getFirstValue("EID");
		String password = form.getFirstValue("password");
		String queryString = "SELECT EID, password from login WHERE EID = '" + eid
				  + "' and password = '" + password +"'";
		try {
			jsonObj = EleaveDB.getJSONBySql(queryString);
			if (jsonObj != null) {
				jsonObj.put("state", 200);
				jsonObj.put("msg", " login successful!");
			} else {
				jsonObj = new JSONObject();
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User or password error!");				
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	
/*	private void userRegister(Form form){
		String usereid = form.getFirstValue("eid");
		String userName = form.getFirstValue("username");
		String userpasswd = form.getFirstValue("password");
		String queryString = "SELECT eid,username,userpasswd from users WHERE eid = '" + usereid + "'";
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
					
					String insertString = "INSERT INTO users (eid, username, userpasswd) values('"+usereid+"','"+userName+"','"+userpasswd+"');";
					logger.info("insert user with:"+insertString);
					statement.executeUpdate(insertString);
					resultset = statement.executeQuery("SELECT eid,username,userpasswd from users WHERE eid = '" + usereid + "'");
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
	}*/

	private void userRegister(Form form){
		String usereid = form.getFirstValue("eid");
		String userName = form.getFirstValue("username");
		String userpasswd = form.getFirstValue("password");
		String queryString = "SELECT eid,username,userpasswd from users WHERE eid = '" + usereid + "'";
		
		try{
			if (userName.trim().length() == 0) {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "User name cannot be empty!");
			} else if (userpasswd.trim().length() == 0) {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "password cannot be empty!");
			} else {
				ResultSet resultset = EleaveDB.getQueryRs(queryString);
				if (resultset.next()) {
					jsonObj.put("state", 104);
					jsonObj.put("msg", "User has been registered!");
				} else {
					String insertString = "INSERT INTO users (eid, username, userpasswd) values('"+usereid+"','"+userName+"','"+userpasswd+"');";
					if (EleaveDB.execSql(insertString) > 0){
						jsonObj = EleaveDB.getJSONBySql(queryString);
						if (jsonObj != null) {
	    				    jsonObj.put("state", 200);
		    			    jsonObj.put("msg", " registered successful!");
					    }
					} else{
						jsonObj.put("state", 104);
						jsonObj.put("msg", "Database error!");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

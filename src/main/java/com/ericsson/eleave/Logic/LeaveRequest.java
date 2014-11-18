package com.ericsson.eleave.Logic;

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

import com.ericsson.eleave.util.EleaveDB;
import com.ericsson.eleave.util.Util;

public class LeaveRequest extends ServerResource {
	private static final Log logger = LogFactory.getLog("LeaveRequest.class");
	protected JSONObject jsonObj = new JSONObject();
	
	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
	}
	
	@Post
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public Representation post(Representation entity) {
		Form form = new Form(entity);
		String action = (String)getRequestAttributes().get("action");
		if ( action!= null) {

			try {
				if ("draftleave".equals(action)) {
					newLeave(form);
				} else if ("submitleave".equals(action)) {
					submitLeave(form);
				} else if ("cancelleave".equals(action)) {
					cancelLeave(form);
				} else if ("queryleave".equals(action)) {
					queryLeave(form);
				} else if ("approveleave".equals(action)) {
					approveLeave(form);
				} else if ("leavetype".equals(action)) {
					LeaveType(form);
				} else if ("queryleavemgr".equals(action)) {
					queryLeaveMgr(form);
				} else if ("todo".equals(action)) {
					toDoList(form);
				} else if ("rejectleave".equals(action)) {
					rejectLeave(form);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		getResponse().setStatus(Status.SUCCESS_OK);
		Representation representation = new JsonRepresentation(jsonObj);
		return representation;
	}
	
	private void newLeave(Form form){
		String newReq = form.getFirstValue("newRequest");
		jsonObj = new JSONObject();

		try{
			JSONObject jsonReq = new JSONObject(newReq);
			int caseId = LeaveProc.newLeaveRequest(jsonReq);
			//System.out.print("caseId:"+caseId);
			
			if (caseId >= 0) {
				jsonObj.put("state", 200);
				jsonObj.put("CaseId", caseId);
				jsonObj.put("StatusID", "1");
			}
			else {
				jsonObj.put("state", 104);
				jsonObj.put("msg", "Wrong request!");
			}
				
   	    } catch (Exception e){
   	    	e.printStackTrace();
		}	
	}
	
	private void submitLeave(Form form){
		String newReq = form.getFirstValue("newRequest");
		String type = form.getFirstValue("type");
		jsonObj = new JSONObject();
		int caseId = -1;

		try{
			logger.info("newRequest:"+newReq);
			JSONObject jsonReq = new JSONObject(newReq);
			//caseId = jsonReq.getInt("CaseId");
			if ("new".equals(type)){
			    caseId = LeaveProc.newLeaveRequest(jsonReq);
			    //System.out.print("caseId:"+caseId);
			    if (caseId < 0) {
			    	jsonObj.put("state", 104);
					jsonObj.put("msg", "Wrong request!");
					return;
			    }
			} 
            if (LeaveProc.submLeaveRequest(caseId)){
    			jsonObj.put("state", 200);
    			jsonObj.put("CaseId", caseId);
				jsonObj.put("StatusID", "2");
		    } else {
		    	jsonObj.put("state", 104);
				jsonObj.put("msg", "Wrong request!");
		    }
				
   	    } catch (Exception e){
   	    	e.printStackTrace();
		}	
	}
	
    private void cancelLeave(Form form){
    	String caseId = form.getFirstValue("CaseId");
    	jsonObj = new JSONObject();
    	
    	try {
			if (LeaveProc.cancelLeaveRequest(Util.stringToInt(caseId))){
				jsonObj.put("state", 200);
    			jsonObj.put("CaseId", caseId);
				jsonObj.put("msg", "Case cancelled!");
			} else {
				jsonObj.put("state", 104);
    	    	jsonObj.put("msg", "case cancelled failed!");
			}
    	} catch (Exception e){
			e.printStackTrace();
		}
    }

    private void queryLeave(Form form){
    	String caseId = form.getFirstValue("CaseId");
    	
    	try {
			jsonObj = LeaveProc.getLeave(caseId);
			if (jsonObj == null) {
				jsonObj = new JSONObject();
				jsonObj.put("state", 104);
				jsonObj.put("msg", "case " + caseId + " doesn't exist!");				
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

    private void approveLeave(Form form){
    	String caseId = form.getFirstValue("CaseId");
    	jsonObj = new JSONObject();
    	
    	try{
    	    if (LeaveProc.approveLeaveRequest(Util.stringToInt(caseId))) {
    	    	jsonObj.put("state", 200);
    	    	jsonObj.put("CaseId", caseId);
				jsonObj.put("StatusID", "3");
    	    } else {
    	    	jsonObj.put("state", 104);
    	    	jsonObj.put("msg", "case approval failed!");
    	    }
    	} catch (Exception e){
   	    	e.printStackTrace();
		}
    }
    
    private void LeaveType(Form form){
    	String queryStr = "select LeaveTypeId,LeaveTypeName from leave_type where LeaveTypeId>3;";
    	
    	try {
    		jsonObj = EleaveDB.getJSONBySql(queryStr);
        	if (jsonObj == null) {
	    		jsonObj = new JSONObject();
		    	jsonObj.put("state", 104);
			    jsonObj.put("msg", "Get Leave Type Error!");				
		    }
    	} catch (Exception e){
			e.printStackTrace();
		}
    }
    
    private void queryLeaveMgr(Form form){
    	String eId = form.getFirstValue("EID");
    	jsonObj = new JSONObject();
    	String queryStr = "select distinct(CaseId),EmployeeId,LeaveTypeName,IssuedDate from view_case where EmployeeId in"
    			        + "(select EmployeeId from employee where LeaderId = (select employeeId from employee where EID= '"
    			        + eId + "'))";
    	
    	try {
    		jsonObj = EleaveDB.getJSONBySql(queryStr);
        	if (jsonObj == null) {
	    		jsonObj = new JSONObject();
		    	jsonObj.put("state", 104);
			    jsonObj.put("msg", "No requests found!");
		    }
    	} catch (Exception e){
			e.printStackTrace();
		}
    }
    
    private void toDoList(Form form){
    	String eId = form.getFirstValue("EID");
    	jsonObj = new JSONObject();
    	JSONObject rs = new JSONObject();
    	try {
    	    String queryStr = "select distinct(CaseId),EnglisthName,LeaveTypeName,IssuedDate from view_case where "
    			    + "StatusID = 2 and EID ='"+eId+"'";
    	    rs = EleaveDB.getJSONBySql(queryStr);
    	    jsonObj.put("Employee",rs);
    	    queryStr = "select distinct(CaseId),EnglisthName,LeaveTypeName,IssuedDate from view_case where "
    			     + "StatusID = 2 and EmployeeId in"
    			     + "(select EmployeeId from employee where LeaderId = (select employeeId from employee where EID= '"
    			     + eId + "'))";
    	    rs = EleaveDB.getJSONBySql(queryStr);
    	    jsonObj.put("Manager",rs);
    	} catch (Exception e){
			e.printStackTrace();
		}
    }

    private void rejectLeave(Form form){
    	String caseId = form.getFirstValue("CaseId");
    	jsonObj = new JSONObject();
    	
    	try {
			if (LeaveProc.rejectLeaveRequest(Util.stringToInt(caseId))){
				jsonObj.put("state", 200);
    			jsonObj.put("CaseId", caseId);
				jsonObj.put("msg", "Case rejected!");
			} else {
				jsonObj.put("state", 104);
    	    	jsonObj.put("msg", "case rejected failed!");
			}
    	} catch (Exception e){
			e.printStackTrace();
		}
    }
}

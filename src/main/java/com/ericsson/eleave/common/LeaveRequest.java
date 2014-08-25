package com.ericsson.eleave.common;

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
				if ("newleave".equals(action)) {
					newLeave(form);
				} else if ("queryleave".equals(action)) {
					queryLeave(form);
				} else if ("approveleave".equals(action)) {
					approveLeave(form);
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
		
	}
	
    private void queryLeave(Form form){
		
	}

    private void approveLeave(Form form){
	
    }

}

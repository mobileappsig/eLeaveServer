package com.ericsson.eleave.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;


/**
 * 
 * @author Karen Li
 *
 */
public class AppRouter extends Application{
	private static final Log logger = LogFactory.getLog("AppRouter.class");
	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		logger.info("router start");
		
		router.attach("/Users/{action}", Usermgmt.class);

		return router;
	}
}

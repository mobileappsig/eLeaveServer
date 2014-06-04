package com.ericsson.eleave.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sqlite.JDBC;

/**
 * 
 * @author Karen Li
 *
 */
public class EleaveDB {
	private static final Log logger = LogFactory.getLog("EleaveDB.class");
	private static Connection connection = null;
/*
	public EleaveDB(){
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:eleave.db");
			//connection = getConnection();
			if (connection == null) {
				logger.info("can not connect to database");
			} else {
				Statement statement = connection.createStatement();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/
	public static Connection getConnection() {
		if (connection == null) {
	    	try {
	    		Class.forName("com.mysql.jdbc.Driver");
	    		//String url = "jdbc:mysql://10.178.255.124:3306/eleave?useUnicode=true&characterEncoding=utf-8";
	    		String url = "jdbc:mysql://127.0.0.1:3306/eleave";
	    		String username = "root";
	    		String password = "";
		    	connection = DriverManager.getConnection(url,username,password);
				Statement statement = connection.createStatement();
				System.out.print("connect DB!");
		  	} catch (Exception e) {
				logger.info("can not connect to database");
				System.out.print("cannot connect DB!");
				e.printStackTrace();
			}
		} 
		return connection;
	}
	
}

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
				statement.executeUpdate("create table if not exists users(userid integer PRIMARY KEY autoincrement, username varchar(200) NOT NULL, userpasswd varchar(200) NOT NULL, logindate datetime default (datetime('now', 'localtime')));");
				statement.executeUpdate("insert into users(username,userpasswd) values('aaa','bbb');");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/
	public static Connection getConnection() {
		if (connection == null) {
	    	try {
	    		Class.forName("org.sqlite.JDBC");
		    	connection = DriverManager.getConnection("jdbc:sqlite:C:/EleaveApp/eleave.db");
				Statement statement = connection.createStatement();
				statement.executeUpdate("create table if not exists users(userid integer PRIMARY KEY autoincrement, username varchar(200) NOT NULL, userpasswd varchar(200) NOT NULL, logindate datetime default (datetime('now', 'localtime')));");
				statement.executeUpdate("insert into users(username,userpasswd) values('aaa','bbb');");
		  	} catch (Exception e) {
				logger.info("can not connect to database");
				e.printStackTrace();
			}
		} 
		return connection;
	}
	
}

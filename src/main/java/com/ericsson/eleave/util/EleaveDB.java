package com.ericsson.eleave.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * 
 * @author Karen Li
 *
 */
public class EleaveDB {
	private static final Log logger = LogFactory.getLog("EleaveDB.class");
	private static Connection connection = null;
	private static Statement statement = null;

    static {
		try {
			connection = getConnection();
			if (connection == null) {
				logger.info("can not connect to database!");
			} else {
				//statement = connection.createStatement();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static Connection getConnection() {
		if (connection == null) {
	    	try {
	    		Class.forName("com.mysql.jdbc.Driver");
	    		//String url = "jdbc:mysql://10.178.255.124:3306/eleave?useUnicode=true&characterEncoding=utf-8";
	    		String url = "jdbc:mysql://127.0.0.1:3307/eleave";
	    		//url = "jdbc:mysql://10.178.255.124:3306/eleave";
	    		String username = "root";
	    		String password = "adminadmin";
		    	connection = DriverManager.getConnection(url,username,password);
		    	connection.setAutoCommit(false);
				statement = connection.createStatement();
				System.out.print("connect DB!");
		  	} catch (Exception e) {
				logger.info("can not connect to database");
				System.out.print("cannot connect DB!");
				e.printStackTrace();
			}
		} 
		return connection;
	}
	
	public static int execSql(String sql){
		int rsnum = 0;
		try {
			logger.info("execute statement:"+sql);
			rsnum = statement.executeUpdate(sql);
		} catch(Exception e){
			e.printStackTrace();
			logger.debug(e);
		}
		return rsnum;
	}
	
	public static void transCommit() {
		try{
		    connection.commit();
		} catch (SQLException e1) {
			try {
			    connection.rollback();
			} catch (Exception e){
			    e.printStackTrace();
			    logger.debug(e);
		    }	
		}
	}
	
	public static void transRollback() {
		try {
		    connection.rollback();
		} catch (Exception e){
		    e.printStackTrace();
		    logger.debug(e);
	    }	
	}
	
	public static ResultSet getQueryRs(String sql){
		ResultSet result = null;
		try {
			logger.info("query statement:"+sql);
		    result = statement.executeQuery(sql);
		    result.last();
		    int cnt = result.getRow();
		    result.beforeFirst();
		    logger.info("getQueryRs return:"+cnt);
		} catch(Exception e){
			e.printStackTrace();
			logger.debug(e);
		}	
		return result;
	}
	
	public static ResultSet getPrepQueryRs(String sql, ArrayList<String> sqlParam){
		ResultSet result = null;
		try {
			logger.info("prep statement:"+sql);
			PreparedStatement ps = connection.prepareStatement(sql);
			if (sqlParam != null) {
				for (int i = 0; i < sqlParam.size(); i++)
				{
				ps.setString(i + 1, sqlParam.get(i));
				}
			}
			logger.info("prep statement:"+ps.toString());
		    result = ps.executeQuery();
		} catch(Exception e){
			e.printStackTrace();
			logger.debug(e);
		}	
		return result;
	}
	
	public static JSONObject getJSONByRs (ResultSet rs){
		JSONObject jsonObj = new JSONObject();
		ArrayList<JSONObject> resultList = new ArrayList<JSONObject>();
		if (rs == null) return null;
		try {
		    ResultSetMetaData rsmd = rs.getMetaData();   
	        int columnCount = rsmd.getColumnCount(); 
	        while (rs.next()){
	        	JSONObject record = new JSONObject();
	        	for (int i=1; i<=columnCount; i++){
	        		record.put(rsmd.getColumnName(i), rs.getString(i));
	        	}
	        	resultList.add(record);
	        }
	        if (resultList.isEmpty()){
	        	jsonObj = null;
	        } else{
	            jsonObj.put("ResultSet", resultList);
	        }
		} catch(Exception e){
			e.printStackTrace();
			logger.debug(e);
		}
		return jsonObj;
	}
	
	public static JSONObject getJSONBySql(String sql){
        JSONObject jsonObj = new JSONObject();
        jsonObj = getJSONByRs(getQueryRs(sql));
		return jsonObj;
	}
	
}

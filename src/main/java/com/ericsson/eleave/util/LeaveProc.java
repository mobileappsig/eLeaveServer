package com.ericsson.eleave.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;


public class LeaveProc {
	private static final Log logger = LogFactory.getLog("LeavePolicy.class");
	
	/**
	 * get StatutoryAnnualLeaveInThisYear, AdditionalAnnualLeaveInThisYear
	 * @param leaveType
	 * @param eId
	 * @return
	 */
    public static int getSALeave(String leaveType, String eId){
    	int retValue = 0;
    	ResultSet resultcond = null;
    	ResultSet resultset = null;
    	ArrayList<String> sqlParam = new ArrayList<String>();
    	sqlParam.add(eId);
    	
    	String queryStr = "select leave_policy.condition,leave_policy.Days from leave_policy,leave_type where "
    			        + "leave_type.LeaveTypeId = leave_policy.LeaveTypeId and leave_type.LeaveTypeName='"
    			        + leaveType + "'";
    	try {
			//logger.info("query policy:"+queryStr);
			resultcond = EleaveDB.getQueryRs(queryStr);
			resultcond.last();
		    int cnt = resultcond.getRow();
		    resultcond.beforeFirst();
			logger.info("after policy:"+cnt);
			while (resultcond.next()) {
				String cond = resultcond.getString("condition");
				resultset = EleaveDB.getPrepQueryRs(cond, sqlParam);
				if ((resultset.next()) && (resultset.getInt(1) > 0)) {
					retValue = resultcond.getInt("Days");
					break;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
    	return retValue;
    }
    
    public static JSONObject getLeaveInfo(String eId){
    	JSONObject jsonObj = new JSONObject();
    	ResultSet resultset = null;
    	/**
    	String queryStr = "select employee.EnglisthName,employee.FirstName,employee.LastName,employee.JobStartingYear,"
    			+ "employee.EmploymentDate,ifnull((select leave_taken_infor.TakenDays from leave_taken_infor "
    			+ "where (leave_taken_infor.ThisYear=0)),0) LastTakenDays, "
    			+ "ifnull((select sum(leave_case.LeaveDays) from leave_case where (leave_case.EmployeeId=employee.EmployeeId) and (month(leave_case.IssuedDate)<7)),0) ThisTakenFHYear,"
    			+ "ifnull((select sum(leave_case.LeaveDays) from leave_case where (leave_case.EmployeeId=employee.EmployeeId) and (month(leave_case.IssuedDate)>6)),0) ThisTakenSHYear,"
    			//+ "ifnull((select leave_taken_infor.TakenDays from leave_taken_infor where (leave_taken_infor.ThisYear=1)),0) ThisTakenDays, "
    			+ "(select month(now())>6) MidYear "
    			+ " from employee LEFT JOIN leave_taken_infor "
    			+ "on leave_taken_infor.EmployeeId = employee.EmployeeId "
    			+ "where employee.EID = '" + eId + "'";**/

    	int thisstat = getSALeave("StatutoryAnnualLeaveInThisYear",eId);//
    	int thistotl = getSALeave("AdditionalAnnualLeaveInThisYear",eId);
    	int thisaddi = thistotl - thisstat;//
    	int lasttotl = getSALeave("AnnualLeaveInLastYear",eId);
    	int lastleft = 0;//
    	int thistakestat = 0;//
    	int thistakeaddi = 0;//
    	int lasttake = 0;//
    	int remaindays = 0;
    	String queryStr = "select * from view_userinfo where view_userinfo.EID = '" + eId + "'";
    	
    	try {
			logger.info("query leave with:"+queryStr);
			resultset = EleaveDB.getQueryRs(queryStr);
			if (resultset.next()) {
				int lastTakenDays = resultset.getInt("LastTakenDays");
				lastleft = lasttotl - lastTakenDays;
				int thisTakenDaysFH = resultset.getInt("ThisTakenFHYear");
				int thisTakenDaysSH = resultset.getInt("ThisTakenSHYear");
				int thisTakenDays = resultset.getInt("thisTakenDays");
				if ((resultset.getInt("MidYear") > 0) && (thisTakenDaysFH < lastleft)) {
					lasttake = lastleft;
				} else {
					lasttake = thisTakenDaysFH + thisTakenDaysSH - thisTakenDays; 
				}
				remaindays = thisstat - thisTakenDays;
				if (remaindays > 0){
					thistakestat = thisTakenDays;
				} else {
					remaindays = remaindays + thisaddi;
					if (remaindays > 0){
						thistakeaddi = thisTakenDays - thisstat;
					} else {
						thistakestat = thisstat;
						thistakeaddi = thisaddi;
					}
				}
				System.out.printf("thisstat,thisaddi,lastleft,lasttake,thistakestat,thistakeaddi:%d,%d,%d,%d,%d,%d",
						           thisstat,thisaddi,lastleft,lasttake,thistakestat,thistakeaddi);

				jsonObj.put("EID", eId);
				jsonObj.put("EnglisthName", resultset.getString("EnglisthName"));
				jsonObj.put("FirstName", resultset.getString("FirstName"));
				jsonObj.put("LastName", resultset.getString("LastName"));
				jsonObj.put("EmploymentDate", resultset.getString("EmploymentDate"));
				jsonObj.put("JobStartingYear", resultset.getString("JobStartingYear"));
				jsonObj.put("ThisStat", thisstat);
				jsonObj.put("ThisAddi", thisaddi);
				jsonObj.put("LastLeft", lastleft);
				jsonObj.put("ThisTakeStat", thistakestat);
				jsonObj.put("ThisTakeAddi", thistakeaddi);
				jsonObj.put("LastTake", lasttake);
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
    	return jsonObj;
    }
    /**
	 * get StatutoryAnnualLeaveInThisYear, AdditionalAnnualLeaveInThisYear
	 * @param leaveType
	 * @param eId
	 * @return
	 */
    public int getLastALeave(String leaveType, String eId){
    	int retValue = 0;
    	ResultSet resultcond = null;
    	ResultSet resultset = null;
    	ArrayList<String> sqlParam = new ArrayList<String>();
    	sqlParam.add(eId);
    	
    	String queryStr = "select leave_policy.condition,leave_policy.Days from leave_policy,leave_type where "
    			        + "leave_type.LeaveTypeId = leave_policy.LeaveTypeId and leave_type.LeaveTypeName='"
    			        + leaveType + "'";
    	try {
			logger.info("query user with:"+queryStr);
			resultcond = EleaveDB.getQueryRs(queryStr);
			while (resultcond.next()) {
				String cond = resultcond.getString("condition");
				resultset = EleaveDB.getPrepQueryRs(cond, sqlParam);
				if (resultset.getInt(1) > 0) {
					retValue = resultcond.getInt("Days");
					break;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}	
    	return retValue;
    }
    
    public static JSONObject getLeave(String caseId){
    	JSONObject jsonObj = new JSONObject();
    	ArrayList<JSONObject> detailList = new ArrayList<JSONObject>();
    	String queryString = "SELECT CaseId,EmployeeId,LeaveTypeName,IssuedDate,LeaveDays,StatusName,StartDay,StopDay,"
    			           + "ifnull(HalfDayOrNot,'NULL') HalfDayOrNot,ifnull(AmOrPm,'NULL') AmOrPm"
		                   + " from view_case WHERE CaseId = '" + caseId + "'";
    	
    	try {
    		ResultSet rs = EleaveDB.getQueryRs(queryString);
    		if (rs == null) return null;
    		boolean caseFlag = true;
    		while (rs.next()){
    			if (caseFlag) {
        			jsonObj.put("CaseId", rs.getString("CaseId"));
        			jsonObj.put("EmployeeId", rs.getString("EmployeeId"));
    		    	jsonObj.put("LeaveTypeName", rs.getString("LeaveTypeName"));
    			    jsonObj.put("IssuedDate", rs.getString("IssuedDate"));
    			    jsonObj.put("LeaveDays", rs.getString("LeaveDays"));
        			jsonObj.put("StatusName", rs.getString("StatusName"));
        			caseFlag = false;
    			}
	        	JSONObject record = new JSONObject();
	        	record.put("StartDay", rs.getString("StartDay"));
	        	record.put("StopDay", rs.getString("StopDay"));
	        	record.put("HalfDayOrNot", rs.getString("HalfDayOrNot"));
	        	record.put("AmOrPm", rs.getString("AmOrPm"));
	        	detailList.add(record);
	        }
			jsonObj.put("LeaveDetail", detailList);
		} catch (Exception e){
			e.printStackTrace();
		}	
    	return jsonObj;
    }
    
    public static int newLeaveRequest(JSONObject jsonObj) {
    	JSONArray detailList = new JSONArray();
    	float ttlDays = 0;
    	int caseId = -1;
    	ArrayList<String> subSqls = new ArrayList<String>();

		try {
    	    String eid = jsonObj.getString("EID");
    	    String leavaTypeId = jsonObj.getString("LeavaTypeId");
    	    String issuedDate = jsonObj.getString("IssuedDate");
    	    detailList = jsonObj.getJSONArray("LeaveDetail");
    	    // calculate total days
    	    for (int i=0; i<detailList.length(); i++) {
    	    	JSONObject detailItem = detailList.getJSONObject(i);
    	    	String strStartDate = detailItem.getString("StartDay");
    	    	String strStopDate = detailItem.getString("StopDay");
    	    	String halfDay = detailItem.getString("HalfDayOrNot");
    	    	String amPm = detailItem.getString("AmOrPm");
    	    	String chkSql = "SELECT * from leave_case_detail where StartDay = '" + strStartDate + "' and '" + strStopDate + "' and AmOrPm ";
    	    	if ("NULL".equals(amPm)) {
    	    		chkSql += " is " + amPm;
    	    	} else {
    	    		chkSql += " = " + amPm;
    	    	}
    	    	ResultSet rs = EleaveDB.getQueryRs(chkSql);
    	    	if (rs.next()){
    	    		logger.info("Duplicate case:"+rs.getString("CaseId"));
    	    		return caseId;
    	    	}
    	    	String subSql = "INSERT INTO `leave_case_detail` VALUES (?,'"
 	    		       + strStartDate + "','" + strStopDate + "'," + halfDay + "," + amPm + ");";
    	    	subSqls.add(subSql);
    	    	Date startDate = Util.stringToDate(strStartDate);
    	    	Date stopDate = Util.stringToDate(strStopDate);
    	    	long day=(stopDate.getTime()-startDate.getTime())/(24*60*60*1000)+1;
    	    	if ("1".equals(halfDay)) {
    	    		ttlDays += day * 0.5;
    	    	} else {
    	    		ttlDays += day;
    	    	}
    	    	System.out.print("day:"+day+"ttlDays:"+ttlDays);
    	    }
    	    String mainSql = "INSERT INTO `leave_case` (EmployeeId,LeavaTypeId,IssuedDate,StatusID,LeaveDays) VALUES ("
    	    		       + eid + "," + leavaTypeId + ",'" + issuedDate + "',2," + ttlDays + ");";
    	    if (EleaveDB.execSql(mainSql)>0) {
    	    	String querySql = "SELECT LAST_INSERT_ID()";
    	    	ResultSet rs = EleaveDB.getQueryRs(querySql);
    	    	if (rs.next()){
    	    		caseId = rs.getInt(1);
    	    		System.out.print("caseId:"+caseId);
    	    		// insert leave_taken_infor
    	    		if ("1".equals(leavaTypeId) || "2".equals(leavaTypeId)) {
        	    		String infoSql = "SELECT * from leave_taken_infor where ThisYear = 1 and EmployeeId = " + eid;
        	    		ResultSet rs1 = EleaveDB.getQueryRs(infoSql);
        	    		if (rs1.next()){
        	    			infoSql = "UPDATE leave_taken_infor set TakenDays = TakenDays + " + ttlDays 
        	    					+ "where EmployeeId = " + eid + " and LeavaTypeId = " + leavaTypeId;
        	    		} else {
        	    			infoSql = "INSERT INTO leave_taken_infor VALUES (" + eid + "," + leavaTypeId +"," + ttlDays + ",1)";
        	    		}
        	    		if (EleaveDB.execSql(infoSql)<=0) {
        	    			EleaveDB.transRollback();
        	    		}
    	    		}
    	    		
    	    	    // insert leave_case_detail
    	    	    for (int i=0; i<subSqls.size(); i++) {
    	    	    	String subSql = subSqls.get(i);
    	    	    	String cid = ""+caseId;
    	    	    	subSql = subSql.replace("?", cid);
    	    	    	if (EleaveDB.execSql(subSql)<=0) {
        	    			EleaveDB.transRollback();
        	    		}
    	    	    }
    	    	    EleaveDB.transCommit();
    	    	}
    	    } else {
    	    	EleaveDB.transRollback();
    	    }
    	    
    	    
    	} catch (Exception e){
			e.printStackTrace();
		}
    	return caseId;
    }
    
    public static boolean approveLeaveRequest(String caseId) {
    	String updSql = "UPDATE leave_case set StatusID = 3 where CaseId = " + caseId;
    	if (EleaveDB.execSql(updSql)>0){
    		EleaveDB.transCommit();
    		return true;
    	}
    	return false;
    }
}

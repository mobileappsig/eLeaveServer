package com.ericsson.eleave.Logic;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date; 
import java.util.Calendar;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ericsson.eleave.util.EleaveDB;
import com.ericsson.eleave.util.Util;


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
    
/*    public static int newLeaveRequest(JSONObject jsonObj) {
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
    }*/

    public static int newLeaveRequest(JSONObject jsonObj) {
    	JSONArray detailList = new JSONArray();
    	float ttlDays = 0;
    	int caseId = -1;
    	ArrayList<String> subSqls = new ArrayList<String>();

		try {
    	    String eid = jsonObj.getString("EmployeeId");
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
    	    	logger.info("check duplicate:"+eid+","+strStartDate+","+strStopDate);
    	    	
    	    	String chkSql = "SELECT * from leave_case_detail where StartDay = '" + strStartDate + "' and '" + strStopDate + "' and AmOrPm ";
    	    	if ("NULL".equals(amPm)) {
    	    		chkSql += " is " + amPm;
    	    	} else {
    	    		chkSql += " = " + amPm;
    	    	}
    	    	chkSql += " and HalfDayOrNot = " + halfDay;
    	    	ResultSet rs = EleaveDB.getQueryRs(chkSql);
    	    	if (rs.next()){
    	    		logger.info("Duplicate case:"+rs.getString("CaseId"));
    	    		return caseId;
    	    	}
    	    	/*
    	    	if (!chkDupReq(eid,strStartDate,strStopDate,amPm,halfDay)) {
    	    		logger.info("Duplicate case:"+strStartDate+","+strStopDate);
    	    		return -1;
    	    	}*/
    	    	String subSql = "INSERT INTO `leave_case_detail` VALUES (?,'"
 	    		       + strStartDate + "','" + strStopDate + "'," + halfDay + "," + amPm + ");";
    	    	subSqls.add(subSql);
    	    }
    	    if (!chkLeftDays(eid,ttlDays)) {
    	    	logger.info("Days left not enough:"+eid+","+ttlDays);
	    		return -1;
    	    }
    	    String mainSql = "INSERT INTO `leave_case` (EmployeeId,LeavaTypeId,IssuedDate,StatusID,LeaveDays) VALUES ("
    	    		       + eid + "," + leavaTypeId + ",'" + issuedDate + "',1," + ttlDays + ");";
    	    if (EleaveDB.execSql(mainSql)>0) {
    	    	String querySql = "SELECT LAST_INSERT_ID()";
    	    	ResultSet rs = EleaveDB.getQueryRs(querySql);
    	    	if (rs.next()){
    	    		caseId = rs.getInt(1);
    	    		System.out.print("caseId:"+caseId);
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

    public static boolean submLeaveRequest(int cId) {
    	boolean submFlag = false;
    	
		try {
			String updSql = "UPDATE leave_case set StatusID = 2 where CaseId = " + cId;
			submFlag = EleaveDB.execSql(updSql)>0;
    	    if (submFlag){
    	    	EleaveDB.transCommit();
        	} else {
        		EleaveDB.transRollback();
        	}
    	        	    
    	} catch (Exception e){
			e.printStackTrace();
		}
    	return submFlag;
    }
    
    public static boolean setLeaveTakenInfo(int cId){
    	float ttlDays=0;
    	float[] ttlTknDays = new float[2];
    	String eid="";
    	boolean submFlag = false;
    	String qrySql = "select EmployeeId,CaseId,LeavaTypeId,StartDay,StopDay,HalfDayOrNot from view_case where CaseId = " + cId;
    	
		try {
			ResultSet rs = EleaveDB.getQueryRs(qrySql);
    	    // calculate total days
			while (rs.next()){
				eid = rs.getString("EmployeeId");
				Date startDate = rs.getDate("StartDay");
				Date stopDate = rs.getDate("StopDay");
    	    	String halfDay = rs.getString("HalfDayOrNot");
    	    	String leaveTypeId = rs.getString("LeavaTypeId");
    	    	float day=(stopDate.getTime()-startDate.getTime())/(24*60*60*1000)+1;

    	    	if ("1".equals(halfDay)) {
    	    		day = day * 0.5f;
    	    	} 
    	    	ttlDays += day;
    	    	if ("1".equals(leaveTypeId)) {
    	    		ttlTknDays[0] += day;
    	    	} else if ("2".equals(leaveTypeId)) {
    	    		ttlTknDays[1] += day;
    	    	}
    	    	System.out.print("day:"+day+"ttlDays:"+ttlDays);
    	    	submFlag = true;
    	    }
			if (!submFlag) return submFlag;
    	    String updSql = "UPDATE leave_case LeaveDays = " + ttlDays + " where CaseId = " + cId;
    	    if (EleaveDB.execSql(updSql)>0){
    	    	for (int i=1; i<3; i++){
    	    		String infoSql = "SELECT * from leave_taken_infor where ThisYear = 1 and EmployeeId = " + eid + " and LeavaTypeId = " + i;
    	    		ResultSet rs1 = EleaveDB.getQueryRs(infoSql);
    	    		if (rs1.next()){
    	    			infoSql = "UPDATE leave_taken_infor set TakenDays = TakenDays + " + ttlTknDays[i-1] 
    	    					+ "where EmployeeId = " + eid + " and LeavaTypeId = " + i;
    	    		} else {
    	    			infoSql = "INSERT INTO leave_taken_infor VALUES (" + eid + "," + i +"," + ttlDays + ",1)";
    	    		}
    	    		if (EleaveDB.execSql(infoSql)<=0) {
    	    			EleaveDB.transRollback();
    	    		}
	    		}
    	    	EleaveDB.transCommit();

        	} else {
        		EleaveDB.transRollback();
        	}
    	        	    
    	} catch (Exception e){
			e.printStackTrace();
		}
    	return submFlag;
    }
    
    public static boolean cancelLeaveRequest(int cId){
		EleaveDB.execSql("delete from leave_case_detail where CaseId = " + cId);
		EleaveDB.execSql("delete from leave_case where CaseId = " + cId);
		EleaveDB.transCommit();

		String qrySql = "select * from view_case where CaseId = " + cId;
    	if (EleaveDB.execSql(qrySql)>0){
    		return false;
    	}
    	
    	return true;
    }
    
    public static boolean approveLeaveRequest(int cId) {
    	String updSql = "UPDATE leave_case set StatusID = 3 where CaseId = " + cId;
    	boolean submFlag = (EleaveDB.execSql(updSql)>0) && setLeaveTakenInfo(cId);
    	if (submFlag) {
    		EleaveDB.transCommit();
    	} else {
    		EleaveDB.transRollback();
    	}
    	return submFlag;
    }
    
    public static boolean chkLeftDays(String eId, float days){
    	int takeDays = 0;
    	boolean daysOk = true;
    	int thistotl = getSALeave("AdditionalAnnualLeaveInThisYear",eId);
    	int lasttotl = getSALeave("AnnualLeaveInLastYear",eId);
    	String chkSql = "select StartDay,StopDay,HalfDayOrNot from view_case where EID='" + eId +"' and LeavaTypeId=3 and "
    			+ "Year(StopDay) = Year(now())";
    	try {
        	ResultSet rs = EleaveDB.getQueryRs(chkSql);
    	    while (rs.next()){
    		    Date startDate = rs.getDate("StartDay");
    		    Date stopDate = rs.getDate("StopDay");
    		    String halfDay = rs.getString("HalfDayOrNot");
    	    	if (startDate.getYear()<stopDate.getYear()){
    	    		startDate = getCurrYearFirst();
    	    	}
    	    	float day=(stopDate.getTime()-startDate.getTime())/(24*60*60*1000)+1;

    	    	if ("1".equals(halfDay)) {
    	    		day = day * 0.5f;
    	    	} 
    	    	takeDays += day;
    	    	
    	    }
    	    chkSql = "select TakenDays from leave_taken_infor,employee where leave_taken_infor.EID=employee.EID "
    	    		+ " and employee.EID='" + eId + "' and ThisYear=1";
    	    rs = EleaveDB.getQueryRs(chkSql);
    	    if (rs.next()){
    	    	takeDays += rs.getInt("TakenDays");
    	    }
    	    daysOk = (thistotl + lasttotl - takeDays - days) > 0;
    		
    	}catch (Exception e){
			e.printStackTrace();
		}
    	
    	return daysOk;
    }
    
    private static Date getCurrYearFirst(){  
        Calendar calendar = Calendar.getInstance(); 
        int year = calendar.get(Calendar.YEAR);
        calendar.clear();  
        calendar.set(Calendar.YEAR, year);  
        Date currYearFirst = calendar.getTime();  
        return currYearFirst;  
    }
    
    public static boolean chkDupReq(String eId, String sday, String eday, String ampm, String hday){
    	boolean reqOk = true;
    	String chksql = "select '" + eId + "' into @eid;";
    	chksql += "select '"+sday+"' into @sday;";
    	chksql += "select '"+eday+"' into @eday;";
    	chksql += "select "+ampm+" into @ampm;";
    	chksql += "select "+hday+" into @hday;";
    	chksql += "call check_leave(@eid,@sday,@eday,@hday,@ampm,@leck);";
    	chksql += "select @leck;";
    	logger.info("chksql:"+chksql);
    	try{
    	    ResultSet rs = EleaveDB.getQueryRs(chksql);
    	    if (rs.next()){
    	    	reqOk = (rs.getInt(1) == 1);
    	    }
    	} catch (Exception e){
			e.printStackTrace();
			reqOk = true;
		}
    	
    	return reqOk;
    }
}

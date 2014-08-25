package com.ericsson.eleave.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;


public class LeavePolicy {
	private static final Log logger = LogFactory.getLog("LeavePolicy.class");
	private Connection connection = null;
	
	LeavePolicy(){
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
    
    public static JSONObject getLeave(String eId){
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
}

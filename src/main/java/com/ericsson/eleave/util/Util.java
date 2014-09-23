package com.ericsson.eleave.util;

import java.text.SimpleDateFormat;
import java.text.DateFormat; 
import java.util.Date; 
import java.text.ParseException;

public class Util {
	public static Date stringToDate(String str) {  
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
        Date date = null;  
        try {  
            date = format.parse(str);   
        } catch (ParseException e) {  
            e.printStackTrace();  
        }  
                                              
        return date;  
    } 
	
	public static int stringToInt(String str) {
		return Integer.valueOf(str).intValue();
		
	}
}

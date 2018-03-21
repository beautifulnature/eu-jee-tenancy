package com.mhdanh.postgres.tenancy.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class MyUtil {
	
	private MyUtil() {}
	
	public static Date toDate(LocalDateTime date) {
		return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static LocalDateTime toLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault())
			      .toLocalDateTime();
	}
	
}

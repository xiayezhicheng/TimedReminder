package com.wanghao.timedreminderdemo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;


public class Alarm implements Serializable{
	public enum Day{
		SUNDAY,
		MONDAY,
		TUESDAY,
		WEDNESDAY,
		THURSDAY,
		FRIDAY,
		SATURDAY;

		@Override
		public String toString() {
			switch(this.ordinal()){
				case 0:
					return "星期日";
				case 1:
					return "星期一";
				case 2:
					return "星期二";
				case 3:
					return "星期三";
				case 4:
					return "星期四";
				case 5:
					return "星期五";
				case 6:
					return "星期六";
			}
			return super.toString();
		}
		
	}
	private static final long serialVersionUID = 8699489847426803799L;
	private int id;
	private Boolean alarmActive = true;
	private Calendar alarmTime = Calendar.getInstance();
	private Day[] days = {Day.MONDAY,Day.TUESDAY,Day.WEDNESDAY,Day.THURSDAY,Day.FRIDAY,Day.SATURDAY,Day.SUNDAY};	
	private String alarmName = "定时通知";
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Boolean getAlarmActive() {
		return alarmActive;
	}
	public void setAlarmActive(Boolean alarmActive) {
		this.alarmActive = alarmActive;
	}
	
	public Calendar getAlarmTime() {
		//?
		if (alarmTime.before(Calendar.getInstance()))
			alarmTime.add(Calendar.DAY_OF_MONTH, 1);
		while(!Arrays.asList(getDays()).contains(Day.values()[alarmTime.get(Calendar.DAY_OF_WEEK)-1])){
			alarmTime.add(Calendar.DAY_OF_MONTH, 1);			
		}
		return alarmTime;
	}
	public String getAlarmTimeString(){
		String time = "";
		if(alarmTime.get(Calendar.HOUR_OF_DAY)<=9) time += "0";
		time += String.valueOf(alarmTime.get(Calendar.HOUR_OF_DAY));
		time += ":";
		
		if (alarmTime.get(Calendar.MINUTE) <= 9)
			time += "0";
		time += String.valueOf(alarmTime.get(Calendar.MINUTE));
		
		return time;
	}
	public void setAlarmTime(Calendar alarmTime) {
		this.alarmTime = alarmTime;
	}
	public void setAlarmTime(String alarmTime) {

		String[] timePieces = alarmTime.split(":");

		Calendar newAlarmTime = Calendar.getInstance();
		newAlarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timePieces[0]));
		newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(timePieces[1]));
		newAlarmTime.set(Calendar.SECOND, 0);
		setAlarmTime(newAlarmTime);		
	
	}
	
	public Day[] getDays() {
		return days;
	}
	public void setDays(Day[] days) {
		this.days = days;
	}
	public void addDay(Day day){
		boolean contains = false;
		for(Day d:getDays()){
			if(d.equals(day))
				contains = true;
		}
		if(!contains){
			List<Day> result = new LinkedList<Day>();
			for(Day d:getDays()) result.add(d);
			result.add(day);
			setDays(result.toArray(new Day[result.size()]));
			
		}
	}
	public void removeDay(Day day) {
	    
		List<Day> result = new LinkedList<Day>();
	    for(Day d : getDays())
	        if(!d.equals(day))
	            result.add(d);
	    setDays(result.toArray(new Day[result.size()]));
	}
	
	public String getAlarmName() {
		return alarmName;
	}
	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}
	
	public String getRepeatDaysString() {
		StringBuilder daysStringBuilder = new StringBuilder();
		if(getDays().length == Day.values().length){
			daysStringBuilder.append("每天");		
		}else{
			Arrays.sort(getDays(), new Comparator<Day>() {
				@Override
				public int compare(Day lhs, Day rhs) {
					
					return lhs.ordinal() - rhs.ordinal();
				}
			});
			for(Day d : getDays()){
				daysStringBuilder.append(d.toString());		
				daysStringBuilder.append(',');
			}				
			daysStringBuilder.setLength(daysStringBuilder.length()-1);
		}
		return daysStringBuilder.toString();
	}
		
	public void schedule(Context context) {
		setAlarmActive(true);
		
		Intent myIntent = new Intent(context, AlarmAlertBroadcastReciever.class);
		myIntent.putExtra("alarm", this);
		//从系统取得一个用于向BroadcastReceiver的Intent广播的PendingIntent对象
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, myIntent,PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime().getTimeInMillis(), pendingIntent);					
	}
	//播报的时间存在十秒以上误差
	public String getTimeUntilNextAlarmMessage(){
		long timeDifference = getAlarmTime().getTimeInMillis() - System.currentTimeMillis();
		long days = timeDifference / (1000 * 60 * 60 * 24);
		long hours = timeDifference / (1000 * 60 * 60) - (days * 24);
		long minutes = timeDifference / (1000 * 60) - (days * 24 * 60) - (hours * 60);
		long seconds = timeDifference / (1000) - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60);
		String alert = "将会于 ";
		if (days > 0) {
			alert += String.format(
					"%d天%d小时%d分 %d秒", days,
					hours, minutes, seconds);
		} else {
			if (hours > 0) {
				alert += String.format("%d小时%d分 %d秒",
						hours, minutes, seconds);
			} else {
				if (minutes > 0) {
					alert += String.format("%d分%d秒", minutes,
							seconds);
				} else {
					alert += String.format("%d秒", seconds);
				}
			}
		}
		alert += "后通知您";
		return alert;
	}
	
}

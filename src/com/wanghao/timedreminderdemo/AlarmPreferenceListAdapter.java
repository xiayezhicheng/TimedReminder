package com.wanghao.timedreminderdemo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import com.wanghao.timedreminderdemo.AlarmPreference.Type;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class AlarmPreferenceListAdapter extends BaseAdapter implements Serializable{
	
	private Context context;
	private Alarm alarm;
	private List<AlarmPreference> preferences = new ArrayList<AlarmPreference>();
	private final String[] repeatDays = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};	
	
	private String[] alarmTones;
	private String[] alarmTonePaths;
	
	public AlarmPreferenceListAdapter(Context context,Alarm alarm) {
		setContext(context);
		setAlarm(alarm);
	}
	
	public Alarm getAlarm() {		
		for(AlarmPreference preference : preferences){
			switch(preference.getKey()){
				case ALARM_ACTIVE:
					alarm.setAlarmActive((Boolean) preference.getValue());
					break;
				case ALARM_NAME:
					alarm.setAlarmName((String) preference.getValue());
					break;
				case ALARM_TIME:
					alarm.setAlarmTime((String) preference.getValue());
					break;
				case ALARM_REPEAT:
					alarm.setDays((Alarm.Day[]) preference.getValue());
					break;
			}
		}
				
		return alarm;
	}

	public void setAlarm(Alarm alarm) {

		this.alarm = alarm;
		preferences.clear();
		preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_ACTIVE,"启用", null, null, alarm.getAlarmActive(),Type.BOOLEAN));
		preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_NAME, "标签",alarm.getAlarmName(), null, alarm.getAlarmName(), Type.STRING));
		preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_TIME, "时间",alarm.getAlarmTimeString(), null, alarm.getAlarmTime(), Type.TIME));
		preferences.add(new AlarmPreference(AlarmPreference.Key.ALARM_REPEAT, "重复",alarm.getRepeatDaysString(), repeatDays, alarm.getDays(),Type.MULTIPLE_LIST));
	}

	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}

	public String[] getRepeatDays() {
		return repeatDays;
	}


	public String[] getAlarmTones() {
		return alarmTones;
	}


	public String[] getAlarmTonePaths() {
		return alarmTonePaths;
	}


	@Override
	public int getCount() {
		return preferences.size();
	}

	@Override
	public Object getItem(int position) {
		return preferences.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		AlarmPreference alarmPreference = (AlarmPreference) getItem(position);
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		switch (alarmPreference.getType()) {
			case BOOLEAN:
				if(null == convertView || convertView.getId() != android.R.layout.simple_list_item_checked)
				convertView = layoutInflater.inflate(android.R.layout.simple_list_item_checked, null);
	
				CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
				checkedTextView.setText(alarmPreference.getTitle());
				checkedTextView.setChecked((Boolean) alarmPreference.getValue());
				break;
			case INTEGER:
			case STRING:
			case MULTIPLE_LIST:
			case TIME:
			default:
				if(null == convertView || convertView.getId() != android.R.layout.simple_list_item_2)
				convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
				
				TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
				text1.setTextSize(18);
				text1.setText(alarmPreference.getTitle());
				
				TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
				text2.setText(alarmPreference.getSummary());
				break;
		}

		return convertView;
	
	}

}

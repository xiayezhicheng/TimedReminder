package com.wanghao.timedreminderdemo;

import java.io.Serializable;
import java.util.Calendar;


import com.wanghao.timedreminderdemo.AlarmPreference.Key;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmPreferencesActivity extends Activity implements Serializable{
	
	private Alarm alarm;
	private ListAdapter listAdapter;
	private ListView listView;
	private Button btn_save,btn_cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_preferences);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey("alarm")) {
			setAlarm((Alarm) bundle.getSerializable("alarm"));
		} else {
			setAlarm(new Alarm());
		}
		if (bundle != null && bundle.containsKey("adapter")) {
			setListAdapter((AlarmPreferenceListAdapter) bundle.getSerializable("adapter"));
		} else {
			setListAdapter(new AlarmPreferenceListAdapter(this, getAlarm()));
		}
		
		btn_save = (Button)findViewById(R.id.btn_save);
		btn_save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Database.init(getApplicationContext());
				if (getAlarm().getId() < 1) {
					Database.create(getAlarm());
				} else {
					Database.update(getAlarm());
				}
				callMathAlarmScheduleService();
				Toast.makeText(AlarmPreferencesActivity.this, getAlarm().getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG).show();
				finish();
			}

		});
		btn_cancel = (Button)findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(AlarmPreferencesActivity.this);
				dialog.setTitle("ɾ��");
				dialog.setMessage("ɾ���������?");
				dialog.setPositiveButton("ɾ��", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						Database.init(getApplicationContext());
						if (getAlarm().getId() < 1) {
							// Alarm not saved
						} else {
							Database.deleteEntry(alarm);
							callMathAlarmScheduleService();
						}
						finish();
					}
				});
				dialog.setNegativeButton("ȡ��", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final AlarmPreferenceListAdapter alarmPreferenceListAdapter = (AlarmPreferenceListAdapter) getListAdapter();
				final AlarmPreference alarmPreference = (AlarmPreference) alarmPreferenceListAdapter.getItem(position);
			
				AlertDialog.Builder alert;
				view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
				switch (alarmPreference.getType()) {
					case BOOLEAN:
						CheckedTextView checkedTextView = (CheckedTextView) view;
						boolean checked = !checkedTextView.isChecked();
						((CheckedTextView) view).setChecked(checked);
						switch (alarmPreference.getKey()) {
						case ALARM_ACTIVE:
							alarm.setAlarmActive(checked);
							break;
						}
						alarmPreference.setValue(checked);
						break;
					case STRING:
	
						alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
	
						alert.setTitle(alarmPreference.getTitle());
						
						final EditText input = new EditText(AlarmPreferencesActivity.this);
	
						input.setText(alarmPreference.getValue().toString());
	
						alert.setView(input);
						alert.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
	
								alarmPreference.setValue(input.getText().toString());
	
								if (alarmPreference.getKey() == Key.ALARM_NAME) {
									alarm.setAlarmName(alarmPreference.getValue().toString());
								}
	
								alarmPreferenceListAdapter.setAlarm(getAlarm());
								alarmPreferenceListAdapter.notifyDataSetChanged();
							}
						});
						alert.show();
						break;
					case MULTIPLE_LIST:
						alert = new AlertDialog.Builder(AlarmPreferencesActivity.this);
	
						alert.setTitle(alarmPreference.getTitle());
						// alert.setMessage(message);
	
						CharSequence[] multiListItems = new CharSequence[alarmPreference.getOptions().length];
						for (int i = 0; i < multiListItems.length; i++)
							multiListItems[i] = alarmPreference.getOptions()[i];
	
						boolean[] checkedItems = new boolean[multiListItems.length];
						for (Alarm.Day day : getAlarm().getDays()) {
							checkedItems[day.ordinal()] = true;
						}
						alert.setMultiChoiceItems(multiListItems, checkedItems, new OnMultiChoiceClickListener() {
	
							@Override
							public void onClick(final DialogInterface dialog, int which, boolean isChecked) {
	
								Alarm.Day thisDay = Alarm.Day.values()[which];
	
								if (isChecked) {
									alarm.addDay(thisDay);
								} else {
									// Only remove the day if there are more than 1
									// selected
									if (alarm.getDays().length > 1) {
										alarm.removeDay(thisDay);
									} else {
										// If the last day was unchecked, re-check
										// it
										((AlertDialog) dialog).getListView().setItemChecked(which, true);
									}
								}
	
							}
						});
						alert.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								alarmPreferenceListAdapter.setAlarm(getAlarm());
								alarmPreferenceListAdapter.notifyDataSetChanged();
	
							}
						});
						alert.show();
						break;
					case TIME:
						TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmPreferencesActivity.this, new OnTimeSetListener() {
	
							@Override
							public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
								Calendar newAlarmTime = Calendar.getInstance();
								newAlarmTime.set(Calendar.HOUR_OF_DAY, hours);
								newAlarmTime.set(Calendar.MINUTE, minutes);
								newAlarmTime.set(Calendar.SECOND, 0);
								alarm.setAlarmTime(newAlarmTime);
								alarmPreferenceListAdapter.setAlarm(getAlarm());
								alarmPreferenceListAdapter.notifyDataSetChanged();
							}
						}, alarm.getAlarmTime().get(Calendar.HOUR_OF_DAY), alarm.getAlarmTime().get(Calendar.MINUTE), true);
						timePickerDialog.setTitle(alarmPreference.getTitle());
						timePickerDialog.show();
					default:
						break;
					}
			}
		});
	}

	public Alarm getAlarm() {
		return alarm;
	}
	public void setAlarm(Alarm alarm) {
		this.alarm = alarm;
	}
	public ListAdapter getListAdapter() {
		return listAdapter;
	}
	public void setListAdapter(ListAdapter listAdapter) {
		this.listAdapter = listAdapter;
		getListView().setAdapter(listAdapter);
	}
	public ListView getListView() {
		if (listView == null)
			listView = (ListView) findViewById(R.id.preference_list);
		return listView;
	}
	public void setListView(ListView listView) {
		this.listView = listView;
	}
	
	private void callMathAlarmScheduleService() {
		Intent serviceIntent = new Intent(this, AlarmService.class);
		this.startService(serviceIntent);
	}
}

package com.wanghao.timedreminderdemo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

@SuppressLint("ServiceCast")
public class AlarmAlertBroadcastReciever extends BroadcastReceiver{

	NotificationManager notificationManager;
	private static int id = 1;
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, AlarmService.class);
		context.startService(serviceIntent);
		
		StaticWakeLock.lockOn(context);
		
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, new Intent(context,AlarmActivity.class),
								PendingIntent.FLAG_CANCEL_CURRENT);
		
		builder.setContentIntent(pIntent)
				.setContentTitle(context.getString(R.string.notify_title))
				.setContentText(context.getString(R.string.notify_content))
				.setDefaults(Notification.DEFAULT_ALL)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setTicker(context.getString(R.string.notify_title));
		notificationManager.notify(id++, builder.build());
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				StaticWakeLock.lockOff(context);
			}
		}, 3000);
	}

}

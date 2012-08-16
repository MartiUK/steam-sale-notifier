package com.jzelinskie.ssn;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class UpdateService extends Service {

	private static final String TAG = "UpdateService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		new UpdateTask().execute();
		this.stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	private class UpdateTask extends AsyncTask<Void, Integer, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> gamesOnSale = new ArrayList<String>();
			DBHandler dbh = new DBHandler(UpdateService.this);
			List<Game> games = dbh.getAllGames();
			for (Game game : games) {
				try {
					game.update();
					dbh.updateGame(game);
					if (game.sale())
						gamesOnSale.add(game.title());
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
			return gamesOnSale;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(ArrayList<String> gamesOnSale) {
			if (gamesOnSale.size() != 0) {
				String contentText = gamesOnSale.toString().substring(1, gamesOnSale.toString().length() - 1);
				Context context = UpdateService.this.getApplicationContext();
				Intent notificationIntent = new Intent(context,
						MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 0, notificationIntent, 0);

				NotificationManager notificationManager = (NotificationManager) context
						.getSystemService(NOTIFICATION_SERVICE);
				Notification updateComplete = new Notification();
				updateComplete.icon = R.drawable.ic_launcher;
				updateComplete.tickerText = context
						.getText(R.string.notification_title);
				updateComplete.when = System.currentTimeMillis();
				updateComplete.flags = Notification.FLAG_AUTO_CANCEL;
				updateComplete.defaults = Notification.DEFAULT_ALL;

				// Required for Android < 3.0
				updateComplete.setLatestEventInfo(context,
						updateComplete.tickerText, contentText, contentIntent);

				notificationManager.notify(69, updateComplete);
			}
		}
	}

}

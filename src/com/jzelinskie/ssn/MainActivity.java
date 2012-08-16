package com.jzelinskie.ssn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends ListActivity {

	private static final String TAG = "MainActivity";

	private DBHandler dbh = new DBHandler(this);
	private List<Game> gameList;
	private SimpleAdapter gamesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRecurringAlarm(MainActivity.this);

		TextView emptyList = (TextView) findViewById(R.id.emptyList);
		ListView listView = getListView();
		listView.setEmptyView(emptyList);
		registerForContextMenu(listView);
		fillData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			searchAndAdd();
			return true;
		case R.id.menu_import:
			Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menu_clear:
			removeAll();
			return true;
		case R.id.menu_refresh:
			new UpdateAllTask().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Game g = gameList.get(info.position);

		switch (item.getItemId()) {
		case R.id.context_update:
			new UpdateTask().execute(g);
			return true;
		case R.id.context_url:
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(g.url()));
			startActivity(i);
			return true;
		case R.id.context_remove:
			new RemoveTask().execute(g);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void setRecurringAlarm(Context context) {
		Calendar updateTime = Calendar.getInstance();
		updateTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		updateTime.set(Calendar.HOUR_OF_DAY, 18);
		updateTime.set(Calendar.MINUTE, 15);

		Intent updater = new Intent(context, UpdateReceiver.class);
		PendingIntent recurringUpdate = PendingIntent.getBroadcast(context, 0,
				updater, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarms.cancel(recurringUpdate);
		alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				recurringUpdate);
	}

	private void fillData() {
		ArrayList<HashMap<String, String>> gamesHash = new ArrayList<HashMap<String, String>>();
		gameList = dbh.getAllGames();

		for (Game game : gameList) {
			HashMap<String, String> gameHash = new HashMap<String, String>();
			gameHash.put("gameTitle", game.title());
			gameHash.put("lastUpdated", game.updated().toString());
			if (game.sale()) {
				gameHash.put("gamePrice", game.price());
				gameHash.put("salePrice", game.salePrice());
			} else {
				gameHash.put("gamePrice", game.price());
				gameHash.put("salePrice", "");
			}
			gamesHash.add(gameHash);
		}

		String[] from = new String[] { "gameTitle", "gamePrice", "salePrice",
				"lastUpdated" };
		int[] to = new int[] { R.id.game_title, R.id.game_price,
				R.id.game_sale_price, R.id.game_updated };
		gamesAdapter = new SimpleAdapter(MainActivity.this, gamesHash,
				R.layout.activity_main_row, from, to);
		setListAdapter(gamesAdapter);
	}

	private class UpdateTask extends AsyncTask<Game, Integer, Boolean> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Updating...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Game... params) {
			try {
				params[0].update();
				dbh.updateGame(params[0]);
				return true;
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			dialog.dismiss();
			fillData();
			if (!success) {
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.error_network),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private class UpdateAllTask extends AsyncTask<Void, Integer, Boolean> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Updating...");
			dialog.show();
		}

		//
		@Override
		protected Boolean doInBackground(Void... params) {
			List<Game> games = dbh.getAllGames();
			for (Game game : games) {
				try {
					game.update();
					dbh.updateGame(game);
				} catch (Exception e) {
					Log.e(TAG, e.toString());
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			dialog.dismiss();
			fillData();
			if (!success) {
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.error_network),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class RemoveTask extends AsyncTask<Game, Integer, Void> {

		@Override
		protected Void doInBackground(Game... params) {
			dbh.deleteGame(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			fillData();
		}

	}

	private void removeAll() {
		new AlertDialog.Builder(this).setMessage(R.string.dialog_removeall_msg)
				.setCancelable(true)
				.setPositiveButton("Yes", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						new RemoveAllTask().execute();
					}
				}).setNegativeButton("No", null).show();
	}

	private class RemoveAllTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			dbh.deleteAllGames();
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			fillData();
		}
	}

	private void searchAndAdd() {
		final EditText input = new EditText(this);
		new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.dialog_add_title))
				.setMessage(getResources().getString(R.string.dialog_add_msg))
				.setView(input)
				.setPositiveButton("Search", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String searchTerm = input.getText().toString();
						new SearchTask().execute(searchTerm);
					}
				}).setNegativeButton("Cancel", null).show();
	}

	private class SearchTask extends
			AsyncTask<String, Integer, ArrayList<Game>> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setMessage("Searching...");
			dialog.show();
		}

		@Override
		protected ArrayList<Game> doInBackground(String... params) {
			try {
				Search search = new Search(params[0]);
				return search.results();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
				return null;
			}
		}

		@Override
		protected void onPostExecute(final ArrayList<Game> results) {
			dialog.dismiss();
			if (results != null) {
				ArrayList<String> resultStrings = new ArrayList<String>();

				for (Game g : results) {
					resultStrings.add(g.title());
				}

				String[] items = resultStrings.toArray(new String[resultStrings
						.size()]);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle("Select the game to monitor");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Game selected = results.get(which);
						for (Game g : gameList) {
							if (selected.title().equals(g.title())) {
								Toast.makeText(
										MainActivity.this,
										getResources().getString(
												R.string.error_duplicate),
										Toast.LENGTH_SHORT).show();
								return;
							}
						}

						new AddTask().execute(selected);
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			} else {
				Toast.makeText(MainActivity.this,
						getResources().getString(R.string.error_network),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class AddTask extends AsyncTask<Game, Integer, Void> {

		@Override
		protected Void doInBackground(Game... params) {
			dbh.addGame(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			fillData();
		}
	}
}

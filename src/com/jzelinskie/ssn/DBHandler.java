package com.jzelinskie.ssn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "ssnGames";
	private static final String TABLE_GAMES = "games";

	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_URL = "url";
	private static final String KEY_PRICE = "stock_price";
	private static final String KEY_SALE_PRICE = "sale_price";
	private static final String KEY_UPDATED = "updated";
	private static final String[] ALL_KEYS = new String[] { KEY_ID, KEY_TITLE,
			KEY_URL, KEY_PRICE, KEY_SALE_PRICE, KEY_UPDATED };

	public DBHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_GAMES + "("
				+ KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE
				+ " TEXT NOT NULL, " + KEY_URL + " TEXT NOT NULL, " + KEY_PRICE
				+ " TEXT NOT NULL, " + KEY_SALE_PRICE + " TEXT NOT NULL, "
				+ KEY_UPDATED + " TEXT NOT NULL" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	private Game gameFromCursor(Cursor c) throws ParseException {
		String title = c.getString(1);
		String url = c.getString(2);
		String price = c.getString(3);
		String salePrice = c.getString(4);
		Date updated = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
				.parse(c.getString(5));
		return new Game(title, url, price, salePrice, updated);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
		onCreate(db);
	}

	public void addGame(Game game) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, game.title());
		values.put(KEY_URL, game.url());
		values.put(KEY_PRICE, game.price());
		values.put(KEY_SALE_PRICE, game.salePrice());
		values.put(KEY_UPDATED, game.updated().toString());

		db.insert(TABLE_GAMES, null, values);
		db.close();
	}

	public Game getGame(String title) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_GAMES, ALL_KEYS, KEY_TITLE + " = ?",
				new String[] { title }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		try {
			Game game = gameFromCursor(cursor);
			return game;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Game> getAllGames() {
		List<Game> gameList = new ArrayList<Game>();
		String selectQuery = "SELECT  * FROM " + TABLE_GAMES;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		if (cursor.moveToFirst()) {
			do {
				try {
					Game game = gameFromCursor(cursor);
					gameList.add(game);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
		}

		return gameList;
	}

	public void updateGame(Game game) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, game.title());
		values.put(KEY_URL, game.url());
		values.put(KEY_PRICE, game.price());
		values.put(KEY_SALE_PRICE, game.salePrice());
		values.put(KEY_UPDATED, game.updated().toString());

		db.update(TABLE_GAMES, values, KEY_TITLE + " = ?",
				new String[] { game.title() });
	}

	public void deleteGame(Game game) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_GAMES, KEY_TITLE + " = ?",
				new String[] { game.title() });
		db.close();
	}

	public void deleteAllGames() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_GAMES, null, null);
		db.close();
	}

	public int getGamesCount() {
		String countQuery = "SELECT  * FROM " + TABLE_GAMES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();
	}
}
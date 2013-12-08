package at.eht13.bls.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "swe02.db";

	public DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		StringBuilder query = new StringBuilder(
				"CREATE TABLE IF NOT EXISTS trainings (");
		query.append("id INTEGER PRIMARY KEY, ");
		query.append("duration INTEGER, ");
		query.append("quality INTEGER, ");
		query.append("endtime INTEGER");
		query.append(");");

		db.execSQL(query.toString());

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		/*
		 * In this method you can compare the old and new version and make
		 * changes to the database if required.
		 */

	}

}

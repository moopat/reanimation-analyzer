package at.eht13.bls.db;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import at.eht13.bls.model.TrainingResult;

public class TrainingResultDAO {
	
	private static DatabaseOpenHelper doh;
	
	public static void init(Context context){
		if(doh == null){
			doh = new DatabaseOpenHelper(context);
		}
	}
	
	public static int insert(TrainingResult tr){
		int lastid = -1;
		
		SQLiteDatabase db = doh.getWritableDatabase();
		
		ContentValues cv = new ContentValues();
		cv.put("duration", tr.getDuration());
		cv.put("quality", tr.getQuality());
		cv.put("endtime", tr.getDate().getTime());
		
		try {
			db.beginTransaction();
			lastid = (int) db.insert("trainings", null, cv);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		
		return lastid;
	}
	
	public static ArrayList<TrainingResult> getAllTrainings(){
		ArrayList<TrainingResult> result = new ArrayList<TrainingResult>();
		
		SQLiteDatabase db = doh.getReadableDatabase();
		Cursor cursor = null;
		
		try {
			cursor = db.query("trainings", null, null, null, null, null, "endtime DESC");
			
			int idIndex = cursor.getColumnIndex("id");
			int durationIndex = cursor.getColumnIndex("duration");
			int qualityIndex = cursor.getColumnIndex("quality");
			int endtimeIndex = cursor.getColumnIndex("endtime");
			
			cursor.moveToFirst();
			
			do {
				TrainingResult tr = new TrainingResult();
				tr.setId(cursor.getInt(idIndex));
				tr.setDuration(cursor.getInt(durationIndex));
				tr.setQuality(cursor.getInt(qualityIndex));
				tr.setDate(new Date(cursor.getLong(endtimeIndex)));
				
				result.add(tr);
				cursor.moveToNext();
			} while (!cursor.isAfterLast());
		} catch (Exception e) {
			// Something went wrong
		} finally {
			if(cursor != null){
				cursor.close();
			}
			db.close();
		}
		
		return result;
	}
	
	public static void deleteAll(){
		SQLiteDatabase db = doh.getWritableDatabase();
		db.delete("trainings", null, null);
	}
	
	public static void delete(TrainingResult tr){
		SQLiteDatabase db = doh.getWritableDatabase();
		db.delete("trainings", "id = ?", new String[]{String.valueOf(tr.getId())});
	}

}

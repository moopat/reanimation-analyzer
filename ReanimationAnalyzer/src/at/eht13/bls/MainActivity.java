package at.eht13.bls;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.webservice.RateFetcher;
import at.eht13.webservice.RateFetcher.OnRateFetchedListener;
import at.eht13.webservice.RateFetcherTask;

/* author:
 * Christiane Prutsch, Markus Deutsch, Clemens Kaar
 * 17.12.2013
 */
public class MainActivity extends Activity implements OnClickListener, OnRateFetchedListener {

	public static final int STATE_IDLE = 1;
	public static final int STATE_RUNNING = 2;

	// ui control elements
	private RateFetcherTask rateFetcher;
	private Button button;
	private TextView intro;
	
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get ui control elements
		intro = (TextView) findViewById(R.id.intro);
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		
		// init shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		updateText();
	}
	
	public void onResume(){
		super.onResume();
		updateRate();
	}
	
	// update compression rate from web service
	private void updateRate(){
		RateFetcher.defaultValue = prefs.getInt("RATE", getResources().getInteger(R.integer.default_rate));
		rateFetcher = new RateFetcherTask(this);
		rateFetcher.execute();
	}
	
	// update compression rate in info text
	private void updateText(){
		intro.setText(getString(R.string.introduction, prefs.getInt("RATE", getResources().getInteger(R.integer.default_rate))));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// switch to reanimation screen
		if (button.isPressed()) {
			Intent aintent = new Intent(this, ReanimationActivity.class);
			startActivityForResult(aintent, 1001);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// show result list after reanimation
		if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(this, ResultListActivity.class);
			intent.putExtra("highlightLast", true);
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle inputs on the action bar items
		switch (item.getItemId()) {
		case R.id.action_reset:
			TrainingResultDAO.deleteAll();
			return true;
		case R.id.action_results:
			Intent intent = new Intent(this, ResultListActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_info:
			Intent iintent = new Intent(this, InfoActivity.class);
			startActivity(iintent);
			return true;
		case R.id.action_rate:
			updateRate();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onRateFetched(int rate) {
		// new rate fetched from web service
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("RATE", rate);
		editor.commit();
		
		updateText();
	}

}

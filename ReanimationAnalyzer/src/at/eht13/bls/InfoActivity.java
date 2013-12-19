package at.eht13.bls;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.support.v4.app.NavUtils;

/* author:
 * Christiane Prutsch, Markus Deutsch, Clemens Kaar
 * 17.12.2013
 */
public class InfoActivity extends Activity implements OnClickListener {
	
	private Button feedback, info, app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		setupActionBar();
		
		// get ui control elements
		feedback = (Button) findViewById(R.id.feedback);
		info = (Button) findViewById(R.id.firstaidInfo);
		app = (Button) findViewById(R.id.firstaidApp);
		
		// on click listener
		feedback.setOnClickListener(this);
		info.setOnClickListener(this);
		app.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// detect, which button was pressed
		if(feedback.isPressed()){
			// mail intent
			Intent mailintent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","support@moop.at", null));
			mailintent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			startActivity(Intent.createChooser(mailintent, "Feedback senden"));
		} else if (info.isPressed()) {
			// browser intent
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.roteskreuz.at/site/erste-hilfe/erste-hilfe-kurse/"));
			startActivity(browserIntent);
		} else if (app.isPressed()) {
			// play store intent
			Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=at.fh.firstaid"));
			startActivity(storeIntent);
		}
		
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// switch back to home screen
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

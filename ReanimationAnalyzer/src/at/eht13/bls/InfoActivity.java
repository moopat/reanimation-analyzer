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

public class InfoActivity extends Activity implements OnClickListener {
	
	private Button feedback, info, app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		setupActionBar();
		
		feedback = (Button) findViewById(R.id.feedback);
		info = (Button) findViewById(R.id.firstaidInfo);
		app = (Button) findViewById(R.id.firstaidApp);
		
		feedback.setOnClickListener(this);
		info.setOnClickListener(this);
		app.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		if(feedback.isPressed()){
			Intent mailintent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","support@moop.at", null));
			mailintent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
			startActivity(Intent.createChooser(mailintent, "Feedback senden"));
		} else if (info.isPressed()) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.roteskreuz.at/site/erste-hilfe/erste-hilfe-kurse/"));
			startActivity(browserIntent);
		} else if (app.isPressed()) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=at.fh.firstaid"));
			startActivity(browserIntent);
		}
		
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

package at.eht13.bls;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import at.eht13.bls.db.TrainingResultDAO;

public class MainActivity extends Activity implements OnClickListener {

	public static final int STATE_IDLE = 1;
	public static final int STATE_RUNNING = 2;

	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {

		if (button.isPressed()) {
			Intent aintent = new Intent(this, ReanimationActivity.class);
			startActivityForResult(aintent, 1001);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
			Intent intent = new Intent(this, ResultListActivity.class);
			intent.putExtra("highlightLast", true);
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}

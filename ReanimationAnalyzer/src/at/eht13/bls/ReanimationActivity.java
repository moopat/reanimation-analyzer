package at.eht13.bls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

public class ReanimationActivity extends Activity implements
		OnSeekBarChangeListener, OnChronometerTickListener, SensorEventListener {

	private Chronometer chronometer;
	private ImageView indicator;
	private RelativeLayout container;
	private SeekBar unlocker;

	private long startTime;
	private long endTime;

	private int currentProgress = 0;

	private SensorManager sensorMan;
	private Sensor accelerometer;

	private float[] mGravity;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;

	private int compressionCnt;
	private Date lastCompressionTime;
	private long optimumRate;
	private List<Long> results;
	private int nrResultsForAvgCalc;
	private double stepSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reanimation);

		container = (RelativeLayout) findViewById(R.id.indicator_container);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		indicator = (ImageView) findViewById(R.id.indicator);
		unlocker = (SeekBar) findViewById(R.id.unlocker);

		unlocker.setOnSeekBarChangeListener(this);
		chronometer.setOnChronometerTickListener(this);

		TrainingResultDAO.init(getApplicationContext());

		sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;

		optimumRate = 600;
		results = new ArrayList<Long>();
		nrResultsForAvgCalc = 3;
		stepSize = 100.0 / optimumRate;

		startTraining();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void startTraining() {
		compressionCnt = 0;
		lastCompressionTime = new Date();

		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
		startTime = new Date().getTime();
	}

	private void stopTraining() {
		chronometer.stop();
		endTime = new Date().getTime();

		float duration = (float) ((int) (endTime - startTime)) / 1000;

		TrainingResult tr = new TrainingResult();
		tr.setDate(new Date());
		tr.setQuality((new Random().nextInt(3)) + 1);
		tr.setDuration((int) Math.ceil(duration));

		TrainingResultDAO.insert(tr);

		finish();
	}

	/**
	 * Every second the quality indicator adapts his value according to the
	 * current quality.
	 * 
	 * @TODO: get the current compression rate, or even better, get the
	 *        percentage of how far the user is off (-100% is too slow, 0 is
	 *        correct, 100% is much too fast). Right now 50 is the best result
	 *        (places marker in the middle), while 0 and 100 are the worst cases
	 *        (placing the marker at the appropriate end).
	 */
	@Override
	public void onChronometerTick(Chronometer chronometer) {
		/*
		 * Random random = new Random(); final int efficiency =
		 * random.nextInt(600); final int newLeftMargin = (int)
		 * (container.getHeight() * efficiency / 600) - indicator.getWidth() /
		 * 2; final int oldValue = ((MarginLayoutParams)
		 * indicator.getLayoutParams()).topMargin;
		 * 
		 * Animation a = new Animation() {
		 * 
		 * @Override protected void applyTransformation(float interpolatedTime,
		 * Transformation t) { MarginLayoutParams params = (MarginLayoutParams)
		 * indicator .getLayoutParams(); params.topMargin = oldValue + (int)
		 * ((newLeftMargin - oldValue) * interpolatedTime);
		 * indicator.setLayoutParams(params); } };
		 * 
		 * a.setDuration(1000); indicator.startAnimation(a);
		 */
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			if ((progress > (currentProgress + 24))
					|| (progress < (currentProgress - 24))) {
				seekBar.setProgress(currentProgress);
			} else {
				currentProgress = progress;
			}
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.getProgress() == 100) {
			stopTraining();
		} else {
			/**
			 * For a smoother experience, only reset the seek bar if the user
			 * did not stop traning.
			 */
			seekBar.setProgress(0);
			currentProgress = 0;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorMan.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorMan.unregisterListener(this);
	}

	private void addCompressionTime(Long time) {
		long optimumRateHalf = optimumRate / 2;

		if (time < optimumRateHalf)
			time = optimumRateHalf;

		if (time > optimumRate + optimumRateHalf)
			time = optimumRate + optimumRateHalf;

		results.add(time);
	}

	private float calculateMedian() {
		List<Long> resultsForAvgCalc = new ArrayList<Long>();

		int resultsSize = results.size();
		
		int n = nrResultsForAvgCalc;

		if (resultsSize < nrResultsForAvgCalc)
			n = results.size();

		for (int i = 0; i < n; i++) {
			resultsForAvgCalc.add(results.get(resultsSize - 1 - i));
		}

		if (resultsForAvgCalc.size() == 1)
			return resultsForAvgCalc.get(0);
		
		Collections.sort(resultsForAvgCalc);

		int m = resultsForAvgCalc.size() / 2;

		if (resultsForAvgCalc.size() % 2 == 0) {
			return (resultsForAvgCalc.get(m) + resultsForAvgCalc.get(m - 1)) / 2;
		}

		return resultsForAvgCalc.get(m);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravity = event.values.clone();

			// compression detection
			float z = mGravity[2];

			mAccelLast = mAccelCurrent;
			mAccelCurrent = z;
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta;

			// Make this higher or lower according to how much
			// compression you want to detect
			if (mAccel > 2.5) {
				Date currentTime = new Date();

				long timeDiff = currentTime.getTime() - lastCompressionTime.getTime();

				if (timeDiff > 300) {
					addCompressionTime(timeDiff);

					double medianNormalized = (calculateMedian() - (optimumRate / 2)) * stepSize;
					
					// display median
					final int newLeftMargin = (int) (container.getHeight() * medianNormalized / 100) - indicator.getWidth() / 2;
					final int oldValue = ((MarginLayoutParams) indicator.getLayoutParams()).topMargin;

					Animation a = new Animation() {

						@Override
						protected void applyTransformation(
								float interpolatedTime, Transformation t) {
							MarginLayoutParams params = (MarginLayoutParams) indicator
									.getLayoutParams();
							params.topMargin = oldValue + (int) ((newLeftMargin - oldValue) * interpolatedTime);
							indicator.setLayoutParams(params);
						}
					};

					a.setDuration(300);
					indicator.startAnimation(a);

					lastCompressionTime = currentTime;
					compressionCnt++;
				}
			}
		}
	}

}

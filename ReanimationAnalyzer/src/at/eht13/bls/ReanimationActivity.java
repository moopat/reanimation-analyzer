package at.eht13.bls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

/* author:
 * Christiane Prutsch, Markus Deutsch, Clemens Kaar
 * 17.12.2013
 */
public class ReanimationActivity extends Activity implements
		OnSeekBarChangeListener, OnChronometerTickListener, SensorEventListener, OnLoadCompleteListener, OnClickListener {

	// ui elements
	private Chronometer chronometer;
	private ImageView indicator;
	private RelativeLayout container;
	private SeekBar unlocker;

	private long startTime;
	private long endTime;

	private int currentProgress = 0;

	// accelerometer sensor
	private SensorManager sensorMan;
	private Sensor accelerometer;

	private float[] mGravity;
	private float mAccel;
	private float mAccelCurrent;
	private float mAccelLast;

	private int compressionCnt;
	private Date lastCompressionTime;
	private int optimumRate;             // frequency
	private long optimumTimeSpan;
	private List<Long> results;
	private int nrResultsForAvgCalc;
	private double stepSize;
	
	// sound output
	private SoundPool sp;
	boolean soundReady = false;
	int sound;
	private CountDownTimer timer;
	private int tickCount = 0;
	
	// shared preferences
	private SharedPreferences prefs;
	private boolean playTicks = true;
	private String KEY_PLAYTICKS = "playticks";
	
	private ImageView switcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reanimation);
		
		// access to shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		playTicks = prefs.getBoolean(KEY_PLAYTICKS, playTicks);

		// get ui control elements
		container = (RelativeLayout) findViewById(R.id.indicator_container);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		indicator = (ImageView) findViewById(R.id.indicator);
		unlocker = (SeekBar) findViewById(R.id.unlocker);
		switcher = (ImageView) findViewById(R.id.silencer);

		// set listeners
		unlocker.setOnSeekBarChangeListener(this);
		chronometer.setOnChronometerTickListener(this);
		switcher.setOnClickListener(this);

		TrainingResultDAO.init(getApplicationContext());

		// init accelerometer sensor
		sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mAccel = 0.00f;
		mAccelCurrent = SensorManager.GRAVITY_EARTH;
		mAccelLast = SensorManager.GRAVITY_EARTH;

		/**
		 * The rate can be changed here:
		 * http://www.moop.at/bls/
		 * It is updated everytime the app is started and when opening the menu and pressing Refresh
		 * in MainActivity.
		 */
		optimumRate = prefs.getInt("RATE", getResources().getInteger(R.integer.default_rate));
		optimumTimeSpan = 60000 / optimumRate;
		results = new ArrayList<Long>();
		nrResultsForAvgCalc = 3;
		stepSize = 100.0 / optimumTimeSpan;

		compressionCnt = -1;
		
		// init sound output
		sp = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
		sp.setOnLoadCompleteListener(this);
        sound = sp.load(this, R.raw.woosh, 1);
		
		updateIndicator();
		
		if(!playTicks){
			switcher.setImageResource(R.drawable.ic_action_muted);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// calculate the total average compression rate
	private long calculateTotalAverage() {
		long diff = endTime - startTime;
		
		return diff / compressionCnt;
	}

	// start the reanimation analysis
	private void startTraining() {
		lastCompressionTime = new Date();

		// timer
		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
		startTime = new Date().getTime();
		
		startTickTack();
	}
	
	// starts the sound output
	private void startTickTack(){
		timer = new CountDownTimer(optimumTimeSpan * 1000, optimumTimeSpan){
			
			public void onTick(long millisUntilFinished) {
		        playSound();
		     }

		     public void onFinish() {
		    	 startTickTack();
		     }
		     
		}.start();
	}
	
	// stops the sound output
	private void stopTickTack(){
		if(timer == null){
			return;
		}
		timer.cancel();
	}
	
	// outputs one tick tack
	private void playSound(){
		if(soundReady && tickCount > 0 && playTicks){
			sp.play(sound, 1, 1, 1, 0, 1.5f);
		}
		tickCount++;
	}

	// stop the reanimation analysis
	private void stopTraining() {
		chronometer.stop();
		endTime = new Date().getTime();

		// only calculate average and show result if the user did compressions
		if (compressionCnt > 0) {
			float duration = (float) ((int) (endTime - startTime)) / 1000;
	
			// create new training result
			TrainingResult tr = new TrainingResult();
			tr.setDate(new Date());
			
			// calculate average compression rate
			long avg = calculateTotalAverage();
			
			// limits
			double downLimit1 = optimumTimeSpan * 0.9;
			double downLimit2 = optimumTimeSpan * 0.75;
			double upLimit1 = optimumTimeSpan * 1.1;
			double upLimit2 = optimumTimeSpan * 1.25;
			
			// detect quality
			int quality = 3;
			
			if (avg < downLimit2)
				quality = 3;
			else if (avg < downLimit1)
				quality = 2;
			else if (avg < upLimit1)
				quality = 1;
			else if (avg < upLimit2)
				quality = 2;
			
			tr.setQuality(quality);
			tr.setDuration((int) Math.ceil(duration));
	
			// save training result
			TrainingResultDAO.insert(tr);
			
			// set activity result
			if (getParent() == null) {
				setResult(Activity.RESULT_OK);
			} else {
				getParent().setResult(Activity.RESULT_OK);
			}
		} else {
			if (getParent() == null) {
				setResult(Activity.RESULT_CANCELED);
			} else {
				getParent().setResult(Activity.RESULT_CANCELED);
			}
		}

		finish();
	}

	@Override
	public void onChronometerTick(Chronometer chronometer) {
		Date currentTime = new Date();

		// update current compression rate, if there are no compressions at the moment
		long timeDiff = currentTime.getTime() - lastCompressionTime.getTime();
		
		if (timeDiff > optimumTimeSpan + (optimumTimeSpan / 2))
		{
			addCompressionTime(timeDiff);

			updateIndicator();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle interaction on the action bar items
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
		// nothing to do :)
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.getProgress() == 100) {
			stopTraining();
		} else {
			// for a smoother experience, only reset the seek bar if the use did not stop training.
			seekBar.setProgress(0);
			currentProgress = 0;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// init accelerometer sensor
		sensorMan.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// pause accelerometer sensor
		sensorMan.unregisterListener(this);
		stopTickTack();
	}

	// adds a new compression time in the result collection
	private void addCompressionTime(Long time) {
		long optimumRateHalf = optimumTimeSpan / 2;

		if (time < optimumRateHalf)
			time = optimumRateHalf;

		if (time > optimumTimeSpan + optimumRateHalf)
			time = optimumTimeSpan + optimumRateHalf;

		results.add(time);
	}
	
	// not used (it would be also possible to use the median calculation instead of average calculation)
	@SuppressWarnings("unused")
	private float calculateMedian() {
		int resultsSize = results.size();
		
		if (resultsSize == 0)
			return optimumTimeSpan / 2;
		
		List<Long> resultsForAvgCalc = new ArrayList<Long>();

		// only use the last results for median calculation
		int n = nrResultsForAvgCalc;

		if (resultsSize < nrResultsForAvgCalc)
			n = resultsSize;

		for (int i = 0; i < n; i++) {
			resultsForAvgCalc.add(results.get(resultsSize - 1 - i));
		}

		// only one value for calculation
		if (resultsForAvgCalc.size() == 1)
			return resultsForAvgCalc.get(0);
		
		// sort values
		Collections.sort(resultsForAvgCalc);

		// calculate median
		int m = resultsForAvgCalc.size() / 2;

		if (resultsForAvgCalc.size() % 2 == 0) {
			return (resultsForAvgCalc.get(m) + resultsForAvgCalc.get(m - 1)) / 2;
		}

		return resultsForAvgCalc.get(m);
	}

	// calculate the average compression time of the last n compressions
	private float calculateAverage() {
		int resultsSize = results.size();
		
		// no results available
		if (resultsSize == 0)
			return optimumTimeSpan / 2;

		int n = nrResultsForAvgCalc;

		if (resultsSize < nrResultsForAvgCalc)
			n = resultsSize;

		long sum = 0;
		
		// calculate average
		for (int i = 0; i < n; i++) {
			sum += results.get(resultsSize - 1 - i);
		}

		return sum / n;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nothing to do :)
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// compression detection by analysing the accelerometer values
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravity = event.values.clone();

			// only z value used
			float z = mGravity[2];

			// calculate current acceleration
			mAccelLast = mAccelCurrent;
			mAccelCurrent = z;
			float delta = mAccelCurrent - mAccelLast;
			mAccel = mAccel * 0.9f + delta;

			// make this higher or lower according to how much compression you want to detect
			if (mAccel > 2.5) {
				if (compressionCnt < 0) {
					// start training at first compression
					compressionCnt++;
					startTraining();
				} else {
					// update current compression result
					Date currentTime = new Date();
	
					long timeDiff = currentTime.getTime() - lastCompressionTime.getTime();
	
					if (timeDiff > 300) {
						addCompressionTime(timeDiff);
	
						updateIndicator();
	
						lastCompressionTime = currentTime;
						compressionCnt++;
					}
				}
			}
		}
	}
	
	private void updateIndicator() {
		// normalize average
		double avgNormalized = (calculateAverage() - (optimumTimeSpan / 2)) * stepSize;
		
		// calculate margin
		final int newTopMargin = (int) (container.getHeight() * avgNormalized / 100) - indicator.getHeight() / 2;
		final int oldValue = ((MarginLayoutParams) indicator.getLayoutParams()).topMargin;

		// move indicator to current average rate
		Animation a = new Animation() {

			@Override
			protected void applyTransformation(
					float interpolatedTime, Transformation t) {
				MarginLayoutParams params = (MarginLayoutParams) indicator
						.getLayoutParams();
				params.topMargin = oldValue + (int) ((newTopMargin - oldValue) * interpolatedTime);
				
				indicator.setLayoutParams(params);
			}
		};

		a.setDuration(300);
		indicator.startAnimation(a);
	}
	
	@Override
	public void onBackPressed() {
		// handle situation, if back button is pressed
		if(compressionCnt < 1) {
			// reanimation not started
			if (getParent() == null) {
				setResult(Activity.RESULT_CANCELED);
			} else {
				getParent().setResult(Activity.RESULT_CANCELED);
			}
			finish();
		} else {
			// reanimation started
			Toast.makeText(getApplicationContext(), getString(R.string.infoBackDisabled), Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		soundReady = true;
	}

	@Override
	public void onClick(View v) {
		// activate/deactivate sound output
		SharedPreferences.Editor editor = prefs.edit();
		
		if(playTicks){
			playTicks = false;
			switcher.setImageResource(R.drawable.ic_action_muted);
		} else {
			playTicks = true;
			switcher.setImageResource(R.drawable.ic_action_loud);
		}
		
		editor.putBoolean(KEY_PLAYTICKS, playTicks);
		editor.commit();
	}

}

package at.eht13.bls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

public class ReanimationActivity extends Activity implements
		OnSeekBarChangeListener, OnChronometerTickListener, SensorEventListener, OnLoadCompleteListener, OnClickListener {

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
	private int optimumRate;             // frequency
	private long optimumTimeSpan;
	private List<Long> results;
	private int nrResultsForAvgCalc;
	private double stepSize;
	
	private SoundPool sp;
	boolean soundReady = false;
	int sound;
	private CountDownTimer timer;
	private int tickCount = 0;
	
	private SharedPreferences prefs;
	private ImageView switcher;
	private boolean playTicks = true;
	private String KEY_PLAYTICKS = "playticks";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reanimation);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		playTicks = prefs.getBoolean(KEY_PLAYTICKS, playTicks);

		container = (RelativeLayout) findViewById(R.id.indicator_container);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		indicator = (ImageView) findViewById(R.id.indicator);
		unlocker = (SeekBar) findViewById(R.id.unlocker);
		switcher = (ImageView) findViewById(R.id.silencer);

		unlocker.setOnSeekBarChangeListener(this);
		chronometer.setOnChronometerTickListener(this);
		switcher.setOnClickListener(this);

		TrainingResultDAO.init(getApplicationContext());

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
	
	private long calculateTotalAverage() {
		long diff = endTime - startTime;
		
		return diff / compressionCnt;
	}

	private void startTraining() {
		lastCompressionTime = new Date();

		chronometer.setBase(SystemClock.elapsedRealtime());
		chronometer.start();
		startTime = new Date().getTime();
		
		startTickTack();
	}
	
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
	
	private void stopTickTack(){
		timer.cancel();
	}
	
	private void playSound(){
		if(soundReady && tickCount > 0 && playTicks){
			sp.play(sound, 1, 1, 1, 0, 1.5f);
		}
		tickCount++;
	}

	private void stopTraining() {
		chronometer.stop();
		endTime = new Date().getTime();

		if (compressionCnt > 0) {
			float duration = (float) ((int) (endTime - startTime)) / 1000;
	
			TrainingResult tr = new TrainingResult();
			tr.setDate(new Date());
			
			long avg = calculateTotalAverage();
			
			double downLimit1 = optimumTimeSpan * 0.9;
			double downLimit2 = optimumTimeSpan * 0.75;
			double upLimit1 = optimumTimeSpan * 1.1;
			double upLimit2 = optimumTimeSpan * 1.25;
			
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
	
			TrainingResultDAO.insert(tr);
			
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
		Date currentTime = new Date();

		long timeDiff = currentTime.getTime() - lastCompressionTime.getTime();
		
		if (timeDiff > optimumTimeSpan + (optimumTimeSpan / 2))
		{
			addCompressionTime(timeDiff);

			updateIndicator();
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
		stopTickTack();
	}

	private void addCompressionTime(Long time) {
		long optimumRateHalf = optimumTimeSpan / 2;

		if (time < optimumRateHalf)
			time = optimumRateHalf;

		if (time > optimumTimeSpan + optimumRateHalf)
			time = optimumTimeSpan + optimumRateHalf;

		results.add(time);
	}
	
	// not used
	private float calculateMedian() {
		int resultsSize = results.size();
		
		if (resultsSize == 0)
			return optimumTimeSpan / 2;
		
		List<Long> resultsForAvgCalc = new ArrayList<Long>();

		int n = nrResultsForAvgCalc;

		if (resultsSize < nrResultsForAvgCalc)
			n = resultsSize;

		
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

	private float calculateAverage() {
		int resultsSize = results.size();
		
		if (resultsSize == 0)
			return optimumTimeSpan / 2;

		int n = nrResultsForAvgCalc;

		if (resultsSize < nrResultsForAvgCalc)
			n = resultsSize;

		long sum = 0;
		
		for (int i = 0; i < n; i++) {
			sum += results.get(resultsSize - 1 - i);
		}

		return sum / n;
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
			if (mAccel > 3) {
				if (compressionCnt < 0) {
					compressionCnt++;
					startTraining();
				} else {
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
		double avgNormalized = (calculateAverage() - (optimumTimeSpan / 2)) * stepSize;
		
		final int newTopMargin = (int) (container.getHeight() * avgNormalized / 100) - indicator.getHeight() / 2;
		final int oldValue = ((MarginLayoutParams) indicator.getLayoutParams()).topMargin;

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
		if(compressionCnt < 1){
			if (getParent() == null) {
				setResult(Activity.RESULT_CANCELED);
			} else {
				getParent().setResult(Activity.RESULT_CANCELED);
			}
			finish();
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.infoBackDisabled), Toast.LENGTH_SHORT).show();
		}
		
	}

	@Override
	public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
		soundReady = true;
	}

	@Override
	public void onClick(View v) {
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

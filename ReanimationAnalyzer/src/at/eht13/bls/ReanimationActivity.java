package at.eht13.bls;

import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

public class ReanimationActivity extends Activity implements OnSeekBarChangeListener, OnChronometerTickListener {
	
	private Chronometer chronometer;
	private ImageView indicator;
	private RelativeLayout container;
	private SeekBar unlocker;
	
	private long startTime;
	private long endTime;
	
	private int currentProgress = 0;
			
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
        
        startTraining();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void startTraining(){
    	chronometer.setBase(SystemClock.elapsedRealtime());
    	chronometer.start();
    	startTime = new Date().getTime();
    }
    
    private void stopTraining(){
    	chronometer.stop();
    	endTime = new Date().getTime();

    	float duration = (float) ((int) (endTime - startTime))/1000;
    	
    	TrainingResult tr = new TrainingResult();
    	tr.setDate(new Date());
    	tr.setQuality((new Random().nextInt(3)) + 1);
    	tr.setDuration((int) Math.ceil(duration));
    	
    	TrainingResultDAO.insert(tr);
    	
    	finish();
    }

	@Override
	public void onChronometerTick(Chronometer chronometer) {
		Random random = new Random();
		final int efficiency = random.nextInt(100);
		final int newLeftMargin = (int) (container.getHeight() * efficiency / 100) - indicator.getWidth()/2;
		final int oldValue = ((MarginLayoutParams) indicator.getLayoutParams()).topMargin;
		
		Animation a = new Animation() {

		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		        MarginLayoutParams params = (MarginLayoutParams) indicator.getLayoutParams();
		        params.topMargin = oldValue + (int) ((newLeftMargin - oldValue) * interpolatedTime);
		        indicator.setLayoutParams(params);
		    }
		};

		a.setDuration(1000);
		indicator.startAnimation(a);
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
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser){
			if ((progress > (currentProgress + 24)) || (progress < (currentProgress - 24))) {
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
		if(seekBar.getProgress() == 100){
			stopTraining();
		}
		
		seekBar.setProgress(0);
		currentProgress = 0;
		
	}
    
}

package at.eht13.bls;

import java.util.Date;
import java.util.Random;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.eht13.bls.R;
import android.view.animation.Transformation;

public class MainActivity extends Activity implements OnClickListener, OnChronometerTickListener {
	
	public static final int STATE_IDLE = 1;
	public static final int STATE_RUNNING = 2;
	
	private TextView tv;
	private Chronometer chronometer;
	private Button button;
	private ImageView indicator;
	private RelativeLayout container;
	
	private int status = STATE_IDLE;
	
	private long startTime;
	private long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tv = (TextView) findViewById(R.id.tv);
        container = (RelativeLayout) findViewById(R.id.indicator_container);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        indicator = (ImageView) findViewById(R.id.indicator);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
        chronometer.setOnChronometerTickListener(this);
                
        
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
    	status = STATE_RUNNING;
    	button.setText(R.string.lblStopTraining);
    	startTime = new Date().getTime();
    	tv.setText("");
    }
    
    private void stopTraining(){
    	chronometer.stop();
    	status = STATE_IDLE;
    	button.setText(R.string.lblStartTraining);
    	endTime = new Date().getTime();

    	float duration = (float) ((int) (endTime - startTime))/1000;
    	tv.setText("Dauer: " + duration);
    }


	@Override
	public void onClick(View v) {
		
		if(button.isPressed()){
			if(status == STATE_IDLE){
				startTraining();
			} else {
				stopTraining();
			}
		}
		
	}


	@Override
	public void onChronometerTick(Chronometer chronometer) {
		Random random = new Random();
		final int efficiency = random.nextInt(100);
		final int newLeftMargin = (int) (container.getWidth() * efficiency / 100) - indicator.getWidth()/2;
		final int oldValue = ((MarginLayoutParams) indicator.getLayoutParams()).leftMargin;
		
		Animation a = new Animation() {

		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {
		        MarginLayoutParams params = (MarginLayoutParams) indicator.getLayoutParams();
		        params.leftMargin = oldValue + (int) ((newLeftMargin - oldValue) * interpolatedTime);
		        indicator.setLayoutParams(params);
		    }
		};

		a.setDuration(1000);
		indicator.startAnimation(a);
		tv.setText(efficiency + "%");

	}
    
}

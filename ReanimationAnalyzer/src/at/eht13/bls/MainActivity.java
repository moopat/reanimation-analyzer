package at.eht13.bls;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import at.eht13.bls.R;

public class MainActivity extends Activity implements OnClickListener {
	
	public static final int STATE_IDLE = 1;
	public static final int STATE_RUNNING = 2;
	
	private Chronometer chronometer;
	private Button button;
	private int status = STATE_IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        
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
    }
    
    private void stopTraining(){
    	chronometer.stop();
    	status = STATE_IDLE;
    	button.setText(R.string.lblStartTraining);
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
    
}

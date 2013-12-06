package at.eht13.bls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

public class ResultListActivity extends Activity {
	
	private ArrayList<TrainingResult> results;
	private ListView list;
	private SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM, H:mm", Locale.getDefault());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_list);
		
		list = (ListView) findViewById(R.id.list);
		
		initResults();
		
		list.setAdapter(new ResultAdapter());
	}
	
	private void initResults(){
		TrainingResultDAO.init(getApplicationContext());
		results = TrainingResultDAO.getAllTrainings();
	}
	
	private class ResultAdapter extends BaseAdapter {
		
		private TrainingResult currentResult;

		@Override
		public int getCount() {
			return results.size();
		}

		@Override
		public Object getItem(int arg0) {
			return results.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			currentResult = (TrainingResult) getItem(position);
			
			/**
			 * Set a type flag.
			 */
			if(convertView == null) {
				final LayoutInflater inflater = LayoutInflater.from(ResultListActivity.this);
				convertView = inflater.inflate(R.layout.item_result, parent, false);
			}
			
			TextView description = ((TextView) convertView.findViewById(R.id.description));
			ImageView icon = ((ImageView) convertView.findViewById(R.id.icon));
			
			StringBuilder durationString = new StringBuilder();
			int seconds = currentResult.getDuration() % 60;
			int minutes = currentResult.getDuration() / 60;
			
			if(minutes > 0){
				durationString.append(minutes == 1 ? getString(R.string.durationMinuteSingular) : getString(R.string.durationMinutePlural, minutes));
				durationString.append(" ");
			}
			
			if(seconds > 0){
				durationString.append(seconds == 1 ? getString(R.string.durationSecondSingular) : getString(R.string.durationSecondPlural, seconds));
				durationString.append(" ");
			}
			
			if(minutes < 1 && seconds < 1){
				durationString.append(getString(R.string.durationSecondPlural, seconds));
			}
			
			description.setText(sdf.format(currentResult.getDate()) + "\nDauer: " + durationString.toString());
			
			switch(currentResult.getQuality()){
				case 1:
					icon.setImageResource(R.drawable.ic_excellent);
					break;
				case 2:
					icon.setImageResource(R.drawable.ic_okay);
					break;
				case 3:
					icon.setImageResource(R.drawable.ic_bad);
					break;
			}

			return convertView;
		}
		
	}

}

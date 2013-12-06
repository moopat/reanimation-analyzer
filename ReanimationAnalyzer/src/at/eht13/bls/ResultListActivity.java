package at.eht13.bls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
		results = new ArrayList<TrainingResult>();
		results.add(new TrainingResult(new Date(), 90, 1));
		results.add(new TrainingResult(new Date(), 34, 2));
		results.add(new TrainingResult(new Date(), 4, 3));
		results.add(new TrainingResult(new Date(), 23, 3));
		results.add(new TrainingResult(new Date(), 452, 1));
		results.add(new TrainingResult(new Date(), 24, 2));
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

			description.setText(sdf.format(currentResult.getDate()) + "\nDauer: " + currentResult.getDuration() + " Sekunden");
			
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

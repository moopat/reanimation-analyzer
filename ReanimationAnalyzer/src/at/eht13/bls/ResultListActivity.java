package at.eht13.bls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import at.eht13.bls.db.TrainingResultDAO;
import at.eht13.bls.model.TrainingResult;

import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

public class ResultListActivity extends Activity {

	private ArrayList<TrainingResult> results;
	private ListView list;
	private TextView empty;

	private SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM, H:mm",
			Locale.getDefault());
	private ResultAdapter adapter;
	boolean highlightLast = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_list);
		
		/**
		 * Has the calling activity required us to highlight the first entry of the list?
		 */
		highlightLast = getIntent().getBooleanExtra("highlightLast", false);

		list = (ListView) findViewById(R.id.list);
		empty = (TextView) findViewById(R.id.empty);

		initResults();

		adapter = new ResultAdapter();
		list.setAdapter(adapter);
		list.setEmptyView(empty);

		/**
		 * Delete items from the list by swiping them.
		 */
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				list, new SwipeDismissListViewTouchListener.DismissCallbacks() {
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							adapter.remove(adapter.getItem(position));
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public boolean canDismiss(int position) {
						return true;
					}
				});
		list.setOnTouchListener(touchListener);
		list.setOnScrollListener(touchListener.makeScrollListener());
		
	}

	/**
	 * Load Training Results from the database
	 * into an ArrayList that can be connected to
	 * the ListView.
	 */
	private void initResults() {
		TrainingResultDAO.init(getApplicationContext());
		results = TrainingResultDAO.getAllTrainings();
	}

	/**
	 * The adapter takes care of displaying traning results
	 * in a list.
	 */
	private class ResultAdapter extends BaseAdapter {

		private TrainingResult currentResult;

		@Override
		public int getCount() {
			return results.size();
		}

		public void remove(Object item) {
			results.remove(item);
			TrainingResultDAO.delete((TrainingResult) item);
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

			if (convertView == null) {
				final LayoutInflater inflater = LayoutInflater.from(ResultListActivity.this);
				convertView = inflater.inflate(R.layout.item_result, parent, false);
			}

			TextView description = ((TextView) convertView.findViewById(R.id.description));
			ImageView icon = ((ImageView) convertView.findViewById(R.id.icon));

			/**
			 * Create text for listitem.
			 */
			StringBuilder durationString = new StringBuilder();
			int seconds = currentResult.getDuration() % 60;
			int minutes = currentResult.getDuration() / 60;

			if (minutes > 0) {
				durationString
						.append(minutes == 1 ? getString(R.string.durationMinuteSingular)
								: getString(R.string.durationMinutePlural,
										minutes));
				durationString.append(" ");
			}

			if (seconds > 0) {
				durationString
						.append(seconds == 1 ? getString(R.string.durationSecondSingular)
								: getString(R.string.durationSecondPlural,
										seconds));
				durationString.append(" ");
			}

			if (minutes < 1 && seconds < 1) {
				durationString.append(getString(R.string.durationSecondPlural,
						seconds));
			}

			description.setText(sdf.format(currentResult.getDate()) + "\n"
					+ getString(R.string.lblDuration) + ": "
					+ durationString.toString());

			/**
			 * Set icon depening on reanimation quality.
			 */
			switch (currentResult.getQuality()) {
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
			
			/**
			 * Highlight the first element if required.
			 */
			convertView.setBackgroundColor((highlightLast && position == 0) ? getResources().getColor(R.color.yellow_light) : getResources().getColor(android.R.color.transparent));

			return convertView;
		}

	}

}

package at.eht13.webservice;

import android.os.AsyncTask;
import at.eht13.webservice.RateFetcher.OnRateFetchedListener;

public class RateFetcherTask extends AsyncTask<Integer, Integer, Integer> {
	private OnRateFetchedListener l;
	
	public RateFetcherTask(OnRateFetchedListener l){
		this.l = l;
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		return RateFetcher.fetch();
	}
	
	@Override
	protected void onPostExecute(Integer rate){
		if(l != null){
			l.onRateFetched(rate);
		}
	}

}

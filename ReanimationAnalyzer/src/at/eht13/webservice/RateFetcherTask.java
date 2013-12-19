package at.eht13.webservice;

import android.os.AsyncTask;
import at.eht13.webservice.RateFetcher.OnRateFetchedListener;

/* author:
 * Christiane Prutsch, Markus Deutsch, Clemens Kaar
 * 17.12.2013
 */
public class RateFetcherTask extends AsyncTask<Void, Integer, Integer> {
	private OnRateFetchedListener l;
	
	public RateFetcherTask(OnRateFetchedListener l){
		this.l = l;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		return RateFetcher.fetch();
	}
	
	@Override
	protected void onPostExecute(Integer rate){
		if(l != null){
			l.onRateFetched(rate);
		}
	}

}

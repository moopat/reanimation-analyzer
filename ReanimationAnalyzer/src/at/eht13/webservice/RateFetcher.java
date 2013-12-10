package at.eht13.webservice;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class RateFetcher {
	
	private static String url = "http://www.moop.at/bls/getRate.php";
	public static int defaultValue;
    
    /**
     * Query server for the current compression rate.
     * 
     * @return int rate
     */
    public static int fetch(){
    	
    	int returnValue = defaultValue;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        
        int tries = 0;
        boolean success = false;
            
        while(tries < 3 && !success){
            try {
            	StringBuilder sb = new StringBuilder();
                tries++;
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                BufferedReader inreader = new BufferedReader(new InputStreamReader(entity.getContent()));
                String line;
                while ((line = inreader.readLine()) != null) {
                    sb.append(line);
                }
                inreader.close();
                success = true;
                return parse(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        return returnValue;
    }
    
    /**
     * This method takes the server response and reads the
     * compression rate.
     * @param result Server response
     * @return int Updated compression count
     */
    private static int parse(String result) {   
    	
    	int value = defaultValue;
    	
    	if(result != null){
    		try {
	    		JSONObject json = new JSONObject(result);
	    		value = json.getInt("rate");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	return value;
    }
    
    public interface OnRateFetchedListener {
    	
    	public void onRateFetched(int rate);
    	
    }

}
package android.ubication;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

/**
 * 
 * @author Flavio Corpa Ríos
 *
 */
public class UbicationService extends IntentService
{
	private static final String URL = "http://www.energysistem.com/ubication/index.php";
	
	// Attributes
	private double latitude;
	private double longitude;
	private double accuracy;
	private LocationManager locManager;
	private LocationListener locListener;	
	private String idUser;
	private int timeout = 1;
	
	// Constructor
	public UbicationService()
	{
		super(UbicationService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		// Get user Id from SQLite
		idUser = getUserId();
		// Get current ubication
		locate();
		
		// Request the server
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(URL);
		
		try 
		{
			// Set request headers
			request.setEntity(new UrlEncodedFormEntity(sendUbication()));
			request.setHeader("Accept", "application/json");
			// Execute request
			HttpResponse response = client.execute(request);
			// Get new timeout from response
			timeout = new JSONObject(EntityUtils.toString(response.getEntity())).getInt("timeout");
			
		}
		catch (Exception e) 
		{
			//TODO Try to resend the ubication later
			Log.e("Error", "Error in server response", e);
		}

		scheduleNextUpdate();
	}
	
	private void scheduleNextUpdate()
	{
	    	Intent intent = new Intent(this, this.getClass());
	    	PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    	Time nextUpdateTime = new Time();
	    	nextUpdateTime.set(System.currentTimeMillis() + timeout * DateUtils.MINUTE_IN_MILLIS);
	    
	    	AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	}
	
	private void locate()
    	{
	    	// Reference the LocationManager
	    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    	
	    	Location location;
	    	
	    	if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	    	{
    			location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	}
	    	else
	    	{
	    		location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    	}
	    	
	    	updateUbication(location);
	    	
	    	// Set listener to update location
	    	locListener = new LocationListener() 
	    	{
	    		public void onLocationChanged(Location loc) {
	    			updateUbication(loc);
    			}
    			public void onProviderDisabled(String provider){
    				Log.e("Warning", "Provider disconnected");
    			}
    			public void onProviderEnabled(String provider){
    				Log.e("Warning", "Provider connected");
    			}
    			public void onStatusChanged(String provider, int status, Bundle extras){
    				Log.e("Warning", "Provider status changed to: " + status);
    			}
	    	};
	    	
	    	// If GPS is enabled, get ubication from GPS, if not, from the phone network
	    	if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	    		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
	    	else
	    		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locListener);
    	}
    	
    	private void updateUbication(Location loc)
    	{
	    	if (loc != null)
	    	{
	    	    latitude = loc.getLatitude();
	    	    longitude = loc.getLongitude();
	    	    accuracy = loc.getAccuracy();
	    	}
    	}
    	
    	private List<NameValuePair> sendUbication()
    	{
    		String id = String.valueOf(System.currentTimeMillis());
    		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    	nameValuePairs.add(new BasicNameValuePair("action", "ubication"));
	    	nameValuePairs.add(new BasicNameValuePair("id", id));
	    	nameValuePairs.add(new BasicNameValuePair("userId", idUser));
	    	nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
	    	nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
	    	nameValuePairs.add(new BasicNameValuePair("accuracy", String.valueOf(accuracy)));
	    	return nameValuePairs;
    	}
    	
    	private String getUserId() 
    	{
    		Db users = new Db(this, "DBUsers", null, 1);
        	SQLiteDatabase db = users.getReadableDatabase();
        	String userId = "";
 
	        if(db != null)
	        {
	        	userId = db.rawQuery("SELECT * FROM Users", null).moveToFirst().getString(1);
	        }
	        return userId;
        }
}

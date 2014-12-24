package android.ubication;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Flavio Corpa Ríos.
 *
 */
public class Main extends Activity {
	
	// Attributes
	TextView edtLatitude;
	TextView edtLongitude;
	TextView edtAccuracy;
	TextView edtProviderStatus;
	Button btnEnable;
	Button btnDisable;
	
	LocationManager locManager;
	LocationListener locListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		edtLatitude = (TextView) this.findViewById(R.id.edtLatitude);
		edtLongitude = (TextView) this.findViewById(R.id.edtLongitude);
		edtAccuracy = (TextView) this.findViewById(R.id.edtAccuracy);
		edtProviderStatus = (TextView) this.findViewById(R.id.edtProviderStatus);
		btnEnable = (Button) this.findViewById(R.id.btnEnable);
		btnDisable = (Button) this.findViewById(R.id.btnDisable);

	        btnEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				locate();
			}
		});
	        
	        btnDisable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    		locManager.removeUpdates(locListener);
			}
		});   
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void locate()
	{
	    	// Reference to LocationManager
	    	locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    	
	    	Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    	
	    	showPosition(location);
	    	
		if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			warnGPSdisabled();
		
		if (!locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			warn3Gdisabled();
	    	
	    	// Set listener to update location
	    	locListener = new LocationListener() {
		    	public void onLocationChanged(Location loc) {
		    		showPosition(loc);
		    	}
		    	public void onProviderDisabled(String provider){
		    		edtProviderStatus.setText("Provider desconectado");
		    	}
		    	public void onProviderEnabled(String provider){
		    		edtProviderStatus.setText("Provider conectado");
		    	}
		    	public void onStatusChanged(String provider, int status, Bundle extras){
		    		edtProviderStatus.setText("Provider status changed to: " + status);
		    	}
	    	};
	    	
	    	// If GPS is enabled, take ubication from it, if not, take the phone network one
	    	if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
	    		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
	    	else
	    		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locListener);
    	}
    	
    	private void showPosition(Location loc) {
	    	if (loc != null) {
	    		edtLatitude.setText("Latitude: " + String.valueOf(loc.getLatitude()));
	    		edtLongitude.setText("Longitude: " + String.valueOf(loc.getLongitude()));
	    		edtAccuracy.setText("Accuracy: " + String.valueOf(loc.getAccuracy()));
	    	}
	    	else {
	    		edtLatitude.setText("Latitude: (undefined)");
	    		edtLongitude.setText("Longitude: (undefined)");
	    		edtAccuracy.setText("Accuracy: (undefined)");    		
	    	}
    	}
    	
    	public void warnGPSdisabled()
    	{
    		Toast msg = Toast.makeText(this, "GPS is disabled!", Toast.LENGTH_SHORT);
		msg.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0).show();
   	}
    
    	public void warn3Gdisabled()
    	{
    		Toast msg = Toast.makeText(this, "Network provider is disabled!", Toast.LENGTH_SHORT);
		msg.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0).show();
    	}
}

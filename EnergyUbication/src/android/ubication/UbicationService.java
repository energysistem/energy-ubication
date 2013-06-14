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
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

/**
 * 
 * @author Flavio Corpa R�os
 *
 */
public class UbicationService extends IntentService
{
	/**
	 * URL del Servidor
	 */
	private static final String URL = "http://www.energysistem.com/ubication/index.php";
	
	
	//Atributos de Clase
	private double latitud;
	private double longitud;
	private double precision;
	private LocationManager gestorLocalizacion;
	private LocationListener locEscuchador;	
	
	//Constructor
	public UbicationService()
	{
		super(UbicationService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.e("LogDebug", "Se ha arrancado el Servicio!");
		
		localizar();
		
		//Env�a la ubicaci�n al Servidor.
		HttpClient comunicacion = new DefaultHttpClient();
		HttpPost peticion = new HttpPost(URL);
		try 
		{
			peticion.setEntity(new UrlEncodedFormEntity(sendUbication()));
			peticion.setHeader("Accept", "application/json");
			HttpResponse respuesta = comunicacion.execute(peticion);
			String respuestaString = EntityUtils.toString(respuesta.getEntity());
			Log.e("LogDebug", "Respuesta del Server: " + respuestaString);
			
			//TODO Implementar que si el Servidor no responde, se vuelva a enviar la ubicaci�n.
			
		} catch (Exception e) 
		{
			Log.e("Error", "Error al recibir respuesta del Servidor.", e);
		}

		//Despu�s, programa el pr�ximo env�o.
		scheduleNextUpdate();
	}
	
	private void scheduleNextUpdate()
	{
	    Intent intent = new Intent(this, this.getClass());
	    PendingIntent pendingIntent =
	        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    //TODO Hay que implementar que el tiempo entre env�os lo determine el Servidor.

	    long currentTimeMillis = System.currentTimeMillis();
	    long nextUpdateTimeMillis = currentTimeMillis + 1 * DateUtils.MINUTE_IN_MILLIS;
	    Time nextUpdateTime = new Time();
	    nextUpdateTime.set(nextUpdateTimeMillis);
	    
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	}
		
	private void localizar()
    {
    	//Obtenemos una referencia al LocationManager
    	gestorLocalizacion = 
    		(LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	Location localizacion;
    	
    	//Obtenemos la �ltima posici�n conocida
    	if (gestorLocalizacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
    	{
    		localizacion = gestorLocalizacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	}
    	else
    	{
    		localizacion = gestorLocalizacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    	}
    	
    	//Mostramos la �ltima posici�n conocida
    	updateUbication(localizacion);
    	
    	//Nos registramos para recibir actualizaciones de la posici�n
    	locEscuchador = new LocationListener() {
	    	public void onLocationChanged(Location localizacion) {
	    		updateUbication(localizacion);
	    	}
	    	public void onProviderDisabled(String proveedor){
	    		//edtEstadoProveedor.setText("Proveedor desconectado");
	    	}
	    	public void onProviderEnabled(String proveedor){
	    		//edtEstadoProveedor.setText("Proveedor conectado");
	    	}
	    	public void onStatusChanged(String proveedor, int estado, Bundle extras){
	    		//edtEstadoProveedor.setText("Estado del proveedor: " + estado);
	    	}
    	};
    	
    	//Si el GPS est� habilitado, usa la ubicaci�n del GPS
    	if (gestorLocalizacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
    		gestorLocalizacion.requestLocationUpdates(
	    			LocationManager.GPS_PROVIDER, 30000, 0, locEscuchador);
    	else //Si no, utiliza la ubicaci�n de la red m�vil.
	    	gestorLocalizacion.requestLocationUpdates(
	    			LocationManager.NETWORK_PROVIDER, 30000, 0, locEscuchador);
    }
     
    private void updateUbication(Location localizacion)
    {
    	if (localizacion != null)
    	{
    	    latitud = localizacion.getLatitude();
    	    longitud = localizacion.getLongitude();
    	    precision = localizacion.getAccuracy();
    	}
    }
    
    private List<NameValuePair> sendUbication()
    {
    	String idEnviado = String.valueOf(System.currentTimeMillis());
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	    nameValuePairs.add(new BasicNameValuePair("action", "ubication"));
	    nameValuePairs.add(new BasicNameValuePair("id", idEnviado));
	    nameValuePairs.add(new BasicNameValuePair("email", "flaviocorpa@gmail.com")); //TODO RECIBIR DEL LOGIN
	    nameValuePairs.add(new BasicNameValuePair("latitude", String.valueOf(latitud)));
	    nameValuePairs.add(new BasicNameValuePair("longitude", String.valueOf(longitud)));
	    nameValuePairs.add(new BasicNameValuePair("accuracy", String.valueOf(precision)));
	    Log.e("LogDebug", "Ubicaci�n enviada: " + nameValuePairs.toString());
	    return nameValuePairs;
    }
	}
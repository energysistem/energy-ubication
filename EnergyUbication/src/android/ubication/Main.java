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
	
	//Atributos
	TextView edtLatitud;
	TextView edtLongitud;
	TextView edtPrecision;
	TextView edtEstadoProveedor;
	Button btnActivar;
	Button btnDesactivar;
	
	LocationManager gestorLocalizacion;
	LocationListener locEscuchador;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		edtLatitud = (TextView) this.findViewById(R.id.edtLatitud);
		edtLongitud = (TextView) this.findViewById(R.id.edtLongitud);
		edtPrecision = (TextView) this.findViewById(R.id.edtPrecision);
		edtEstadoProveedor = (TextView) this.findViewById(R.id.edtEstadoProveedor);
		btnActivar = (Button) this.findViewById(R.id.btnActivar);
		btnDesactivar = (Button) this.findViewById(R.id.btnDesactivar);

        btnActivar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				localizar();
			}
		});
        
        btnDesactivar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	gestorLocalizacion.removeUpdates(locEscuchador);
			}
		});   
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private void localizar()
    {
    	//Obtenemos una referencia al LocationManager
    	gestorLocalizacion = 
    		(LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	
    	//Obtenemos la última posición conocida
    	Location localizacion = 
    		gestorLocalizacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	//Mostramos la última posición conocida
    	mostrarPosicion(localizacion);
    	
    	//Si el GPS está deshabilitado
		if (!gestorLocalizacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
			advertirGPSdeshabilitado();
		
		//Si no hay cobertura
		if (!gestorLocalizacion.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			advertir3Gdeshabilitado();
    	
    	//Nos registramos para recibir actualizaciones de la posición
    	locEscuchador = new LocationListener() {
	    	public void onLocationChanged(Location localizacion) {
	    		mostrarPosicion(localizacion);
	    	}
	    	public void onProviderDisabled(String proveedor){
	    		edtEstadoProveedor.setText("Proveedor desconectado");
	    	}
	    	public void onProviderEnabled(String proveedor){
	    		edtEstadoProveedor.setText("Proveedor conectado");
	    	}
	    	public void onStatusChanged(String proveedor, int estado, Bundle extras){
	    		Log.i("", "Estado del proveedor: " + estado);
	    		edtEstadoProveedor.setText("Estado del proveedor: " + estado);
	    	}
    	};
    	
    	gestorLocalizacion.requestLocationUpdates(
    			LocationManager.GPS_PROVIDER, 30000, 0, locEscuchador);
    }
     
    private void mostrarPosicion(Location localizacion) {
    	if (localizacion!=null) {
    		edtLatitud.setText("Latitud: " + String.valueOf(localizacion.getLatitude()));
    		edtLongitud.setText("Longitud: " + String.valueOf(localizacion.getLongitude()));
    		edtPrecision.setText("Precisión: " + String.valueOf(localizacion.getAccuracy()));
    	}
    	else {
    		edtLatitud.setText("Latitud: (Sin datos)");
    		edtLongitud.setText("Longitud: (Sin datos)");
    		edtPrecision.setText("Precisión: (Sin datos)");    		
    	}
    }
    
    
    public void advertirGPSdeshabilitado()
    {
    	Toast mensaje = Toast.makeText(this, "¡El GPS está deshabilitado!", Toast.LENGTH_SHORT);
		mensaje.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
		mensaje.show();	
    }
    
    public void advertir3Gdeshabilitado()
    {
    	Toast mensaje = Toast.makeText(this, "¡El Operador de Telefonía está desconectado!", Toast.LENGTH_SHORT);
		mensaje.setGravity(Gravity.BOTTOM|Gravity.CENTER, 0, 0);
		mensaje.show();	
    }
}

/*
	//Obtenemos una referencia al LocationManager que es la clase principal en la que
	//nos basaremos siempre a la hor de utilizar la API de localización de Android.
	LocationManager gestorLocalizacion = (LocationManager)getSystemService(LOCATION_SERVICE);
	
	
	//Obtenemos la lista de todos los proveedores.
	List<String> listaProveedores = gestorLocalizacion.getAllProviders();
	
	//A continuación vamos a obtener las propiedades del primero de los proveedores obtenidos.
	//Obtenemos una referencia al primer proveedor.
	LocationProvider proveedor = gestorLocalizacion.getProvider(listaProveedores.get(0));
	//Obtenemos la propiedad de la precisión del proveedor.
	int precision = proveedor.getAccuracy();
	//Obtenemos la propiedad que nos indica si obtiene la altitud.
	boolean obtieneAltitud = proveedor.supportsAltitude();
	//Obtenemos la propiedad del nivel de consumo de recursos del proveedor.
	int consumoRecursos = proveedor.getPowerRequirement();
	
	//Creamos el objeto de la clase Criteria
	Criteria criterio = new Criteria();
	//Especificamos precisión alta
	criterio.setAccuracy(Criteria.ACCURACY_FINE);
	//Especificamos que proporcione la altitud
	criterio.setAltitudeRequired(true);
	
	//Mejor proveedor que cumple los criterios especificados
	String mejorProveedorCriterio = gestorLocalizacion.getBestProvider(criterio, false);
	//Lista de proveedores que cumplen los criterios especificados
	List<String> listaProveedoresCriterio = gestorLocalizacion.getProviders(criterio, false);
	
	//Si el GPS no está habilitado
	if (!gestorLocalizacion.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
		advertirGPSdeshabilitado();
	}
	
	LocationListener locEscuchador = new LocationListener() {
		public void onLocationChanged(Location localizacion) {
			mostrarPosicion(localizacion);
		}
		public void onProviderDisabled(String proveedor) {
			edtEstadoProveedor.setText("Proveedor Apagado");
		}
		public void onProviderEnabled(String proveedor) {
			edtEstadoProveedor.setText("Proveedor Encendido");
		}
		public void onStatusChanged(String proveedor, int estado, Bundle extras) {
			edtEstadoProveedor.setText("Estado del Proveedor: " + estado);
		}
	};
*/
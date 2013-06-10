package android.ubication;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.text.format.Time;

/**
 * 
 * @author Flavio Corpa R�os
 *
 */
public class UbicationService extends IntentService
{
	  public UbicationService()
	  {
	    //super(UbicationService.class.getSimpleName());
		  super("UbicationService");
	  }

	  @Override
	  protected void onHandleIntent(Intent intent)
	  {
	    //Env�a la ubicaci�n al Servidor.

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
	    long nextUpdateTimeMillis = currentTimeMillis + 15 * DateUtils.MINUTE_IN_MILLIS;
	    Time nextUpdateTime = new Time();
	    nextUpdateTime.set(nextUpdateTimeMillis);
	    
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	  }
	}
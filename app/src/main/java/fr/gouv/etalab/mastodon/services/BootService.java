package fr.gouv.etalab.mastodon.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Thomas on 29/08/2017.
 * BroadcastReceiver to start service when device boot
 */

public class BootService extends BroadcastReceiver {

    public BootService() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, StreamingService.class));
    }

}
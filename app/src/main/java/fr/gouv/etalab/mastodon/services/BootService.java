package fr.gouv.etalab.mastodon.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

/**
 * Created by Thomas on 29/08/2017.
 * BroadcastReceiver to start service when device boot
 */

public class BootService extends BroadcastReceiver {

    public BootService() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Account> accounts = new AccountDAO(context, db).getAllAccount();
        if( accounts != null){
            for (Account account: accounts) {
                Intent intentService = new Intent(context, StreamingService.class);
                intentService.putExtra("acccountId", account.getId());
                intentService.putExtra("accountAcct", account.getAcct());
                context.startService(intentService);
            }
        }
    }

}
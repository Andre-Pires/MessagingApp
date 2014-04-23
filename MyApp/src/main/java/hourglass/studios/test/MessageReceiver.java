package hourglass.studios.test;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.widget.Toast;


/**
 * Created by Pires on 7/6/13.
 */
public class MessageReceiver extends BroadcastReceiver {

    SharedPreferences prefs;
    ContentResolver contentResolver;

    @Override
    public void onReceive(Context context, Intent intent) {

        int counter;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle bundle = intent.getExtras();
        String sms = "", phone = "";
        SmsMessage[] messages;
        contentResolver = context.getContentResolver();

        if (bundle != null) {

            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                phone += messages[i].getOriginatingAddress();
                sms += messages[i].getMessageBody();
            }

            Toast.makeText(context, "You have a new message from - " + getContactName(phone) + " .", Toast.LENGTH_SHORT).show();


            counter = prefs.getInt("counter", 0);

            SharedPreferences.Editor prefEditor = prefs.edit();


            prefEditor.putString("message_" + counter, sms);

            ++counter;
            prefEditor.putInt("counter", counter);

            prefEditor.commit();

        }

    }

    private String getContactName(String number) {
        String name;
        Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        if (uri_cont != null) {
            Cursor cs = contentResolver.query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

            if (cs != null && cs.moveToFirst()) {
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cs.close();
            } else
                name = number;
        } else
            name = number;
        return name;
    }
}

package hourglass.studios.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;


/**
 * Created by Pires on 7/6/13.
 */
public class MessageReceiver extends BroadcastReceiver {

    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        int counter;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Bundle bundle = intent.getExtras();
        String sms = "", phone = "";
        SmsMessage[] msgs;

        if (bundle != null){

            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                phone += msgs[i].getOriginatingAddress();
                sms += msgs[i].getMessageBody();
            }

            Toast.makeText(context,"-->" + sms,Toast.LENGTH_SHORT).show();


            counter = prefs.getInt("counter", 0);

            SharedPreferences.Editor prefEditor = prefs.edit();


            prefEditor.putString("message_"+counter, sms);

            ++counter;
            prefEditor.putInt("counter", counter);

            prefEditor.commit();

        }

    }
}

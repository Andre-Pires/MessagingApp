package hourglass.studios.test;



import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

/**
 * Created by Pires on 6/29/13.
 */
public class Message extends ListActivity {

    static final String PREFS_MESS = "Message_Prefs";
    int num_sms;
    SharedPreferences prefs;
    Button buttonsend;
    EditText phone_rc, sms_rc;
    TextView txt2;
    String phone_sd, sms_sd;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message);

        // Preferences
        //prefs = getSharedPreferences(PREFS_MESS, MODE_PRIVATE);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initializeVars();


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        for (int i = 0; i < num_sms; i++)
            listItems.add(i, prefs.getString("message_" + i, null));

        adapter.notifyDataSetChanged();

    }

    public void initializeVars(){



        if (prefs.contains("counter"))
            num_sms = prefs.getInt("counter", 0);
        else
            num_sms = 0;


        buttonsend = (Button) findViewById(R.id.btsend);
        txt2 = (TextView) findViewById(R.id.textView2);
        phone_rc = (EditText) findViewById(R.id.textphone);
        sms_rc = (EditText) findViewById(R.id.textsms);

    }

    public void buttonSendOnClick(View v) {


        phone_sd = String.valueOf(phone_rc.getText());
        sms_sd = String.valueOf(sms_rc.getText());

        sendMessage();

    }

    public void sendMessage(){


        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentsms = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredsms = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);


        /*
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));


        */


       // PendingIntent sentsms = PendingIntent.getActivity(this, 0,new Intent(this, MainActivity.class), 0); //o intent tem de ter conteudo no segundo campo senão segfault


        SmsManager smsman = SmsManager.getDefault();



        try{
            smsman.sendTextMessage(phone_sd,null, sms_sd, sentsms, deliveredsms);
           // Toast.makeText(getBaseContext(),"Sending..",Toast.LENGTH_SHORT).show();
            txt2.setText("Sending..");
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getBaseContext(),"Error while sending!",Toast.LENGTH_SHORT).show();
           // txt2.setText("Error sending!");
        }


        // adicionar à lista supostamente
        listItems.add(sms_sd);
        adapter.notifyDataSetChanged();



        SharedPreferences.Editor prefEditor = prefs.edit();


        prefEditor.putString("message_"+ num_sms, listItems.get(num_sms));

        ++num_sms;
        prefEditor.putInt("counter", num_sms);

        prefEditor.commit();


        finish();

    }

}

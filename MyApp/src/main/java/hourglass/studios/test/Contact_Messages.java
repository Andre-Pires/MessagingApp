package hourglass.studios.test;


import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Pires on 6/29/13.
 */
public class Contact_Messages extends ListActivity{

    String thread, phone,  name = "";
    Button buttonsend;
    EditText sms_rc;
    TextView contact;
    String sms_sd;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;

    //Contact Sms List
    ArrayList<String> contacts=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_messages);

        //---- recovering the thread_id from main_activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            thread = extras.getString("thread");
        }

        contact = (TextView) findViewById(R.id.textCont);
        buttonsend = (Button) findViewById(R.id.btsend);
        sms_rc = (EditText) findViewById(R.id.textsms);


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);



        // ver se não é possivel fazer merge com o código de cima

        Uri uri = Uri.parse("content://sms/inbox");
        String where = "thread_id="+thread;
        Cursor mycursor= getContentResolver().query(uri, null, where ,null,null);

        //-----contact's name ------------------
        if(mycursor.moveToFirst())
            phone=mycursor.getString(mycursor.getColumnIndexOrThrow("address"));

        mycursor.close();

        if(!(phone.equals(""))){

            //contact's number
            Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            Cursor cs= getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER+"='"+ phone +"'",null,null);

            if(cs.getCount()>0)
            {
                cs.moveToFirst();
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
        }

        if(!name.equals(""))
            contact.setText( name + " - " + phone);

        else contact.setText(phone);



        //-------------contact's sms list-----------------
        String strUriCon = "content://sms/conversations/" + thread;
        Uri uriSmsConversations = Uri.parse(strUriCon);
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null,"date");


        if(c.moveToFirst())
        {
            while(c.moveToNext())
            {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if(c.getInt(c.getColumnIndexOrThrow("type")) == 1)                       // descobrir o que é q os numeros do type significam mesmo
                    listItems.add(name + "\n" + body);

                else if(c.getInt(c.getColumnIndexOrThrow("type")) == 2)
                    listItems.add("Me" + "\n" + body);

                else listItems.add(number + "\n" + body);
            }
        }
        c.close();

        adapter.notifyDataSetChanged();
    }


    public void buttonSendOnClick(View v) {

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
                        Toast.makeText(getBaseContext(), "Failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Airplane mode On",
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


        SmsManager smsman = SmsManager.getDefault();



        try{
            smsman.sendTextMessage(phone,null, sms_sd, sentsms, deliveredsms);
        }catch (Exception e){
            e.printStackTrace();
        }

       adapter.notifyDataSetChanged();

    }
}

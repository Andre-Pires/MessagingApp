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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Pires on 6/29/13.
 */
public class Message extends ListActivity{

    SharedPreferences prefs;
    Button buttonsend;
    EditText phone_rc, sms_rc;
    String phone_sd, sms_sd;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;

    //Contact Sms List
    ArrayList<String> contacts=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        //-------------lista de sms do meu numero-----------------
        Uri uri = Uri.parse("content://sms/");
        Cursor c= getContentResolver().query(uri, null, null, null, null);



        if(c.moveToLast()){
            while(c.moveToPrevious())
            {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                //String number =c.getString(c.getColumnIndexOrThrow("address"));
                listItems.add(body);
            }
        }
        c.close();

        adapter.notifyDataSetChanged();

        initializeList();

        buttonsend = (Button) findViewById(R.id.btsend);
        phone_rc = (EditText) findViewById(R.id.textphone);
        sms_rc = (EditText) findViewById(R.id.textsms);



    }

    public void initializeList(){


        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);



        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {

                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                new String[]{id}, null);



                        while (pCur.moveToNext())
                              contacts.add(name + " : " +  pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + " \n " +
                                      cellType(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))));

                        pCur.close();
                    }
            }


        }

        //eliminating duplicates and sort list
        HashSet hs = new HashSet();
        hs.addAll(contacts);

        contacts.clear();
        contacts.addAll(hs);

        Collections.sort(contacts);

        //---------------------Devia estar no topo---------------------------

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, contacts);
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.textphone);
        textView.setAdapter(adapter);


        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // --------- Eliminating cell type on contact field
            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index, long arg3) {

           String cenas = (String) av.getItemAtPosition(index);
               cenas =  cenas.replaceAll("( \n [a-zA-Z]*)", "");
               textView.setText(cenas);

            }



        });

    }


    public String cellType(int type){


        String stringType = "";


        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                stringType = "Home";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                stringType = "Mobile";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                stringType = "Work";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                stringType = "Home Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                stringType = "Work Fax";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                stringType = "Main";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                stringType = "Other";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                stringType = "Custom";
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                stringType = "Pager";
                break;
        }

        return stringType;
    }


    public void buttonSendOnClick(View v) {


        phone_sd = String.valueOf(phone_rc.getText()).replaceAll("[^0-9+]", "");

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
            smsman.sendTextMessage(phone_sd,null, sms_sd, sentsms, deliveredsms);
        }catch (Exception e){
            e.printStackTrace();
        }

       adapter.notifyDataSetChanged();

        finish();

    }
}

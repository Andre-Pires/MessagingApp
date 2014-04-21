package hourglass.studios.test;


import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Pires on 6/29/13.
 */
public class Contact_Messages extends ListActivity{

    private int maxListSize = 50;
    private String thread, phone = "";
    private String name = "";
    private EditText sms_rc;
    private TextView contact;
    private String sms_sd;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private ArrayList<String> listItems = new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    private ArrayAdapter<String> adapter;

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
        sms_rc = (EditText) findViewById(R.id.textsms);


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        if (phone.equals("")){
            phone = getPhoneNumber("content://sms/inbox");
        }
        if (phone.equals("")){
            phone = getPhoneNumber("content://sms/sent");
        }
        if (phone.equals("")){
            phone = getPhoneNumber("content://sms/drafts");
        }
        if (phone.equals("")){
            phone = getPhoneNumber("content://sms/outbox");
        }
        if (phone.equals("")){
            phone = getPhoneNumber("content://sms/failed");
        }

        if (!(phone.equals(""))) {

            //contact's number
            Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            Cursor cs= getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER+"='"+ phone +"'",null,null);

            if (cs != null && cs.getCount() > 0)
            {
                cs.moveToFirst();
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cs.close();
            }


            if (!name.equals(""))
                contact.setText(name + " - " + phone);

            else contact.setText(phone);

        } else
            contact.setText("Failed to retrieve name");

        //-------------contact's sms list-----------------
        populateSmsList();
    }

    private void populateSmsList() {
        String strUriCon = "content://sms/conversations/" + thread;
        Uri uriSmsConversations = Uri.parse(strUriCon);
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null,"date");

        if (c != null) {

            if (c.getCount() > maxListSize)
                listItems.add("Press to show more entries");

            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext()) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)                       // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals(""))
                        listItems.add(name + "\n\n" + body);

                    else listItems.add(number + "\n\n" + body);

                } else if (c.getInt(c.getColumnIndexOrThrow("type")) == 2)
                    listItems.add("Me" + "\n\n" + body);

                else listItems.add(number + "\n\n" + body);
            }
            c.close();

        }

        adapter.notifyDataSetChanged();
    }

    private String getPhoneNumber(String uriString){
        Uri uri = Uri.parse(uriString);
        String where = "thread_id="+thread;
        Cursor cursorPhone = getContentResolver().query(uri, null, where, null, null);
        String phone = "";

        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            phone = cursorPhone.getString(cursorPhone.getColumnIndexOrThrow("address"));
            cursorPhone.close();
        }
        return phone;
    }


    private void increaseList(int step) {
        String strUriCon = "content://sms/conversations/" + thread;
        Uri uriSmsConversations = Uri.parse(strUriCon);
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null, "date");
        ArrayList<String> tempList = new ArrayList<String>();

        int oldMaxListSize = maxListSize;
        maxListSize += step;

        if (c != null) {

            listItems.remove(0); /// remove first item;
            tempList.addAll(listItems);
            listItems.clear();

            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext() && c.getPosition() <= c.getCount() - oldMaxListSize) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)    // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals(""))
                        listItems.add(name + "\n\n" + body);

                    else listItems.add(number + "\n\n" + body);

                } else if (c.getInt(c.getColumnIndexOrThrow("type")) == 2)
                    listItems.add("Me" + "\n\n" + body);

                else listItems.add(number + "\n\n" + body);
            }
            c.close();

            listItems.addAll(tempList);
        }

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listItems.get(position).equals("Press to show more entries")) {
            increaseList(50);
        }

    }
}


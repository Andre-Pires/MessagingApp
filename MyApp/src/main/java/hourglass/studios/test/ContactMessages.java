package hourglass.studios.test;


import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
public class ContactMessages extends ListActivity {

    private int maxListSize = 50;
    private String thread, phone = "";
    private String name = "";
    private EditText smsReceived;
    private String smsSent = "";
    private BroadcastReceiver sentReceiver;
    private BroadcastReceiver deliveredReceiver;


    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private ArrayList<String> listContactSms = new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    private ArrayAdapter<String> contactSmsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_messages);

        String[] uriSms = {"content://sms/inbox", "content://sms/sent", "content://sms/drafts", "content://sms/outbox", "content://sms/failed"};


        //---- recovering the thread_id from main_activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            thread = extras.getString("thread");
        }

        TextView contact = (TextView) findViewById(R.id.textCont);
        smsReceived = (EditText) findViewById(R.id.textsms);


        contactSmsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContactSms);
        setListAdapter(contactSmsAdapter);

        int counter = 0;
        while (phone.equals("")) {
            phone = getPhoneNumber(uriSms[counter], thread);
            counter++;
        }

        if (!(phone.equals(""))) {

            //contact's number
            Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
            Cursor cs = getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + phone + "'", null, null);

            if (cs != null && cs.getCount() > 0) {
                cs.moveToFirst();
                name = cs.getString(cs.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
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
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null, "date");

        if (c != null) {

            if (c.getCount() > maxListSize)
                listContactSms.add("Press to show more entries");

            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext()) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)                       // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals(""))
                        listContactSms.add(name + "\n\n" + body);

                    else listContactSms.add(number + "\n\n" + body);

                } else if (c.getInt(c.getColumnIndexOrThrow("type")) == 2)
                    listContactSms.add("Me" + "\n\n" + body);

                else listContactSms.add(number + "\n\n" + body);
            }
            c.close();

        }

        contactSmsAdapter.notifyDataSetChanged();
    }

    private String getPhoneNumber(String uriString, String thread) {
        Uri uri = Uri.parse(uriString);
        String where = "thread_id=" + thread;
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

            listContactSms.remove(0); /// remove first item;
            tempList.addAll(listContactSms);
            listContactSms.clear();

            if (c.getCount() > maxListSize)
                listContactSms.add("Press to show more entries");

            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext() && c.getPosition() <= c.getCount() - oldMaxListSize) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)    // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals(""))
                        listContactSms.add(name + "\n\n" + body);

                    else listContactSms.add(number + "\n\n" + body);

                } else if (c.getInt(c.getColumnIndexOrThrow("type")) == 2)
                    listContactSms.add("Me" + "\n\n" + body);

                else listContactSms.add(number + "\n\n" + body);
            }
            c.close();

            listContactSms.addAll(tempList);
        }

        contactSmsAdapter.notifyDataSetChanged();
    }


    public void buttonSendOnClick(View v) {

        smsSent = String.valueOf(smsReceived.getText());
        smsReceived.setText("");
        sendMessage();

        if (smsSent.isEmpty())
            Toast.makeText(getBaseContext(), "Message field is empty.",
                    Toast.LENGTH_SHORT).show();
        else
            sendMessage();

    }

    public void sendMessage() {


        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        SmsManager smsMan = SmsManager.getDefault();

        PendingIntent sentSms = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredSms = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);


        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
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
        };

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
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
        };

        //---when the SMS has been sent---
        registerReceiver(sentReceiver, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));

        try {
            smsMan.sendTextMessage(phone, null, smsSent, sentSms, deliveredSms);
            ContentValues values = new ContentValues();
            values.put("address", phone);
            values.put("date", System.currentTimeMillis());
            values.put("body", smsSent);
            getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        } catch (Exception e) {
            e.printStackTrace();
        }



        listContactSms.add("Me" + "\n\n" + smsSent);
        contactSmsAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listContactSms.get(position).equals("Press to show more entries")) {
            int focus = contactSmsAdapter.getCount() - 1;
            increaseList(50);
            l.setSelectionFromTop(maxListSize - focus, 105);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null && deliveredReceiver != null) {
            unregisterReceiver(sentReceiver);
            unregisterReceiver(deliveredReceiver);
        }
    }
}


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
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private ArrayList<SmsItem> contactItems = new ArrayList<SmsItem>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    private ArrayAdapter<SmsItem> contactItemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_messages);


        //---- recovering the thread_id from main_activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            thread = extras.getString("thread");
        }

        TextView contact = (TextView) findViewById(R.id.textCont);
        smsReceived = (EditText) findViewById(R.id.textsms);

        registerReceivers();

        ListView l = getListView();
        l.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        l.setStackFromBottom(true);

        contactItemAdapter = new SmsListAdapter(this, R.layout.item_left, contactItems);
        l.setAdapter(contactItemAdapter);


        //show contact's name and number(if possible)
        showContactInfo(contact);

        //contact's sms list
        populateSmsList();
    }

    private void showContactInfo(TextView contact) {
        String[] uriSms = {"content://sms/inbox", "content://sms/sent", "content://sms/drafts", "content://sms/outbox", "content://sms/failed"};

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
    }

    private void populateSmsList() {
        String strUriCon = "content://sms/conversations/" + thread;
        Uri uriSmsConversations = Uri.parse(strUriCon);
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null, "date");

        if (c != null) {

            if (c.getCount() > maxListSize) {
                    contactItems.add(new SmsItem("", "Press to show more entries"));
            }


            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext()) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)             // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals("")) {
                        contactItems.add(new SmsItem(name, body));
                    } else {
                        contactItems.add(new SmsItem(number, body));
                    }

                } else {
                    if (c.getInt(c.getColumnIndexOrThrow("type")) == 2) {
                        contactItems.add(new SmsItem("Me", body));
                    } else {
                        contactItems.add(new SmsItem(number, body));
                    }
                }
            }
            c.close();

        }

        contactItemAdapter.notifyDataSetChanged();
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
        // ArrayList<String> tempList = new ArrayList<String>(); TODO
        ArrayList<SmsItem> tempList = new ArrayList<SmsItem>();

        int oldMaxListSize = maxListSize;
        maxListSize += step;

        if (c != null) {

            contactItems.remove(0); /// remove first item;
            tempList.addAll(contactItems);
            contactItems.clear();

            if (c.getCount() > maxListSize)
                contactItems.add(new SmsItem("", "Press to show more entries"));

            c.moveToPosition(c.getCount() - maxListSize);

            while (c.moveToNext() && c.getPosition() <= c.getCount() - oldMaxListSize) {
                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number = c.getString(c.getColumnIndexOrThrow("address"));

                if (c.getInt(c.getColumnIndexOrThrow("type")) == 1)    // type ALL - 0,DRAFTS - 3,INBOX - 1,OUTBOX - 4, SENT - 2
                {
                    if (!(name == null) && !name.equals("")) {
                        contactItems.add(new SmsItem(name, body));
                    } else {
                        contactItems.add(new SmsItem(number, body));
                    }

                } else if (c.getInt(c.getColumnIndexOrThrow("type")) == 2) {
                    contactItems.add(new SmsItem("Me", body));
                }

                else {
                    contactItems.add(new SmsItem(number, body));

                }
            }
            c.close();

            contactItems.addAll(tempList);
        }

        contactItemAdapter.notifyDataSetChanged();
    }


    public void buttonSendOnClick(View v) {

        smsSent = String.valueOf(smsReceived.getText());

        if (smsSent.isEmpty())
            Toast.makeText(getBaseContext(), "Message field is empty.",
                    Toast.LENGTH_SHORT).show();
        else{
            smsReceived.setText("");
            sendMessage();
        }


    }

    public void sendMessage() {



        SmsManager smsMan = SmsManager.getDefault();

        PendingIntent sentSms = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredSms = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);


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

        contactItems.add(new SmsItem("Me", smsSent));
        contactItemAdapter.notifyDataSetChanged();
    }

    private void registerReceivers() {
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
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (contactItems.get(position).getText().equals("Press to show more entries")) {
            int focus = contactItemAdapter.getCount() - 1;
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


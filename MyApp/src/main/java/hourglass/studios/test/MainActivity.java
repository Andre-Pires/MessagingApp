package hourglass.studios.test;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import static hourglass.studios.test.R.layout.activity_main;

public class MainActivity extends ListActivity {

    private Class textMessage, contactMessages;
    private Intent startSettings, sms, contactSms;


    //List of array strings which will serve as list items
    private ArrayList<String> listThreadItems = new ArrayList<String>();

    //Defining string adapter which will handle data of listview
    private ArrayAdapter<String> threadAdapter;

    //Keeps track of list items' thread number, to pass to contact's messages
    private ArrayList<String> listThreadNumb = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);

        createConversations();
    }


    private void createConversations() {

        threadAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listThreadItems);
        setListAdapter(threadAdapter);


        String strUriCon = "content://sms/conversations";
        Uri uriSmsThreads = Uri.parse(strUriCon);
        Cursor threadCursor = getContentResolver().query(uriSmsThreads, null, null, null, "date");


        if (threadCursor != null && threadCursor.getCount() > 0) {
            threadCursor.moveToLast();
            do {

                String smsBody = threadCursor.getString(threadCursor.getColumnIndexOrThrow("snippet"));
                String thread = threadCursor.getString(threadCursor.getColumnIndexOrThrow("thread_id"));
                String number = "";
                String name = "";

                listThreadNumb.add(thread);

                Uri uri = Uri.parse("content://sms/inbox");
                String where = "thread_id=" + thread;
                Cursor contactCursor = getContentResolver().query(uri, null, where, null, null);

                if (contactCursor != null && contactCursor.moveToFirst()) {
                    number = contactCursor.getString(contactCursor.getColumnIndexOrThrow("address"));
                    contactCursor.close();
                } else if (!number.equals("")){

                    // To check for name/number in any sms type
                    if (number.equals("")) {
                        number = getPhoneNumber("content://sms/sent", thread);
                    }
                    if (number.equals("")) {
                        number = getPhoneNumber("content://sms/drafts", thread);
                    }
                    if (number.equals("")) {
                        number = getPhoneNumber("content://sms/outbox", thread);
                    }
                    if (number.equals("")) {
                        number = getPhoneNumber("content://sms/failed", thread);
                    }
                }

                // arranjar para suportar varios numeros
                final boolean stringEmpty = number != null && number.equals("");

                if (!stringEmpty)
                    name = getContactName(number);

                listThreadItems.add(name + "\n\n" + smsBody);
            } while (threadCursor.moveToPrevious());

            threadCursor.close();
        }

        threadAdapter.notifyDataSetChanged();
    }

    private String getContactName(String number) {
        String name;
        Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        if (uri_cont != null) {
            Cursor cs = getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

            if (cs != null && cs.moveToFirst()) {
                name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                cs.close();
            } else
                name = number;
        } else
            name = number;
        return name;
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


    public void buttonSmsOnClick(View v) {


        try {
            textMessage = Class.forName("hourglass.studios.test.Message");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        sms = new Intent(MainActivity.this, textMessage);
        startActivity(sms);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            contactMessages = Class.forName("hourglass.studios.test.Contact_Messages");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        contactSms = new Intent(this, contactMessages);
        contactSms.putExtra("thread", listThreadNumb.get(position));
        startActivityForResult(contactSms, RESULT_OK);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettings = new Intent("hourglass.studios.test.SETTINGS");
                startActivity(startSettings);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        threadAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String message = extras.getString("textMessage");
            if (message != null && !message.equals("")) {
                listThreadItems.add(message);
                threadAdapter.notifyDataSetChanged();
            }
        }
    }
}

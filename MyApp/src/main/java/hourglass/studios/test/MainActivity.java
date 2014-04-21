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

    private Class txtmessage, contact_messages;
    private Intent startSettings, sms, contact_sms;


    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    private ArrayList<String> listItems = new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    private ArrayAdapter<String> adapter;

    //LIST OF ARRAY STRINGS WITH THREAD NUMBER
    private ArrayList<String> listThread = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);

        createConversations();
    }

    private void createConversations() {

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);


        String strUriCon = "content://sms/conversations";
        Uri uriSmsConversations = Uri.parse(strUriCon);
        Cursor c = getContentResolver().query(uriSmsConversations, null, null, null, "date");


        if (c != null && c.getCount() > 0) {
            c.moveToLast();
            do {

                String smsBody = c.getString(c.getColumnIndex("snippet"));
                String thread = c.getString(c.getColumnIndex("thread_id"));
                String number = "";
                String name = "";

                listThread.add(thread);

                Uri uri = Uri.parse("content://sms/inbox");
                String where = "thread_id=" + thread;
                Cursor myCursor = getContentResolver().query(uri, null, where, null, null);


                final boolean canMoveToFirst = myCursor != null && myCursor.moveToFirst();

                if (canMoveToFirst) {
                    number = myCursor.getString(myCursor.getColumnIndexOrThrow("address"));
                    myCursor.close();
                } else {

                    // To check for name/number in any sms type
                    if (number.equals("")) {
                        number = getPhoneNumber("content://sms/inbox", thread);
                    }
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

                if (!stringEmpty) {

                    //contact's number
                    Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

                    if (uri_cont != null) {
                        Cursor cs = getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER + "='" + number + "'", null, null);

                        if (cs != null && cs.getCount() > 0) {
                            cs.moveToFirst();
                            name = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                            cs.close();
                        } else
                            name = number;
                    } else
                        name = number;

                }

                listItems.add(name + "\n\n" + smsBody);
            }  while (c.moveToPrevious());

            c.close();
        }

        adapter.notifyDataSetChanged();
    }

    private String getPhoneNumber(String uriString, String thread){
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


    public void buttonSmsOnClick(View v) {


        try {
            txtmessage = Class.forName("hourglass.studios.test.Message");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        sms = new Intent(MainActivity.this, txtmessage);
        startActivity(sms);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            contact_messages = Class.forName("hourglass.studios.test.Contact_Messages");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        contact_sms = new Intent(MainActivity.this, contact_messages);
        contact_sms.putExtra("thread", listThread.get(position));
        startActivity(contact_sms);

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

        adapter.notifyDataSetChanged();

    }
}

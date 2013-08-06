package hourglass.studios.test;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static hourglass.studios.test.R.layout.activity_main;
import static hourglass.studios.test.R.layout.contact_messages;

public class MainActivity extends ListActivity {

    // para controlar a lista
    int counter, max;
    Class txtmessage, contact_messages;
    Button buttonsms;
    Intent startSettings, sms, contact_sms;


    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;

    //LIST OF ARRAY STRINGS WITH THREAD NUMBER
    ArrayList<String> listThread=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);

        buttonsms = (Button) findViewById(R.id.btsms);

        createConversations();



    }

    private void createConversations()
    {

    adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
    setListAdapter(adapter);

    //-------------teste -----------------

    String strUriCon = "content://sms/conversations";
    Uri uriSmsConversations = Uri.parse(strUriCon);
    Cursor c = getContentResolver().query(uriSmsConversations, null, null, null,"date");

        while (c.moveToNext())
        {
                String smsBody = c.getString(c.getColumnIndex("snippet"));
                String thread = c.getString(c.getColumnIndex("thread_id"));
                String number = "";


                listThread.add(thread);

                Uri uri = Uri.parse("content://sms/inbox");
                String where = "thread_id="+thread;
                Cursor mycursor= getContentResolver().query(uri, null, where ,null,null);



                if(mycursor.moveToFirst())
                        number=mycursor.getString(mycursor.getColumnIndexOrThrow("address"));

                mycursor.close();

                // arranjar para suportar varios numeros
                if(!(number.equals(""))){

                    //contact's number
                    Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
                    Cursor cs= getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER+"='"+number+"'",null,null);

                    if(cs.getCount()>0)
                    {
                        cs.moveToFirst();
                        number = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }


               listItems.add(number + "\n\n" + smsBody);
            }

        adapter.notifyDataSetChanged();
}


// Obsolete -- To be deleted

/*
    private void createConversations()
    {

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        //-------------teste -----------------

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c= getContentResolver().query(uri, null, null, null, null);



        if (max  == 0){

            max = 60;
        }
        // To show only the last 60 messages

        counter = 0;

        if(c.move(max))
        {
            listItems.add("Press to show more messages.");

            counter++;

           while(c.moveToPrevious())
           {
               if(counter > max)
                   break;

                String body = c.getString(c.getColumnIndexOrThrow("body"));
                String number =c.getString(c.getColumnIndexOrThrow("address"));

               //contact's number
               Uri uri_cont = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
               Cursor cs= getContentResolver().query(uri_cont, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, ContactsContract.PhoneLookup.NUMBER+"='"+number+"'",null,null);

               if(cs.getCount()>0)
               {
                   cs.moveToFirst();
                   number = cs.getString(cs.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
               }

               listItems.add(number + "\n\n" + body);

               counter++;
            }
            c.close();

        }

        adapter.notifyDataSetChanged();
    }
*/

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
        contact_sms.putExtra("thread",listThread.get(position));
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
                startActivity( startSettings);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.notifyDataSetChanged();

    }
}

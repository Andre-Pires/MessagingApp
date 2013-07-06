package hourglass.studios.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import static hourglass.studios.test.R.layout.activity_main;

public class MainActivity extends Activity {

    Class txtmessage;
    Button buttonsms;
    TextView txt2;
    Intent startSettings, sms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        buttonsms = (Button) findViewById(R.id.btsms);
        txt2 = (TextView) findViewById(R.id.textView2);
    }

    public void buttonSmsOnClick(View v) {



        try {
            txtmessage = Class.forName("hourglass.studios.test.Message");
            txt2.setText("Going to text message.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            txt2.setText("Failed..");
        }

        sms = new Intent(MainActivity.this, txtmessage);
        startActivity(sms);


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
}

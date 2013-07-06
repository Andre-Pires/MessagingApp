package hourglass.studios.test;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Pires on 6/27/13.
 */
public class Settings extends ListActivity {

    String stuff[] = {"DisplayText", "cenas2", "cenas3", "cenas4", "cenas5", "cenas6", "cenas7", "cenas8", "cenas9", "cenas10", "cenas11", "cenas12", "cenas13", "cenas14", "cenas15"};
    @Override
    protected void onCreate(Bundle sets) {
        super.onCreate(sets);
        setListAdapter(new ArrayAdapter<String>(Settings.this, android.R.layout.simple_list_item_checked , stuff));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(position == 0){

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}

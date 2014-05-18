package hourglass.studios.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by A. Pires on 10/05/2014.
 */
public class ThreadListAdapter extends ArrayAdapter<ThreadItem> {
    private final LayoutInflater inflater;
    private final String messagesText = " messages ";
    private final String messageText = " message ";
    private List<ThreadItem> threadItems = new ArrayList<ThreadItem>();

    private static class ThreadViewHolder {

        private TextView name;
        private TextView text;
        private TextView msgCount;
    }

    public ThreadListAdapter(Context context, int resource, List<ThreadItem> objects) {
        super(context, resource, objects);
        this.inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        this.threadItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = this.getItemViewType(position);

        switch(viewType) {
            case 0: {
                ThreadViewHolder holder;

                View view = convertView;
                if (view == null) {
                    view = inflater.inflate(R.layout.thread_item, parent, false);

                    holder = new ThreadViewHolder();
                    holder.name = (TextView) view.findViewById(R.id.threadName);
                    holder.text = (TextView) view.findViewById(R.id.threadText);
                    holder.msgCount = (TextView) view.findViewById(R.id.messageCount);
                    view.setTag(holder);
                } else {

                    holder = (ThreadViewHolder) view.getTag();
                }

                ThreadItem item = threadItems.get(position);

                if (item != null) {
                        holder.name.setText(item.getName());
                        holder.text.setText(item.getText());

                        //distinguishing between the text for several message or just one
                        if (Integer.parseInt(item.getMsgCount()) == 1)
                            holder.msgCount.setText(item.getMsgCount() + messageText);
                        else
                            holder.msgCount.setText(item.getMsgCount() + messagesText);
                }
                return view;
            }


            default:
                throw new IllegalStateException("The view type shouldn't exist.");
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
            return 0;
    }
}


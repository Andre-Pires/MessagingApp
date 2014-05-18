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
public class SmsListAdapter extends ArrayAdapter<SmsItem> {
    private final LayoutInflater inflater;
    private List<SmsItem> smsItems = new ArrayList<SmsItem>();
    private final int rightViewType = 1;
    private final int leftViewType = 2;
    private final int showMoreType = 0;
    private final int viewCount = 3;;

    private static class LeftViewHolder {
        private TextView name;
        private TextView text;
    }

    private static class RightViewHolder {
        private TextView name;
        private TextView text;
    }

    private static class ShowViewHolder {
        private TextView showMoreText;
    }

    public SmsListAdapter(Context context, int resource, List<SmsItem> objects) {
        super(context, resource, objects);
        this.inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        this.smsItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = this.getItemViewType(position);

        switch(viewType) {
            case rightViewType: {
                RightViewHolder rightHolder;

                View view = convertView;
                if (view == null) {
                    view = inflater.inflate(R.layout.item_right, parent, false);

                    rightHolder = new RightViewHolder();
                    rightHolder.name = (TextView) view.findViewById(R.id.rightName);
                    rightHolder.text = (TextView) view.findViewById(R.id.rightText);
                    view.setTag(rightHolder);
                } else {

                    rightHolder = (RightViewHolder) view.getTag();
                }

                SmsItem item = smsItems.get(position);

                if (item != null) {
                    if (rightHolder.text != null && rightHolder.name != null) {
                        rightHolder.name.setText(item.getName());
                        rightHolder.text.setText(item.getText());
                    }
                }
                return view;
            }

            case leftViewType: {
                LeftViewHolder leftHolder;

                View view = convertView;
                if (view == null) {
                    view = inflater.inflate(R.layout.item_left, parent, false);

                    leftHolder = new LeftViewHolder();
                    leftHolder.name = (TextView) view.findViewById(R.id.leftName);
                    leftHolder.text = (TextView) view.findViewById(R.id.leftText);
                    view.setTag(leftHolder);
                } else {
                    leftHolder = (LeftViewHolder) view.getTag();
                }

                SmsItem item = smsItems.get(position);

                if (item != null) {
                    if (leftHolder.text != null && leftHolder.name != null) {
                        leftHolder.name.setText(item.getName());
                        leftHolder.text.setText(item.getText());
                    }
                }
                return view;
            }
            case showMoreType: {
                ShowViewHolder holder;

                View view = convertView;
                if (view == null) {
                    view = inflater.inflate(R.layout.item_showmore, parent, false);

                    holder = new ShowViewHolder();
                    holder.showMoreText = (TextView) view.findViewById(R.id.showText);
                    view.setTag(holder);
                } else {
                    holder = (ShowViewHolder) view.getTag();
                }

                SmsItem item = smsItems.get(position);

                if (item != null) {
                    if (holder.showMoreText != null) {
                        holder.showMoreText.setText(item.getText());
                    }
                }
                return view;
            }


            default:
                throw new IllegalStateException("The view type shouldn't exist.");
        }
    }

    @Override
    public int getViewTypeCount() {
        return viewCount;
    }

    @Override
    public int getItemViewType(int position) {
        final String name = smsItems.get(position).getName();

        if (name.equals("Me")) {
            return rightViewType;
        } else if(!name.equals("")){
            return leftViewType;
        } else {
            return showMoreType;
        }
    }
}


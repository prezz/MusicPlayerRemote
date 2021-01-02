package net.prezz.mpr.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.prezz.mpr.R;

import java.util.List;


public class PartitionArrayAdapter extends ArrayAdapter<PartitionAdapterEntity> {

    public PartitionArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        throw new UnsupportedOperationException();
    }

    public PartitionArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        throw new UnsupportedOperationException();
    }

    public PartitionArrayAdapter(Context context, int textViewResourceId, PartitionAdapterEntity[] objects) {
        super(context, textViewResourceId, objects);
        throw new UnsupportedOperationException();
    }

    public PartitionArrayAdapter(Context context, int resource, int textViewResourceId, PartitionAdapterEntity[] objects) {
        super(context, resource, textViewResourceId, objects);
        throw new UnsupportedOperationException();
    }

    public PartitionArrayAdapter(Context context, int textViewResourceId, List<PartitionAdapterEntity> objects) {
        super(context, textViewResourceId, objects);
    }

    public PartitionArrayAdapter(Context context, int resource, int textViewResourceId, List<PartitionAdapterEntity> objects) {
        super(context, resource, textViewResourceId, objects);
        throw new UnsupportedOperationException();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout)convertView;

        PartitionAdapterEntity entity = getItem(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (entity.getEntity().isClientPartition()) {
                view = (LinearLayout) inflater.inflate(R.layout.view_list_item_partition_selected, parent, false);
            } else {
                view = (LinearLayout) inflater.inflate(R.layout.view_list_item_partition, parent, false);
            }
        }

        TextView textView1 = (TextView)view.findViewById(R.id.partition_list_item_text1);
        TextView textView2 = (TextView)view.findViewById(R.id.partition_list_item_text2);

        textView1.setText(entity.getText());
        textView2.setText(entity.getSubText());

        return view;
    }

    public void setData(PartitionAdapterEntity[] data) {
        setNotifyOnChange(false);
        clear();
        if (data != null) {
            addAll(data);
        }
        notifyDataSetChanged();
    }
}

package net.prezz.mpr.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
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

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = (LinearLayout) inflater.inflate(R.layout.view_list_item_partition, parent, false);

            TextView textView1 = (TextView)view.findViewById(R.id.partition_list_item_text1);
            view.setTag(createTag(false, textView1.getTextColors()));
        }

        ViewTag tag = (ViewTag)view.getTag();

        TextView textView1 = (TextView)view.findViewById(R.id.partition_list_item_text1);
        TextView textView2 = (TextView)view.findViewById(R.id.partition_list_item_text2);

        PartitionAdapterEntity entity = getItem(position);

        textView1.setText(entity.getText());
        textView2.setText(entity.getSubText());

        if (entity.getEntity().isClientPartition() != tag.selected) {
            if (entity.getEntity().isClientPartition()) {
                int color = getSelectionColor();
                textView1.setTextColor(color);
                textView2.setTextColor(color);
            } else {
                textView1.setTextColor(tag.standardColors);
                textView2.setTextColor(tag.standardColors);
            }
            tag.selected = entity.getEntity().isClientPartition();
        }

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

    private int getSelectionColor() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.redFocusColor, typedValue, true);
        return typedValue.data;
    }

    private ViewTag createTag(boolean selected, ColorStateList colorStateList) {
        ViewTag tag = new ViewTag();
        tag.selected = selected;
        tag.standardColors = colorStateList;
        return tag;
    }

    private static final class ViewTag {
        boolean selected;
        ColorStateList standardColors;
    }
}

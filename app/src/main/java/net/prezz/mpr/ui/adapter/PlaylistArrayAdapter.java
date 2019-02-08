package net.prezz.mpr.ui.adapter;

import java.util.List;

import net.prezz.mpr.R;
import net.prezz.mpr.model.PlayerState;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PlaylistArrayAdapter extends ArrayAdapter<PlaylistAdapterEntity> {
	
	private int currentSong = -1;
	private PlayerState playerState = PlayerState.STOP;
	
    public PlaylistArrayAdapter(Context context, int textViewResourceId) {
    	super(context, textViewResourceId);
    	throw new UnsupportedOperationException();
    }

    public PlaylistArrayAdapter(Context context, int resource, int textViewResourceId) {
    	super(context, resource, textViewResourceId);
    	throw new UnsupportedOperationException();
    }

    public PlaylistArrayAdapter(Context context, int textViewResourceId, PlaylistAdapterEntity[] objects) {
    	super(context, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }

    public PlaylistArrayAdapter(Context context, int resource, int textViewResourceId, PlaylistAdapterEntity[] objects) {
    	super(context, resource, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }

    public PlaylistArrayAdapter(Context context, int textViewResourceId, List<PlaylistAdapterEntity> objects) {
    	super(context, textViewResourceId, objects);
    }

    public PlaylistArrayAdapter(Context context, int resource, int textViewResourceId, List<PlaylistAdapterEntity> objects) {
    	super(context, resource, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		LinearLayout view = (LinearLayout)convertView;

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = (LinearLayout)inflater.inflate(R.layout.view_list_item_playlist, parent, false);
			
			TextView textView1 = (TextView)view.findViewById(R.id.playlist_list_item_text1);
			view.setTag(createTag(R.drawable.ic_drag, textView1.getTextColors()));
		}
		
		int drawableId = R.drawable.ic_drag;
		if (position == currentSong) {
			switch (playerState) {
			case PLAY:
				drawableId = R.drawable.ic_play;
				break;
			case STOP:
				drawableId = R.drawable.ic_stop;
				break;
			case PAUSE:
				drawableId = R.drawable.ic_pause;
				break;
			}
		}
		
		ViewTag tag = (ViewTag)view.getTag();
		if (tag.drawable != drawableId) {
			ImageView imageView = (ImageView)view.findViewById(R.id.playlist_list_item_drag_image);
			imageView.setImageResource(drawableId);
			tag.drawable = drawableId;
		}

		TextView textView1 = (TextView)view.findViewById(R.id.playlist_list_item_text1);
		TextView textView2 = (TextView)view.findViewById(R.id.playlist_list_item_text2);
		TextView textViewTime = (TextView)view.findViewById(R.id.playlist_list_item_time);
		TextView textViewPriority = (TextView)view.findViewById(R.id.playlist_list_item_priority);
		
		PlaylistAdapterEntity entity = getItem(position);
		String priority = entity.getPriority();

		textView1.setText(entity.getSubText());
		textView2.setText(entity.getText());
		textViewTime.setText(entity.getTime());
		textViewPriority.setText(priority != null ? priority : "");
		
		if (entity.prioritized() != tag.prioritized) {
			if (entity.prioritized()) {
                int color = getPrioritizedColor();
				textView1.setTextColor(color);
				textView2.setTextColor(color);
			} else {
				textView1.setTextColor(tag.standardColors);
				textView2.setTextColor(tag.standardColors);
			}
			tag.prioritized = entity.prioritized();
		}
		
		return view;
	}
		
	public void setData(PlaylistAdapterEntity[] data, int currentSong, PlayerState playerState) {
		this.currentSong = currentSong;
		this.playerState = playerState;
		
		setNotifyOnChange(false);
	    clear();
	    if (data != null) {
	    	addAll(data);
	    }
		notifyDataSetChanged();
	}

    private int getPrioritizedColor() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.redFocusColor, typedValue, true);
        return typedValue.data;
    }

	private ViewTag createTag(int drawable, ColorStateList colorStateList) {
		ViewTag tag = new ViewTag();
		tag.drawable = drawable;
		tag.standardColors = colorStateList;
		tag.prioritized = false;
		return tag;
	}
	
	private static final class ViewTag {
		int drawable;
		boolean prioritized;
		ColorStateList standardColors;
	}
}

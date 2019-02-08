package net.prezz.mpr.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity.FileType;
import net.prezz.mpr.model.external.CoverReceiver;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class LibraryArrayAdapter extends ArrayAdapter<AdapterEntity> implements SectionIndexer {

	private ArrayList<String> sectionsList = new ArrayList<String>();
	private ArrayList<Integer> positionForSection = new ArrayList<Integer>();
	private ArrayList<Integer> sectionForPosition = new ArrayList<Integer>();
	private boolean showCover;
	private Integer coverSize;

	
    public LibraryArrayAdapter(Context context, int textViewResourceId) {
    	super(context, textViewResourceId);
    	throw new UnsupportedOperationException();
    }

    public LibraryArrayAdapter(Context context, int resource, int textViewResourceId) {
    	super(context, resource, textViewResourceId);
    	throw new UnsupportedOperationException();
    }

    public LibraryArrayAdapter(Context context, int textViewResourceId, AdapterEntity[] objects) {
    	super(context, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }

    public LibraryArrayAdapter(Context context, int resource, int textViewResourceId, AdapterEntity[] objects) {
    	super(context, resource, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }

    public LibraryArrayAdapter(Context context, int textViewResourceId, List<AdapterEntity> objects) {
    	super(context, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }

    public LibraryArrayAdapter(Context context, int resource, int textViewResourceId, List<AdapterEntity> objects) {
    	super(context, resource, textViewResourceId, objects);
    	throw new UnsupportedOperationException();
    }
	
    public LibraryArrayAdapter(Context context, int textViewResourceId, AdapterEntity[] objects, AdapterIndexStrategy sectionIndexStrategy, boolean showCover) {
    	super(context, textViewResourceId, objects);
    	
    	sectionIndexStrategy.createSectionIndexes(objects, sectionsList, positionForSection, sectionForPosition);

    	this.showCover = showCover;
    	if (showCover) {
    		coverSize = Integer.valueOf(context.getResources().getDimensionPixelSize(R.dimen.library_list_view_cover_size));
    	}
    }
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		View view = convertView;
		AdapterEntity entity = getItem(position);

		if (view == null && entity instanceof LibraryAdapterEntity) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.view_list_item_library, parent, false);
			view.setTag(new Wrapper(view, false, showCover, coverSize));
		} else if (view == null && entity instanceof UriAdapterEntity) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.view_list_item_library, parent, false);
			view.setTag(new Wrapper(view, false, false, null));
		} else if (view == null && entity instanceof SectionAdapterEntity) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(android.R.layout.preference_category, parent, false);
			view.setTag(new Wrapper(view, true, false, null));
        }
		
		Wrapper wrapper = (Wrapper) view.getTag();
		wrapper.setAdapterEntity(entity);

		return view;
	}
	
	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		AdapterEntity entity = getItem(position);
		if (entity instanceof LibraryAdapterEntity) {
			return 0;
		} else if (entity instanceof UriAdapterEntity) {
			return 1;
		} else {
			return 2;
		}
	}
	
	@Override
	public Object[] getSections() {
		return sectionsList.toArray();
	}

	@Override
	public int getPositionForSection(int section) {
		if (section >= positionForSection.size()) {
			section = positionForSection.size() - 1;
		}
		return positionForSection.get(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position >= sectionForPosition.size()) {
			position = sectionForPosition.size() - 1;
		}
		return sectionForPosition.get(position);
	}
	
	private static final class Wrapper {
		private View view;
		private TextView textView1;
		private TextView textView2;
		private TextView textViewTime;
		private ImageView coverView;
		private boolean showCover;
		private Integer coverSize;
		private TaskHandle loaderTask = TaskHandle.NULL_HANDLE;
		private AdapterEntity currentAdapterEntity;
        private ColorStateList standardColors1;
        private ColorStateList standardColors2;


        public Wrapper(View view, boolean isSection, boolean showCover, Integer coverSize) {
			this.view = view;
			if (!isSection) {
				this.showCover = showCover;
				this.coverSize = coverSize;
				this.textView1 = (TextView)view.findViewById(R.id.library_list_item_text1);
				this.textView2 = (TextView)view.findViewById(R.id.library_list_item_text2);
				this.textViewTime = (TextView)view.findViewById(R.id.library_list_item_time);
				this.coverView = (ImageView) view.findViewById(R.id.library_list_item_cover_image);
                this.standardColors1 = textView1.getTextColors();
                this.standardColors2 = textView2.getTextColors();
			}
		}
		
		public void setAdapterEntity(AdapterEntity entity) {
			if (currentAdapterEntity == entity) {
				return;
			}
			currentAdapterEntity = entity;
			if (entity instanceof LibraryAdapterEntity) {
				textView1.setText(((LibraryAdapterEntity) entity).getSubText());
				textView2.setText(entity.getText());
				textViewTime.setText(((LibraryAdapterEntity) entity).getTime() != null ? ((LibraryAdapterEntity) entity).getTime() : "");
				if (showCover) {
					loadCover((LibraryAdapterEntity) entity);
				}
			} else if (entity instanceof UriAdapterEntity) {
				textView1.setText(((UriAdapterEntity) entity).getSubText());
				textView2.setText(entity.getText());
				if (((UriAdapterEntity) entity).getEntity().getFileType() == FileType.PLAYLIST) {
                    textView1.setTextColor(standardColors1.withAlpha(128));
                    textView2.setTextColor(standardColors2.withAlpha(128));
				} else {
                    textView1.setTextColor(standardColors1);
                    textView2.setTextColor(standardColors2);
                }
			} else if (entity instanceof SectionAdapterEntity) {
				((TextView)view).setText(entity.getText());
			}
		}
		
		private void loadCover(LibraryAdapterEntity adapterEntity) {
			loaderTask.cancelTask();
			
			coverView.setImageBitmap(null);
			LibraryEntity entity = adapterEntity.getEntity();
			if (entity.getTag() == Tag.ALBUM) {
				coverView.setVisibility(View.VISIBLE);
				Boolean compilation = entity.getMetaCompilation();
				String artist = (compilation == Boolean.TRUE) ? null : entity.getLookupArtist();
                String album = entity.getLookupAlbum();
                loaderTask = ExternalInformationService.getCover(artist, album, coverSize, new CoverReceiver() {
					@Override
					public void receiveCover(Bitmap bitmap) {
						coverView.setImageBitmap(bitmap);
					}
				});
			} else {
				coverView.setVisibility(View.GONE);
			}
		}
	}
}

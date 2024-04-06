package net.prezz.mpr.ui.library.filtered;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.external.CoverReceiver;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.model.external.UrlReceiver;
import net.prezz.mpr.ui.CoverActivity;
import net.prezz.mpr.ui.adapter.AdapterEntity;
import net.prezz.mpr.ui.adapter.FilteredTrackTitleAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryAdapterEntity;
import net.prezz.mpr.ui.adapter.LibraryArrayAdapter;
import net.prezz.mpr.ui.adapter.SortedAdapterIndexStrategy;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.Boast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class FilteredTrackAndTitleActivity extends FilteredActivity implements ActivityResultCallback<ActivityResult> {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private TaskHandle getCoverHandle = TaskHandle.NULL_HANDLE;
    private TaskHandle lastFmHandle = TaskHandle.NULL_HANDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getCoverHandle.cancelTask();
        lastFmHandle.cancelTask();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_filtered_track_and_title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.filtered_change_cover:
            changeCover();
            return true;
        case R.id.filtered_clear_cover:
            setCover(ExternalInformationService.NULL_URL);
            return true;
        case R.id.filtered_lastfm_album:
            goToLastFm();
            return true;
         default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,    long id) {
    }

    @Override
    protected TaskHandle getEntities(LibraryEntity entity, ResponseReceiver<LibraryEntity[]> responseReceiver) {
        return MusicPlayerControl.getFilteredTracksAndTitlesFromLibrary(entity, responseReceiver);
    }

    @Override
    protected AdapterEntity[] createAdapterEntities(LibraryEntity[] entities) {
        AdapterEntity[] result = new AdapterEntity[entities.length];

        for (int i = 0; i < entities.length; i++) {
            result[i] = new FilteredTrackTitleAdapterEntity(entities[i]);
        }

        return result;
    }

    @Override
    protected ListAdapter createAdapter(AdapterEntity[] adapterEntities) {
        updateMainInfo(adapterEntities);
        return new LibraryArrayAdapter(this, android.R.layout.simple_list_item_1, adapterEntities, SortedAdapterIndexStrategy.INSTANCE, false);
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            String url = result.getData().getStringExtra(CoverActivity.URL_RESULT_KEY);
            setCover(url);
        }
    }

    private void setCover(String url) {
        String artist = getArtist(adapterEntities);
        String album = getAlbum(adapterEntities);
        if (Utils.nullOrEmpty(album)) {
            Boast.makeText(this, R.string.library_action_not_possible).show();
            return;
        }

        Integer maxHeight = (isLandscape()) ? null : Integer.valueOf(getResources().getDimensionPixelSize(R.dimen.library_album_cover_height));

        getCoverHandle.cancelTask();
        getCoverHandle = ExternalInformationService.setCoverUrl(artist, album, url, maxHeight, new CoverReceiver() {
            @Override
            public void receiveCover(Bitmap bitmap) {
                ImageView imageView = (ImageView)findViewById(R.id.filtered_track_title_cover_image);
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        });

        //in case of compilation, set cover for all artists
        Set<String> allArtists = getAllArtists(adapterEntities);
        allArtists.remove(artist);
        for (String a : allArtists) {
            ExternalInformationService.setCoverUrl(a, album, url, maxHeight, null);
        }
    }
    
    private void changeCover() {
        String artist = getArtist(adapterEntities);
        String album = getAlbum(adapterEntities);
        if (Utils.nullOrEmpty(album)) {
            Boast.makeText(this, R.string.library_action_not_possible).show();
            return;
        }

        Intent intent = new Intent(this, CoverActivity.class);
        Bundle args = new Bundle();
        args.putString(CoverActivity.ARTIST_ARGUMENT_KEY, artist);
        args.putString(CoverActivity.ALBUM_ARGUMENT_KEY, album);
        intent.putExtras(args);
        activityResultLauncher.launch(intent);
    }

    private void goToLastFm() {
        String artist = getArtist(adapterEntities);
        String album = getAlbum(adapterEntities);
        if (Utils.nullOrEmpty(artist) || Utils.nullOrEmpty(album)) {
            Boast.makeText(this, R.string.library_action_not_possible).show();
            return;
        }

        lastFmHandle.cancelTask();
        lastFmHandle = ExternalInformationService.getAlbumInfoUrls(artist, album, new UrlReceiver() {
            @Override
            public void receiveUrls(String[] urls) {
                if (urls != null && urls.length > 0) {
                    String url = urls[0];
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    Boast.makeText(FilteredTrackAndTitleActivity.this, R.string.library_action_not_possible).show();
                }
            }
        });
    }

    private void updateMainInfo(AdapterEntity[] entities) {
        TextView textViewCount = (TextView)findViewById(R.id.filtered_track_title_album_track_count);
        if (textViewCount != null) {
            textViewCount.setText(getCount(entities));
        }

        TextView textViewLength = (TextView)findViewById(R.id.filtered_track_title_album_length);
        if (textViewLength != null) {
            textViewLength.setText(getString(R.string.library_length, getLength(entities)));
        }

        TextView textViewYear = (TextView)findViewById(R.id.filtered_track_title_album_year);
        if (textViewYear != null) {
            textViewYear.setText(getString(R.string.library_year, getYear(entities)));
        }

        TextView textViewGenre = (TextView)findViewById(R.id.filtered_track_title_album_genre);
        if (textViewGenre != null) {
            textViewGenre.setText(getString(R.string.library_genre, getGenre(entities)));
        }

        String artist = getArtist(entities);
        String album = getAlbum(entities);
        Integer maxHeight = (isLandscape()) ? null : Integer.valueOf(getResources().getDimensionPixelSize(R.dimen.library_album_cover_height));
        getCoverHandle.cancelTask();
        getCoverHandle = ExternalInformationService.getCover(artist, album, maxHeight, new CoverReceiver() {
            @Override
            public void receiveCover(Bitmap bitmap) {
                if (bitmap != null) {
                    ImageView imageView = (ImageView)findViewById(R.id.filtered_track_title_cover_image);
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }

    private String getCount(AdapterEntity[] entities) {
        StringBuilder sb = new StringBuilder();

        int trackCount = entities.length;
        sb.append(trackCount);
        sb.append(" ");
        if (trackCount == 1) {
            sb.append(getString(R.string.library_track_count));
        } else {
            sb.append(getString(R.string.library_tracks_count));
        }

        return sb.toString();
    }

    private String getLength(AdapterEntity[] entities) {
        StringBuilder sb = new StringBuilder();

        int length = 0;
        for (AdapterEntity e : entities) {
            length += ((LibraryAdapterEntity)e).getEntity().getMetaLength();
        }
        int hours = length / 3600;
        int remaining = length % 3600;
        int minutes = remaining / 60;
        int seconds = remaining % 60;
        if (hours > 0) {
            sb.append(String.format("%d:%02d:%02d", hours, minutes, seconds));
        } else {
            sb.append(String.format("%d:%02d", minutes, seconds));
        }

        return sb.toString();
    }

    private String getYear(AdapterEntity[] entities) {
        TreeSet<Integer> years = new TreeSet<Integer>();
        for (AdapterEntity e : entities) {
            Integer year = ((LibraryAdapterEntity)e).getEntity().getMetaYear();
            if (year != null) {
                years.add(year);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Integer year : years) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(year);
        }

        return sb.toString();
    }

    private String getGenre(AdapterEntity[] entities) {
        TreeSet<String> genres = new TreeSet<String>();
        for (AdapterEntity e : entities) {
            String genre = ((LibraryAdapterEntity)e).getEntity().getMetaGenre();
            if (genre != null) {
                genres.add(genre);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String genre : genres) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(genre);
        }

        return sb.toString();
    }

    private Set<String> getAllArtists(AdapterEntity[] entities) {
        Set<String> artists = new HashSet<String>();
        if (entities != null) {
            for (AdapterEntity entity : entities) {
                artists.add(((LibraryAdapterEntity)entity).getEntity().getArtist());
            }
        }
        return artists;
    }

    private String getArtist(AdapterEntity[] entities) {
        Set<String> artists = getAllArtists(entities);
        return artists.size() == 1 ? artists.iterator().next() : null;
    }

    private String getAlbum(AdapterEntity[] entities) {
        Set<String> albums = new HashSet<String>();
        if (entities != null) {
            for (AdapterEntity entity : entities) {
                albums.add(((LibraryAdapterEntity)entity).getEntity().getAlbum());
            }
        }

        return albums.size() == 1 ? albums.iterator().next() : null;
    }

    private boolean isLandscape() {

        Display display = this.getDisplay();
        int rotation = display.getRotation();
        return (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270);
    }
}

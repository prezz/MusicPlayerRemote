package net.prezz.mpr.ui;

import java.util.Arrays;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.external.CoverReceiver;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.model.external.UrlReceiver;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.view.DataFragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.appcompat.app.AppCompatActivity;

public class CoverActivity extends AppCompatActivity implements OnEditorActionListener {

    public static final String ARTIST_ARGUMENT_KEY = "artist";
    public static final String ALBUM_ARGUMENT_KEY = "album";

    public static final String URL_RESULT_KEY = "url";

    public static final String INDEX_SAVED_INSTANCE_STATE = "saved_index";
    public static final String URLS_SAVED_INSTANCE_STATE = "saved_urls";

    private Integer coverIndex = Integer.valueOf(0);
    private String[] coverUrls;
    private TaskHandle getCoverHandle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_cover);

        String artist = getArgument(ARTIST_ARGUMENT_KEY);
        if (artist != null) {
            TextView artistText = (TextView)findViewById(R.id.cover_artist_text);
            artistText.setText(artist);
        }

        String album = getArgument(ALBUM_ARGUMENT_KEY);
        TextView albumText = (TextView)findViewById(R.id.cover_album_text);
        if (album != null) {
            albumText.setText(album);
        }
        albumText.setOnEditorActionListener(this);

        getCoverHandle = TaskHandle.NULL_HANDLE;

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            coverIndex = (Integer) dataFragment.getData(INDEX_SAVED_INSTANCE_STATE, Integer.valueOf(0));

            //restore entities if loaded into memory again (or after rotation)
            Object[] objectEntities = (Object[]) dataFragment.getData(URLS_SAVED_INSTANCE_STATE, null);
            if (objectEntities != null) {
                coverUrls = Arrays.copyOf(objectEntities, objectEntities.length, String[].class);
            }
        }

        if (coverUrls != null) {
            toggleButtonEnablement(false);
            getCover(coverIndex);
        } else {
            getCoverUrls(artist, album);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        getCoverHandle.cancelTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DataFragment dataFragment = DataFragment.getSaveFragment(this, getClass());
        if (dataFragment != null) {
            dataFragment.setData(INDEX_SAVED_INSTANCE_STATE, coverIndex);
            dataFragment.setData(URLS_SAVED_INSTANCE_STATE, coverUrls);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            TextView artistText = (TextView)findViewById(R.id.cover_artist_text);
            TextView albumText = (TextView)findViewById(R.id.cover_album_text);

            getCoverUrls(artistText.getText().toString(), albumText.getText().toString());

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(albumText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    public void onPreviousClick(View view) {
        if (--coverIndex < 0) {
            coverIndex = coverUrls.length - 1;
        }

        toggleButtonEnablement(false);
        getCover(coverIndex);
    }

    public void onSelectClick(View view) {
        if (coverIndex >= 0 && coverIndex < coverUrls.length) {
            Intent returnValue = new Intent();
            returnValue.putExtra(URL_RESULT_KEY, coverUrls[coverIndex]);
            setResult(RESULT_OK, returnValue);
            finish();
        }
    }

    public void onNextClick(View view) {
        if (++coverIndex >= coverUrls.length) {
            coverIndex = 0;
        }

        toggleButtonEnablement(false);
        getCover(coverIndex);
    }

    private void getCoverUrls(String artist, String album) {
        if (!Utils.nullOrEmpty(album)) {
            toggleButtonEnablement(false);

            ImageView imageView = (ImageView)findViewById(R.id.cover_album_image);
            TextView textView = (TextView)findViewById(R.id.cover_index_text);
            if (imageView != null) {
                imageView.setImageBitmap(null);
            }
            if (textView != null) {
                textView.setText(R.string.cover_searching_covers);
            }

            getCoverHandle.cancelTask();
            getCoverHandle = ExternalInformationService.getCoverUrls(artist, album, new UrlReceiver() {
                @Override
                public void receiveUrls(String[] urls) {
                    coverIndex = 0;
                    coverUrls = urls;
                    getCover(coverIndex);
                }
            });
        }
    }

    private void getCover(int index) {
        setCurrentCoverText(index);
        if (coverIndex >= 0 && coverIndex < coverUrls.length) {
            getCoverHandle.cancelTask();
            getCoverHandle = ExternalInformationService.getCover(coverUrls[index], new CoverReceiver() {
                @Override
                public void receiveCover(Bitmap bitmap) {
                    toggleButtonEnablement(true);
                    ImageView imageView = (ImageView)findViewById(R.id.cover_album_image);
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
        }
    }

    private void setCurrentCoverText(int index) {
        TextView textView = (TextView)findViewById(R.id.cover_index_text);
        if (textView != null) {
            if (coverUrls != null && coverUrls.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(index + 1);
                sb.append("/");
                sb.append(coverUrls.length);
                textView.setText(sb.toString());
            } else {
                textView.setText(R.string.cover_no_covers);
            }
        }
    }

    private String getArgument(String argumentKey) {
        String arg = (String) this.getIntent().getExtras().getString(argumentKey);
        return arg;
    }

    private void toggleButtonEnablement(boolean enabled) {
        Button prevButton = (Button)findViewById(R.id.cover_button_previous);
        prevButton.setEnabled(enabled);

        Button selectButton = (Button)findViewById(R.id.cover_button_select);
        selectButton.setEnabled(enabled);

        Button nextButton = (Button)findViewById(R.id.cover_button_next);
        nextButton.setEnabled(enabled);
    }
}

package net.prezz.mpr.ui.settings.servers;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.servers.ServerConfiguration;
import net.prezz.mpr.model.servers.ServerConfigurationService;
import net.prezz.mpr.R;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddEditServerActivity extends Activity implements OnEditorActionListener {

    public static final String CONFIGURATION_ARGUMENT_KEY = "serverConfiguration";
    private ServerConfiguration editConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_add_edit_server);

        editConfiguration = (ServerConfiguration)getIntent().getExtras().getSerializable(CONFIGURATION_ARGUMENT_KEY);
        if (editConfiguration != null) {
            setTitle(R.string.add_edit_server_edit_title);
            setTextViewText(R.id.add_edit_server_name_text, editConfiguration.getName());
            setTextViewText(R.id.add_edit_server_host_text, editConfiguration.getHost());
            setTextViewText(R.id.add_edit_server_port_text, editConfiguration.getPort());
            setTextViewText(R.id.add_edit_server_password_text, editConfiguration.getPassword());
            setStreamingViewUrl(editConfiguration.getStreaming());
        } else {
            setTitle(R.string.add_edit_server_add_title);
            setTextViewText(R.id.add_edit_server_port_text, "6600");
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        TextView passwordTextView = (TextView) findViewById(R.id.add_edit_server_password_text);
        passwordTextView.setOnEditorActionListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (addServer()) {
                setResult(RESULT_OK);
                finish();
            }
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

    public void onStreamingClick(View view) {
        Switch streamingSwitch = (Switch) view;
        if (streamingSwitch.isChecked()) {
            String host = getTextViewText(R.id.add_edit_server_host_text);
            String port = getTextViewText(R.id.add_edit_server_port_text);

            StringBuilder sb = new StringBuilder();
            if (!host.startsWith("http")) {
                sb.append("http://");
            }
            sb.append(host);

            try {
                int portNumber = Integer.parseInt(port);
                int delta = portNumber - 6600;
                sb.append(":" + Integer.toString(8000 + delta));
            } catch (NumberFormatException ex) {
                sb.append(":8000");
            }

            setStreamingViewUrl(sb.toString());
        } else {
            setStreamingViewUrl("");
        }
    }

    public void onSaveClick(View view) {
        if (addServer()) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean addServer() {
        if (!isFinishing()) {
            String name = getTextViewText(R.id.add_edit_server_name_text);
            String host = getTextViewText(R.id.add_edit_server_host_text);
            String port = getTextViewText(R.id.add_edit_server_port_text);
            String password = getTextViewText(R.id.add_edit_server_password_text);
            String streaming = getTextViewText(R.id.add_edit_server_streaming_url_text);

            if ("".equals(name)) {
                name = host;
            }

            if (!host.isEmpty() && !port.isEmpty()) {
                if (editConfiguration != null) {
                    int id = editConfiguration.getId();
                    ServerConfigurationService.updateServerConfiguration(new ServerConfiguration(id, name, host, port, password, streaming));
                } else {
                    ServerConfigurationService.addServerConfiguration(new ServerConfiguration(name, host, port, password, streaming));
                }
                return true;
            }
        }

        return false;
    }

    private void setTextViewText(int id, String text) {
        TextView textView = (TextView) findViewById(id);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private String getTextViewText(int id) {
        TextView textView = (TextView) findViewById(id);
        if (textView != null) {
            return textView.getText().toString();
        }

        return null;
    }

    private void setStreamingViewUrl(String url) {
        Switch streamingSwitch = (Switch) findViewById(R.id.add_edit_server_streaming_url_switch);
        TextView streamingText = (TextView) findViewById(R.id.add_edit_server_streaming_url_text);

        if (Utils.nullOrEmpty(url)) {
            if (streamingSwitch != null) {
                streamingSwitch.setChecked(false);
            }

            if (streamingText != null) {
                streamingText.setVisibility(View.GONE);
                streamingText.setText("");
            }
        } else {
            if (streamingSwitch != null) {
                streamingSwitch.setChecked(true);
            }

            if (streamingText != null) {
                streamingText.setVisibility(View.VISIBLE);
                streamingText.setText(url);
            }
        }
    }
}

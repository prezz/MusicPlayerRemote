package net.prezz.mpr.ui.mpd;

import android.content.Context;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.servers.ServerConfiguration;
import net.prezz.mpr.model.servers.ServerConfigurationService;
import net.prezz.mpr.mpd.MpdSettings;

public class MpdPlayerSettings implements MpdSettings {

    private String name;
    private String mpdHost;
    private String mpdPort;
    private String mpdPassword;
    private String mpdStreamingUrl;

    protected MpdPlayerSettings(String name, String mpdHost, String mpdPort, String mpdPassword, String mpdStreamingUrl) {
        this.name = name;
        this.mpdHost = mpdHost;
        this.mpdPort = mpdPort;
        this.mpdPassword = mpdPassword;
        this.mpdStreamingUrl = mpdStreamingUrl;
    }

    public static MpdPlayerSettings create(Context context) {
        ServerConfiguration selectedConfiguration = ServerConfigurationService.getSelectedServerConfiguration();

        String name = selectedConfiguration.getName();
        String mpdHost = selectedConfiguration.getHost();
        String mpdPort = selectedConfiguration.getPort();
        String mpdPassword = selectedConfiguration.getPassword();
        String mpdStreamingUrl = selectedConfiguration.getStreaming();

        return new MpdPlayerSettings(name, mpdHost, mpdPort, mpdPassword, mpdStreamingUrl);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMpdHost() {
        return mpdHost;
    }

    @Override
    public String getMpdPort() {
        return mpdPort;
    }

    @Override
    public String getMpdPassword() {
        return mpdPassword;
    }

    public String getMpdStreamingUrl() {
        return mpdStreamingUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof MpdPlayerSettings) {
            MpdPlayerSettings other = (MpdPlayerSettings) obj;

            if (!(Utils.equals(mpdHost, other.mpdHost))) {
                return false;
            }
            if (!(Utils.equals(mpdPort, other.mpdPort))) {
                return false;
            }
            if (!(Utils.equals(mpdPassword, other.mpdPassword))) {
                return false;
            }
            if (!(Utils.equals(mpdStreamingUrl, other.mpdStreamingUrl))) {
                return false;
            }

            return true;
        }

        return false;
    }
}

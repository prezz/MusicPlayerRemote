package net.prezz.mpr.ui.adapter;

import android.annotation.SuppressLint;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.UriEntity;

public class PlaylistAdapterEntity implements AdapterEntity {

    private static final long serialVersionUID = -1787348995732040871L;

    private PlaylistEntity entity;
    private boolean showPriority;


    public PlaylistAdapterEntity(PlaylistEntity playlistEntity, boolean showPriority) {
        this.entity = playlistEntity;
        this.showPriority = showPriority;
    }

    public PlaylistEntity getEntity() {
        return entity;
    }

    @Override
    public String getSectionIndexText() {
        return getText();
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();

//        Integer metaDisc = getEntity().getDisc();
//        if (metaDisc != null) {
//            sb.append(String.format("(%d) ", metaDisc));
//        }

        Integer track = entity.getTrack();
        if (track != null) {
            sb.append(String.format("%02d - ", track));
        }

        String title = entity.getTitle();
        if (title != null) {
            sb.append(title);
        } else {
            UriEntity uri = entity.getUriEntity();
            if (uri != null) {
                sb.append(uri.getUriFilname());
            }
        }

        return sb.toString();
    }

    public String getSubText() {
        StringBuilder sb = new StringBuilder();

        String artist = entity.getArtist();
        if (artist != null) {
            sb.append(artist);
        }

        String album = entity.getAlbum();
        if (album != null) {
            sb.append(" - ");
            sb.append(album);
        }

        if (sb.length() == 0) {
            String name = entity.getName();
            if (name != null) {
                sb.append(name);
            } else {
                UriEntity uri = entity.getUriEntity();
                if (uri != null) {
                    sb.append(uri.getUriFilname());
                }
            }
        }

        return sb.toString();
    }

    @SuppressLint("DefaultLocale")
    public String getTime() {
        Integer time = entity.getTime();
        return (time != null) ? String.format("%d:%02d", time.intValue() / 60, time.intValue() % 60) : "";
    }

    public String getPriority() {
        Integer priority = entity.getPriority();
        return (showPriority && priority != null) ? priority.toString() : null;
    }

    public boolean prioritized() {
        return entity.getPriority() != null && entity.getPriority().intValue() > 0;
    }
}

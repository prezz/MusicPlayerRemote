package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.R;

public class UriAdapterEntity implements AdapterEntity {

    private static final long serialVersionUID = 6951202513706174870L;

    private UriEntity uriEntity;


    public UriAdapterEntity(UriEntity uriEntity) {
        this.uriEntity = uriEntity;
    }

    @Override
    public String getSectionIndexText() {
        return uriEntity.getUriPath();
    }

    @Override
    public String getText() {
        return uriEntity.getUriPath();
    }

    public String getSubText() {
        switch (uriEntity.getUriType()) {
        case DIRECTORY:
            return ApplicationActivator.getContext().getString(R.string.library_directory);
        case FILE:
            return ApplicationActivator.getContext().getString((uriEntity.getFileType() == UriEntity.FileType.PLAYLIST) ? R.string.library_file_playlist : R.string.library_file);
        }

        return "";
    }

    public UriEntity getEntity() {
        return uriEntity;
    }
}

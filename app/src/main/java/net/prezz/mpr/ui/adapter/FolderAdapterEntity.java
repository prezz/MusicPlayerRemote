package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;

public class FolderAdapterEntity extends LibraryAdapterEntity {

    public FolderAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getText() {
        return getEntity().getUriEntity().getFullUriPath(true);
    }

    @Override
    public String getSubText() {
        return null;
    }

    @Override
    public String getTime() {
        return null;
    }
}

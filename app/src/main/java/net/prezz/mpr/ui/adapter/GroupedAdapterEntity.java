package net.prezz.mpr.ui.adapter;

import net.prezz.mpr.model.LibraryEntity;

public class GroupedAdapterEntity extends LibraryAdapterEntity {

    public GroupedAdapterEntity(LibraryEntity entity) {
        super(entity);
    }

    @Override
    public String getText() {
        return getEntity().getUriPath();
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

package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

public class MpdGetGenresCommand extends MpdDatabaseCommand<LibraryEntity, LibraryEntity[]> {


    public MpdGetGenresCommand(LibraryEntity entity) {
        super(entity);
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, LibraryEntity entity) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        Cursor c = databaseHelper.selectGenres(entity);

        try {
            int i = 0;
            LibraryEntity[] result = new LibraryEntity[c.getCount()];
            if (c.moveToFirst()) {
                do {
                    String genre = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    metaCount += c.getInt(2);
                    metaCount += c.getInt(3);
                    result[i++] = entityBuilder.clear().setTag(Tag.GENRE).setGenre(genre).setUriEntity(entity.getUriEntity()).setUriFilter(entity.getUriFilter()).setMetaCount(metaCount).build();
                } while (c.moveToNext());
            }
            return result;
        } finally {
            c.close();
        }
    }

    @Override
    protected LibraryEntity[] onError() {
        return new LibraryEntity[0];
    }
}

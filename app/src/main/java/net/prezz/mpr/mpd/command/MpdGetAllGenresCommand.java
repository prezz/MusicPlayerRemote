package net.prezz.mpr.mpd.command;

import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.LibraryEntity.Builder;
import net.prezz.mpr.model.LibraryEntity.Tag;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;
import android.database.Cursor;

import java.util.Collections;
import java.util.Set;

public class MpdGetAllGenresCommand extends MpdDatabaseCommand<MpdGetAllGenresCommand.Param, LibraryEntity[]> {

    protected static final class Param {
        public final UriEntity uriEntity;
        public final Set<String> uriFilter;

        public Param(UriEntity uriEntity, Set<String> uriFilter) {
            this.uriEntity = uriEntity;
            this.uriFilter = (uriFilter != null) ? Collections.unmodifiableSet(uriFilter) : null;
        }
    }

    public MpdGetAllGenresCommand(UriEntity uriEntity, Set<String> uriFilter) {
        super(new Param(uriEntity, uriFilter));
    }

    @Override
    protected LibraryEntity[] doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {
        Builder entityBuilder = LibraryEntity.createBuilder();

        UriEntity uriEntity = param.uriEntity;
        Set<String> uriFilter = param.uriFilter;

        Cursor c = databaseHelper.selectAllGenres(uriEntity, uriFilter);

        try {
            int i = 0;
            LibraryEntity[] result = new LibraryEntity[c.getCount()];
            if (c.moveToFirst()) {
                do {
                    String genre = c.getString(0);
                    Integer metaCount = c.getInt(1);
                    metaCount += c.getInt(2);
                    metaCount += c.getInt(3);
                    result[i++] = entityBuilder.clear().setTag(Tag.GENRE).setGenre(genre).setUriEntity(uriEntity).setUriFilter(uriFilter).setMetaCount(metaCount).build();
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

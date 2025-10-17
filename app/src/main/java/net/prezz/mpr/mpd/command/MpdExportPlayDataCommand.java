package net.prezz.mpr.mpd.command;

import android.database.Cursor;

import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MpdExportPlayDataCommand extends MpdDatabaseCommand<MpdExportPlayDataCommand.Param, String> {

    protected static final class Param {
        public final int offset;
        public final int limit;

        public Param(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }
    }

    public MpdExportPlayDataCommand(int offset, int limit) {
        super(new Param(offset, limit));
    }

    @Override
    protected String doExecute(MpdLibraryDatabaseHelper databaseHelper, Param param) throws Exception {

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("Artist", "Album", "Title", "Date", "Count")
                .setSkipHeaderRecord(false)
                .get();

        StringBuilder csvStringBuilder = new StringBuilder();
        CSVPrinter csvPrinter = new CSVPrinter(csvStringBuilder, csvFormat);

        Cursor c = databaseHelper.exportPlayData(param.offset, param.limit);
        try {
            if (c.moveToFirst()) {
                do {
                    String artist = c.getString(0);
                    String album = c.getString(1);
                    String title = c.getString(2);
                    LocalDate playDate = LocalDate.parse(c.getString(3), DateTimeFormatter.ISO_DATE);
                    Integer playCount = c.getInt(4);

                    csvPrinter.printRecord(artist, album, title, playDate.toString(), playCount);
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }

        return csvStringBuilder.toString();
    }

    @Override
    protected String onError() {
        return "Export failed";
    }
}

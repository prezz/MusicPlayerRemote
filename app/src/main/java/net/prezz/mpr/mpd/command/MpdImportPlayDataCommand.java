package net.prezz.mpr.mpd.command;

import net.prezz.mpr.mpd.database.MpdLibraryDatabaseHelper;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MpdImportPlayDataCommand extends MpdDatabaseCommand<String, Boolean> {

    public MpdImportPlayDataCommand(String csvData) {
        super(csvData);
    }

    @Override
    protected Boolean doExecute(MpdLibraryDatabaseHelper databaseHelper, String param) throws Exception {

        String[] header = new String[] {"Artist", "Album", "Title", "Date", "Count"};
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(header)
                .setSkipHeaderRecord(false)
                .get();

        StringReader csvReader = new StringReader(param);
        CSVParser csvParser = CSVParser.parse(csvReader, csvFormat);
        List<CSVRecord> csvRecords = csvParser.getRecords();

        if (csvRecords.isEmpty()) {
            return Boolean.FALSE;
        }

        if (!Arrays.equals(csvRecords.get(0).values(), header)) {
            return Boolean.FALSE;
        }

        databaseHelper.beginTransaction();
        try {
            for (int i = 1; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);

                String artist = record.get(header[0]);
                String album = record.get(header[1]);
                String title = record.get(header[2]);
                LocalDate playData = LocalDate.parse(record.get(header[3]), DateTimeFormatter.ISO_DATE);
                int playCount = isPositive(Integer.parseInt(record.get(header[4])));

                databaseHelper.upsertPlayData(artist, album, title, playData.format(DateTimeFormatter.ISO_DATE), playCount);
            }

            databaseHelper.setTransactionSuccessful();
        } finally {
            databaseHelper.endTransaction();
        }

        return Boolean.TRUE;
    }

    @Override
    protected Boolean onError() {
        return Boolean.FALSE;
    }

    private static int isPositive(int value) {
        if (value < 1) {
            throw new IllegalArgumentException();
        }
        return value;
    }
}

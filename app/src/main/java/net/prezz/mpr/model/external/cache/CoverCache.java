package net.prezz.mpr.model.external.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import net.prezz.mpr.ui.ApplicationActivator;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class CoverCache {

	private Context context;
	private CoverCacheDatabaseHelper coverCacheDatabaseHelper;
	
	public CoverCache() {
		context = ApplicationActivator.getContext();
		coverCacheDatabaseHelper = new CoverCacheDatabaseHelper(context);
	}
	
	public String getCoverUrl(String artist, String album) {
		try {
			Cursor cursor = coverCacheDatabaseHelper.getCoverUrl(artist, album);
			try {
				if (cursor.moveToFirst()) {
					String url = cursor.getString(0);
					return url;
				}
			} finally {
				cursor.close();
				coverCacheDatabaseHelper.close(); 
			}
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error getting cover url", ex);
		}
		
		return null;
	}

	public void insertCoverUrl(String artist, String album, String coverUrl) {
		try {
			coverCacheDatabaseHelper.insertCoverUrl(artist, album, coverUrl);
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error inserting cover url", ex);
		} finally {
            coverCacheDatabaseHelper.close();
        }
    }
	
	public void deleteCoverUrl(String artist, String album) {
		try {
			coverCacheDatabaseHelper.deleteCoverUrl(artist, album);
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error deleting cover url", ex);
		} finally {
            coverCacheDatabaseHelper.close();
        }
    }
	
	public byte[] getCoverImage(String coverUrl) throws IOException {
		try {
			String filename = getCoverFile(coverUrl);
			if (filename != null) {
				return readFile(new File(filename));
			}
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error loading cover image", ex);
		}
		
		return null;
	}
	
	public void insertCoverImage(String coverUrl, byte[] imageData) throws IOException {
		try {
			File cacheDir = context.getCacheDir();
			File coverFile = new File(cacheDir, UUID.randomUUID().toString());
			writeFile(coverFile, imageData);
			
			coverCacheDatabaseHelper.insertCoverFile(coverUrl, coverFile.getAbsolutePath());
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error saving cover image", ex);
		} finally {
            coverCacheDatabaseHelper.close();
        }
    }
	
	public void deleteCoverImageIfLastUsage(String coverUrl) {
		try {
			int useCount = 0;
			Cursor cursor = coverCacheDatabaseHelper.getUrlUseCount(coverUrl);
			try {
				if (cursor.moveToFirst()) {
					useCount = cursor.getInt(0);
				}
			} finally {
				cursor.close();
			}

            if (useCount <= 1) {
                String filename = getCoverFile(coverUrl);
                coverCacheDatabaseHelper.deleteCoverFile(coverUrl);
                if (filename != null) {
                    File file = new File(filename);
                    file.delete();
                }
            }
		} catch (Exception ex) {
			Log.e(CoverCache.class.getName(), "Error deleting cover image", ex);
		} finally {
            coverCacheDatabaseHelper.close();
        }
    }
				
	private String getCoverFile(String coverUrl) throws IOException {
		Cursor cursor = coverCacheDatabaseHelper.getCoverFile(coverUrl);
		try {
			if (cursor.moveToFirst()) {
				String filename = cursor.getString(0);
				return filename;
			}
		} finally {
			cursor.close();
			coverCacheDatabaseHelper.close(); 
		}

		return null;
	}
	
	private void writeFile(File coverFile, byte[] imageData) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(coverFile);
		try {
			outputStream.write(imageData);
			outputStream.flush();
 		} finally {
			outputStream.close();
 		}
	}
	
	private byte[] readFile(File coverFile) throws IOException {
		if (coverFile.exists()) {
			FileInputStream inputStream = new FileInputStream(coverFile);
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

				int len;
				byte[] buffer = new byte[2048];
				while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				
				outputStream.flush();
				return outputStream.toByteArray();
			} finally {
				inputStream.close();
			}
		}
		return null;
	}
}

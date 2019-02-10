package net.prezz.mpr.model.external;

import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.TaskHandleImpl;
import net.prezz.mpr.model.external.amazon.AmazonCoverService;
import net.prezz.mpr.model.external.cache.CoverCache;
import net.prezz.mpr.model.external.gracenote.GracenoteCoverService;
import net.prezz.mpr.model.external.lastfm.LastFmCoverAndInfoService;
import net.prezz.mpr.model.external.local.HttpCoverService;
import net.prezz.mpr.model.external.local.MpdCoverService;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ExternalInformationService {

	public static final String NULL_URL = "";
	private static final int MAX_HEIGHT = 1024;
	private static final Object lock = new Object();
	
	private static final String ASCII =
		      "AaEeIiOoUu"    // grave
		    + "AaEeIiOoUuYy"  // acute
		    + "AaEeIiOoUuYy"  // circumflex
		    + "AaOoNn"        // tilde
		    + "AaEeIiOoUuYy"  // umlaut
		    + "Aa"            // ring
		    + "Cc"            // cedilla
		    + "OoUu"          // double acute
		    ;

		private static final String UNICODE =
		      "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
		    + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
		    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
		    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
		    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
		    + "\u00C5\u00E5"
		    + "\u00C7\u00E7"
		    + "\u0150\u0151\u0170\u0171"
		    ;	
	
	private static CoverCache coverCache = new CoverCache();
	private static MpdCoverService mpdCoverService = new MpdCoverService();
	private static HttpCoverService httpCoverService = new HttpCoverService();
    private static LastFmCoverAndInfoService lastFmCoverAndInfoService = new LastFmCoverAndInfoService();
    private static GracenoteCoverService gracenoteCoverService = new GracenoteCoverService();
	private static AmazonCoverService amazonCoverService = new AmazonCoverService();
    private static Executor executor = Utils.createExecutor();
	
	
	private ExternalInformationService() {
	}
	
	public static TaskHandle getCover(String artist, String album, Integer maxHeight, final CoverReceiver coverReceiver) {
		AsyncTask<Object, Object, Bitmap> task = new AsyncTask<Object, Object, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				synchronized (lock) {
					try {
						String artistParam = (params[0] != null) ? (String)params[0] : "";
						String albumParam = (params[1] != null) ? (String)params[1] : "";

                        byte[] coverData = null;

						String coverUrl = coverCache.getCoverUrl(artistParam, albumParam);
						if (coverUrl == null) {
                            coverUrl = getCoverUrlInternal(artistParam, albumParam);

                            if (!NULL_URL.equals(coverUrl)) {
                                coverData = downloadCoverImage(coverUrl);
                                if (coverData != null) {
                                    coverCache.insertCoverImage(coverUrl, coverData);
                                } else {
                                    coverUrl = NULL_URL;
                                }
                            }

							coverCache.insertCoverUrl(artistParam, albumParam, coverUrl);
						}
						
						if (NULL_URL.equals(coverUrl)) {
							return getNoCoverImage((Integer) params[2]);
						}

                        if (coverData == null) {
                            coverData = coverCache.getCoverImage(coverUrl);
                        }

						if (coverData != null) {
							try {
								Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
								return scaleImage((Integer) params[2], bitmap);
							} catch (Exception ex) {
								Log.e(ExternalInformationService.class.getName(), "Error decoding byte array to bitmap", ex);
                                coverData = null;
							}
						}
	
						coverCache.deleteCoverImageIfLastUsage(coverUrl);
                        coverCache.deleteCoverUrl(artistParam, albumParam);

                        coverUrl = getCoverUrlInternal(artistParam, albumParam);

                        if (!NULL_URL.equals(coverUrl)) {
                            coverData = downloadCoverImage(coverUrl);
                            if (coverData != null) {
                                coverCache.insertCoverImage(coverUrl, coverData);
                            } else {
                                coverUrl = NULL_URL;
                            }
                        }

                        coverCache.insertCoverUrl(artistParam, albumParam, coverUrl);

                        if (NULL_URL.equals(coverUrl)) {
                            return getNoCoverImage((Integer)params[2]);
                        }

                        if (coverData != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
                            return scaleImage((Integer)params[2], bitmap);
                        }

						return getNoCoverImage((Integer)params[2]);
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
					}
					
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				coverReceiver.receiveCover(result);
			}
		};

		try {
			return new TaskHandleImpl<Object, Object, Bitmap>(task.executeOnExecutor(executor, artist, album, maxHeight));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}

	private static String getCoverUrlInternal(String artistParam, String albumParam) {

        List<String> coverUrlList = new ArrayList<>();

        coverUrlList.addAll(mpdCoverService.getCoverUrls(artistParam, albumParam));
        if (!coverUrlList.isEmpty()) {
            return coverUrlList.get(0);
        }

		coverUrlList.addAll(httpCoverService.getCoverUrls(artistParam, albumParam));
		if (!coverUrlList.isEmpty()) {
			return coverUrlList.get(0);
		}

		List<String> artists = createQueryStrings(artistParam, false);
		List<String> albums = createQueryStrings(albumParam, true);
		artists.add(null);

		for (String artist : artists) {
			for (String album : albums) {
				coverUrlList = lastFmCoverAndInfoService.getCoverUrls(artist, album);
				if (!coverUrlList.isEmpty()) {
                    return coverUrlList.get(0);
				}
				coverUrlList = gracenoteCoverService.getCoverUrls(artist, album);
				if (!coverUrlList.isEmpty()) {
                    return coverUrlList.get(0);
				}
				coverUrlList = amazonCoverService.getCoverUrls(artist, album);
				if (!coverUrlList.isEmpty()) {
                    return coverUrlList.get(0);
				}
			}
		}

        return NULL_URL;
	}

	public static TaskHandle getCover(String url, final CoverReceiver coverReceiver) {
		AsyncTask<Object, Object, Bitmap> task = new AsyncTask<Object, Object, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				synchronized (lock) {
					try {
						String url = (String)params[0];
						if (Utils.nullOrEmpty(url)) {
							return getNoCoverImage(null);
						}
	
						byte[] coverData = downloadCoverImage(url);
						if (coverData == null) {
							return getNoCoverImage(null);
						}
	
						Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
						return scaleImage(null, bitmap);
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
					}
					
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				coverReceiver.receiveCover(result);
			}
		};
		
		try {
			return new TaskHandleImpl<Object, Object, Bitmap>(task.executeOnExecutor(executor, url));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}	
	
	public static TaskHandle getCoverUrls(String artist, String album, final UrlReceiver urlReceiver) {
		AsyncTask<Object, Object, String[]> task = new AsyncTask<Object, Object, String[]>() {
			@Override
			protected String[] doInBackground(Object... params) {
				synchronized (lock) {
					Set<String> result = new LinkedHashSet<String>();
					
					try {
						String artistParam = (params[0] != null) ? (String)params[0] : "";
						String albumParam = (params[1] != null) ? (String)params[1] : "";

                        List<String> mpdUrlList = mpdCoverService.getCoverUrls(artistParam, albumParam);
                        result.addAll(mpdUrlList);

						List<String> httpUrlList = httpCoverService.getCoverUrls(artistParam, albumParam);
						result.addAll(httpUrlList);

						List<String> artists = createQueryStrings(artistParam, false);
						List<String> albums = createQueryStrings(albumParam, true);
						artists.add(null);
							
						for (String artist : artists) {
							for (String album : albums) {
								List<String> lastfmUrlList = lastFmCoverAndInfoService.getCoverUrls(artist, album);
								result.addAll(lastfmUrlList);

                                List<String> gracenoteUrlList = gracenoteCoverService.getCoverUrls(artist, album);
                                result.addAll(gracenoteUrlList);

								List<String> amazonUrlList = amazonCoverService.getCoverUrls(artist, album);
								result.addAll(amazonUrlList);
							}
						}
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
					}
					
					return result.toArray(new String[result.size()]);
				}
			}
			
			@Override
			protected void onPostExecute(String[] result) {
				urlReceiver.receiveUrls(result);
			}
		};
		
		try {
			return new TaskHandleImpl<Object, Object, String[]>(task.executeOnExecutor(executor, artist, album));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}
	
	public static TaskHandle setCoverUrl(String artist, String album, String url, Integer maxHeight, final CoverReceiver coverReceiver) {
		AsyncTask<Object, Object, Bitmap> task = new AsyncTask<Object, Object, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Object... params) {
				synchronized (lock) {
					try {
						String artistParam = (params[0] != null) ? (String)params[0] : "";
						String albumParam = (params[1] != null) ? (String)params[1] : "";
						String urlParam = (params[2] != null) ? (String)params[2] : NULL_URL;
						Boolean loadParam = (Boolean) params[4];
	
						String existingUrl = coverCache.getCoverUrl(artistParam, albumParam);
						if (existingUrl != null) {
							coverCache.deleteCoverImageIfLastUsage(existingUrl);
							coverCache.deleteCoverUrl(artistParam, albumParam);
						}
						coverCache.insertCoverUrl(artistParam, albumParam, urlParam);
						
						if (loadParam == Boolean.TRUE) {
							if (NULL_URL.equals(urlParam)) {
								return getNoCoverImage((Integer)params[3]);
							}
							
							byte[] coverData = downloadCoverImage(urlParam);
							if (coverData == null) {
								return getNoCoverImage((Integer)params[3]);
							}
							coverCache.insertCoverImage(urlParam, coverData);
							
							Bitmap bitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
							return scaleImage((Integer)params[3], bitmap);
						}
						
						return null;
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error fetching cover", ex);
					}
					
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				if (coverReceiver != null) {
					coverReceiver.receiveCover(result);
				}
			}
		};
		
		try {
			return new TaskHandleImpl<Object, Object, Bitmap>(task.executeOnExecutor(executor, artist, album, url, maxHeight, Boolean.valueOf(coverReceiver != null)));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to load cover. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}
	
	public static TaskHandle getArtistInfoUrls(String artist, final UrlReceiver urlReceiver) {
		AsyncTask<Object, Object, String[]> task = new AsyncTask<Object, Object, String[]>() {
			@Override
			protected String[] doInBackground(Object... params) {
				synchronized (lock) {
					List<String> result = new ArrayList<String>();
					try {
						List<String> artists = createQueryStrings((String)params[0], false);
						
						for (String artist : artists) {
							List<String> infoUrlList = lastFmCoverAndInfoService.getArtistInfoUrls(artist);
							result.addAll(infoUrlList);
						}
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error getting artist info", ex);
					}
					
					return result.toArray(new String[result.size()]);
				}
			}
			
			@Override
			protected void onPostExecute(String[] result) {
				urlReceiver.receiveUrls(result);
			}
		};
		
		try {
			return new TaskHandleImpl<Object, Object, String[]>(task.executeOnExecutor(executor, artist));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to artist info. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}	
	
	public static TaskHandle getAlbumInfoUrls(String artist, String album, final UrlReceiver urlReceiver) {
		AsyncTask<Object, Object, String[]> task = new AsyncTask<Object, Object, String[]>() {
			@Override
			protected String[] doInBackground(Object... params) {
				synchronized (lock) {
					List<String> result = new ArrayList<String>();
					try {
						List<String> artists = createQueryStrings((String)params[0], false);
						List<String> albums = createQueryStrings((String)params[1], true);
						
						for (String artist : artists) {
							for (String album : albums) {
								List<String> infoUrlList = lastFmCoverAndInfoService.getAlbumInfoUrls(artist, album);
								result.addAll(infoUrlList);
							}
						}
					} catch (Exception ex) {
						Log.e(ExternalInformationService.class.getName(), "Error getting album info", ex);
					}
					
					return result.toArray(new String[result.size()]);
				}
			}
			
			@Override
			protected void onPostExecute(String[] result) {
				urlReceiver.receiveUrls(result);
			}
		};
		
		try {
			return new TaskHandleImpl<Object, Object, String[]>(task.executeOnExecutor(executor, artist, album));
		} catch (RejectedExecutionException ex) {
			Log.e(ExternalInformationService.class.getName(), "Unable to album info. Exection rejected", ex);
			return TaskHandle.NULL_HANDLE;
		}
	}	
	
	private static Bitmap getNoCoverImage(Integer maxHeight) {
		Bitmap bitmap = BitmapFactory.decodeResource(ApplicationActivator.getContext().getResources(), R.drawable.no_cover);
		return scaleImage(maxHeight, bitmap);
	}
	
	private static Bitmap scaleImage(Integer scaledHeight, Bitmap coverImage) {
		try {
			int height = (scaledHeight != null) ? Math.min(((Integer)scaledHeight).intValue(), MAX_HEIGHT) : MAX_HEIGHT;
			int width = (int)(((float)height / (float)coverImage.getHeight()) * coverImage.getWidth());
			return Bitmap.createScaledBitmap(coverImage, width, height, true);
		} catch (Exception ex) {
			Log.e(ExternalInformationService.class.getName(), "Error scaling cover", ex);
		}
		
		return coverImage;
	}
	
	private static List<String> createQueryStrings(String input, boolean stripParentheseSuffix) {
		List<String> result = new ArrayList<String>();
		
		if (!Utils.nullOrEmpty(input)) {
			String ascii = convertNonAscii(input);
			if (ascii != null) {
				if (stripParentheseSuffix) {
					String strippedAsciiParenthese = stripParentheseSuffix(ascii);
					if (strippedAsciiParenthese != null) {
						result.add(strippedAsciiParenthese);
					}
					String strippedAsciiBracket = stripBracketSuffix(ascii);
					if (strippedAsciiBracket != null) {
						result.add(strippedAsciiBracket);
					}
				}
				result.add(ascii);
			}
	
			if (stripParentheseSuffix) {
				String strippedParenthese = stripParentheseSuffix(input);
				if (strippedParenthese != null) {
					result.add(strippedParenthese);
				}
				String strippedBracket = stripBracketSuffix(input);
				if (strippedBracket != null) {
					result.add(strippedBracket);
				}
			}
			result.add(input);
		}
		
		return result;
	}
	
	private static String convertNonAscii(String s) {
		boolean converted = false;
		
		StringBuilder sb = new StringBuilder();
		int l = s.length();
		for (int i = 0; i < l; i++) {
			char c = s.charAt(i);
			int pos = UNICODE.indexOf(c);
			if (pos > -1) {
				converted = true;
				sb.append(ASCII.charAt(pos));
			} else {
				sb.append(c);
			}
		}
		
		return (converted) ? sb.toString() : null;
	}

	private static String stripParentheseSuffix(String input) {
		if (input.endsWith(")")) {
			 int end = input.lastIndexOf("(");
			 if (end > 0) {
				 String stripped = input.substring(0, end);
				 return stripped.trim();
			 }
		}
		
		return null;
	}
		
	private static String stripBracketSuffix(String input) {
		if (input.endsWith("]")) {
			 int end = input.lastIndexOf("[");
			 if (end > 0) {
				 String stripped = input.substring(0, end);
				 return stripped.trim();
			 }
		}
		
		return null;
	}

	private static byte[] downloadCoverImage(String coverUrl) {
		try {
			URL url = new URL(coverUrl);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(10000);
			int contentLength = connection.getContentLength();
	
			DataInputStream stream = new DataInputStream(url.openStream());
			try {
				byte[] buffer = new byte[contentLength];
				stream.readFully(buffer);
				return buffer;
			} finally {
				stream.close();
			}
		} catch (Exception ex) {
			Log.e(ExternalInformationService.class.getName(), "error downloading cover", ex);
		}
		
		return null;
	}
}

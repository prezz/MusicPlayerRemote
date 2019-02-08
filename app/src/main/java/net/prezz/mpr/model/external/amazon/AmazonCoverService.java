package net.prezz.mpr.model.external.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.external.CoverService;
import net.prezz.mpr.ui.ApplicationActivator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

public class AmazonCoverService implements CoverService {

	@Override
	public List<String> getCoverUrls(String artist, String album) {
		try {
			String request = createRequest(artist, album);
			if (request == null) {
				return Collections.emptyList();
			}
			
			List<String> coverInfoList = readCoverInfo(request);
			return coverInfoList;
		} catch (Exception ex) {
			Log.e(AmazonCoverService.class.getName(), "Error getting covers from Amazon", ex);
		}
		
		return Collections.emptyList();
	}
	
	private String createRequest(String artist, String album) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException {
		if (album != null) {
			Context context = ApplicationActivator.getContext();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Resources resources = context.getResources();
			String awsAccessKeyId = sharedPreferences.getString(resources.getString(R.string.settings_covers_amazon_access_key_id_key), "");
			String awsSecretKey = sharedPreferences.getString(resources.getString(R.string.settings_covers_amazon_secret_key_key), "");
			
			if (!Utils.nullOrEmpty(awsAccessKeyId) && !Utils.nullOrEmpty(awsSecretKey)) {
				Map<String, String> queury = new HashMap<String, String>();
				queury.put("Service", "AWSECommerceService");
				queury.put("Operation", "ItemSearch");
				queury.put("ResponseGroup", "Images");
				queury.put("SearchIndex", "Music");
				if (artist != null && !artist.isEmpty()) {
					queury.put("Artist", artist);
				}
				queury.put("Title", album);
				queury.put("AssociateTag", "ignore-21"); //associate tags in Europe ends with -21
				
				SignedRequestsHelper helper = new SignedRequestsHelper(awsAccessKeyId, awsSecretKey);
				return helper.sign(queury);
			}
		}
		
		return null;
	}
	
	private List<String> readCoverInfo(String request) throws IOException, XmlPullParserException {
		URL url = new URL(request);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(10000);
	    
	    try {
            InputStream inputStream = connection.getInputStream();
            try {
                return parseXmlResponse(inputStream);
            } finally {
                inputStream.close();
            }
	    } finally {
	    	connection.disconnect();
	    }
	}
	
	private List<String> parseXmlResponse(InputStream inputStream) throws XmlPullParserException, IOException {
		List<String> result = new ArrayList<String>();
		
		XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, "UTF-8");
        
        String name = null;
        boolean largeImage = false;
        String coverUrl = null;
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if ("LargeImage".equals(name)) {
					largeImage = true;
				}
				if (largeImage) {
	 				if ("URL".equals(name)) {
	 					coverUrl = parser.nextText();
					}
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				if (coverUrl != null && "LargeImage".equals(name)) {
					if (!result.contains(coverUrl)) {
						result.add(coverUrl);
					}
					coverUrl = null;
					largeImage = false;
				}
				break;
			}
			eventType = parser.next();
		}
		
		return result;
	}
}

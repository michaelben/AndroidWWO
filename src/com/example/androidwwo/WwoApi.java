package com.example.androidwwo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.androidwwo.LocalWeather.CurrentCondition;
import com.example.androidwwo.LocalWeather.Data;

public class WwoApi {
	public static final boolean LOGD = true;

	//These keys are only for Demo purpose.
	//You should replace the key with your own key.
	//You can obtain your own key after registering on World Weather Online website.
	public static final String FREE_API_KEY = "xkq544hkar4m69qujdgujn7w";
	public static final String PREMIUM_API_KEY = "w9ve379xdu8etugm7e2ftxd6";
	
	String key;
	String apiEndPoint;
	
	WwoApi(boolean freeAPI) {
		if(freeAPI) {
			key = FREE_API_KEY;
		} else {
			key = PREMIUM_API_KEY;
		}
	}
	
	WwoApi setKey(String key) {
		this.key = key;
		return this;
	}
	
	WwoApi setApiEndPoint(String apiEndPoint) {
		this.apiEndPoint = apiEndPoint;
		return this;
	}
	
	class RootParams {
		String getQueryString(Class cls) {
			String query = null;
			
			Field[] fields = cls.getDeclaredFields();
			
			try {
			  for (Field field : fields) {
				  Object f = field.get(this);
				  if(f != null) {
					if(query == null)
						query = "?" + URLEncoder.encode(field.getName(), "UTF-8") + "="
							+ URLEncoder.encode((String)f, "UTF-8");
					else
						query += "&" + URLEncoder.encode(field.getName(), "UTF-8") + "="
								+ URLEncoder.encode((String)f, "UTF-8");
				  }
			  }
			} catch (Exception e) {
				
			}
			
			return query;
		}
	}
	
	static InputStream getInputStream(String url) {
		InputStream is = null;
		
		try {
			is = (new URL(url)).openConnection().getInputStream();
		} catch (Exception e) {
			
		}
		
		return is;
	}
	
	static XmlPullParser getXmlPullParser(InputStream is) {
		XmlPullParser xpp = null;
		
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	
			// process data
			// use local parser for preventing synchronization issue
	        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        xpp = factory.newPullParser();
	
	        xpp.setInput( rd );
		} catch (Exception e) {
			
		}

		return xpp;
    }
	
	static String getDecode(String value) {
		//value = new String(Base64.decode(value));
		if (value.startsWith("<![CDATA["))
			value = value.substring(9, value.length() - 3);
		return value;
	}

	static String getTextForTag(XmlPullParser xpp, String tag) 
	throws XmlPullParserException, IOException {
		while(myXppNextTag(xpp) != xpp.START_TAG || !xpp.getName().equals(tag))
			if(LOGD) Log.d("CallWWOApi", "Tag="+xpp.getName());
		xpp.require(xpp.START_TAG, "", tag);
		String text = xpp.nextText();
		if(LOGD) Log.d("CallWWOApi", "getTextForTag " + text);
		xpp.require(xpp.END_TAG, "", tag);
		
		return text;
	}
	
	static void reachNextStartTagFor(XmlPullParser xpp, String tag) 
	throws XmlPullParserException, IOException {
		while(myXppNextTag(xpp) != xpp.START_TAG || !xpp.getName().equals(tag))
			if(LOGD) Log.d("CallWWOApi", "Tag="+xpp.getName());
		xpp.require(xpp.START_TAG, "", tag);
	}
	
	//xpp.nextTag does not pass TEXT event
	//myXppNextTag passes TEXT event and whitespace, 
	static int myXppNextTag(XmlPullParser xpp)
	throws XmlPullParserException, IOException {
		   int eventType = xpp.next();
		   while(eventType != xpp.START_TAG &&  eventType != xpp.END_TAG) {   // skip whitespace
		      eventType = xpp.next();
		      if (eventType == xpp.END_DOCUMENT) break;
		   }
		   if (eventType != xpp.START_TAG &&  eventType != xpp.END_TAG) {
		      throw new XmlPullParserException("expected start or end tag", xpp, null);
		   }
		   return eventType;
	}
}
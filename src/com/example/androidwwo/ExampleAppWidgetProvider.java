package com.example.androidwwo;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * A widget provider.
 *
 * <p>See also the following files:
 * <ul>
 *   <li>ExampleAppWidgetConfigure.java</li>
 *   <li>ExampleBroadcastReceiver.java</li>
 *   <li>res/layout/appwidget_configure.xml</li>
 *   <li>res/layout/appwidget_provider.xml</li>
 *   <li>res/xml/appwidget_provider.xml</li>
 * </ul>
 */
public class ExampleAppWidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "ExampleAppWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.
        /*
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            String titlePrefix = ExampleAppWidgetConfigure.loadTitlePref(context, appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId, null);
        }*/
        
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {
    	LocalWeather.Data weather;
    	LocationSearch.Data loc;
    	
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Build the widget update for today
        	Log.d(TAG, "onStartCommand");
        	
            buildUpdate(this);

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        
        /**
         * Build a widget update to show the current weather
         * Will block until the online API returns.
         */
        public void buildUpdate(Context context) {
        	MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
        	    @Override
        	    public void gotLocation(Location location){
        	        //Got the location!
        	    	//Prevent multiple execution of the callback gotLocation, as reported at
        	    	//http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-a
        	    	Boolean Done = false;
        	    	synchronized (Done) {
        	    		if(!Done) {
        	    			String q = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude());

        	    			//get weather
        	    			LocalWeather lw = new LocalWeather(true);
        	    			String query = (lw.new Params(lw.key)).setQ(q).getQueryString(LocalWeather.Params.class);
        	    			weather = lw.callAPI(query);
        	    			
        	    			//get location
        	    			LocationSearch ls = new LocationSearch(true);
        	    			query = (ls.new Params(ls.key)).setQuery(q).getQueryString(LocationSearch.Params.class);
        	    			loc = ls.callAPI(query);
        	    			
        	    			//updateWidget
        	    			updateAppWidget(UpdateService.this);
        	    			
        	    			Done = true;
        	    		}
        	    	}
        	    }
        	};
        	
        	Log.d(TAG, "buildUpdate");
        	
        	MyLocation myLocation = new MyLocation();
        	myLocation.getLocation(this, locationResult);
        }
        
        void updateAppWidget(Context context) {
            RemoteViews views = null;

            // Build an update that holds the updated widget contents
            views = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
            
            views.setTextViewText(R.id.textViewTemp, weather.current_condition.temp_C+"\u2103");
            views.setTextViewText(R.id.textViewLocation, loc.region+", "+loc.country);
            views.setTextViewText(R.id.textViewDesc, weather.current_condition.weatherDesc);
            views.setImageViewBitmap(R.id.imageViewWidget, BitmapUtil.loadBitmap(weather.current_condition.weatherIconUrl));
            
            // When user clicks on widget, launch to detail page
            /*
            String definePage = String.format("%s://%s/%s", ExtendedWikiHelper.WIKI_AUTHORITY,
                    ExtendedWikiHelper.WIKI_LOOKUP_HOST, wordTitle);
            Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(definePage));
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    0, // no requestCode
                    defineIntent, 
                    0, // no flags
            );
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);
            */
                
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, ExampleAppWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, views);
        }
    }
}
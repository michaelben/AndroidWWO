AndroidWWO - Sample Android AppWidget for WWO API
-----------------------------
AndroidWWO is a sample Android AppWidget for [World Weather Online API](http://www.worldweatheronline.com/). It will show weather based on your current location.

How to run
----------
1. Install AndroidWWO.apk on your android device.
2. On home screen, long click to pop up context menu.
3. Select AppWidget->AndroidWWO.

You should see result as [this screenshot](https://github.com/michaelben/AndroidWWO/blob/master/image/screenshot.png).

How to use
-----------
```java
LocalWeather lw = new LocalWeather(true);
String query = (lw.new Params(lw.key)).setQ(q).getQueryString(LocalWeather.Params.class);
weather = lw.callAPI(query);
```

Feature
-----------
1. Builder pattern for params used for construct query string
2. Use XML PullParser to process XML response from API call for cheap memory footprint and fast xml processing on mobile device.

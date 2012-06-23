package com.MrTaxi.Driver;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.MrTaxi.core.Polyline;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class tab1_Activity extends MapActivity
{
	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay mylayer;
	private String address;
	private Boolean hasaddress=false;
	
	
	private Button btn;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab1);
		findControl();
		//direction();
		
		btn=(Button) this.findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if(hasaddress) direction();
            }
		});

	}

	private void findControl()
	{
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mapController = mapView.getController();
		mapController.setZoom(16);
		
		//定位點
		List<Overlay> overlays = mapView.getOverlays();
		mylayer = new MyLocationOverlay(this, mapView);
		//顯示羅盤
		mylayer.enableCompass();
		//啟動更新(如果坐標有變動會跟著移動)
		mylayer.enableMyLocation();
		mylayer.runOnFirstFix(new Runnable()
		{

			public void run()
			{
				mapController.animateTo(mylayer.getMyLocation());
				
				
				
				
				double lat=mylayer.getMyLocation().getLatitudeE6()/1E6;
				double lon=mylayer.getMyLocation().getLongitudeE6()/1E6;
				address=getAddress(lon,lat);
				hasaddress=true;

				
				
				
			}
		});
		overlays.add(mylayer);
		
	}
	
	
	
	
	
	public String getAddress(Double lon,Double lat) {
		String returnAddress = "";
		try {
		    		Double longitude = lon;	//取得經度
		    		Double latitude = lat;	//取得緯度
 
		    		//建立Geocoder物件: Android 8 以上模疑器測式會失敗
		    		Geocoder gc = new Geocoder(this, Locale.TRADITIONAL_CHINESE); 	//地區:台灣
		    		//自經緯度取得地址
		    		List<Address> lstAddress = gc.getFromLocation(latitude, longitude, 1);
 
		    	//	if (!Geocoder.isPresent()){ //Since: API Level 9
		    	//		returnAddress = "Sorry! Geocoder service not Present.";
		    	//	}
		    		returnAddress = lstAddress.get(0).getAddressLine(0);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return returnAddress;
	}
	
	
	
	
	
	
	private void direction(){
		_points.clear();
		_points=GetDirection();
		Polyline polyline = new Polyline(_points);  
        
        mapView.getOverlays().add(polyline); //map是MapView类型  
          
        mapView.invalidate();  
        mapView.setBuiltInZoomControls(true);  
          
//        MapController mapcontroller=mapView.getController();  
//        GeoPoint  point=new GeoPoint(39950181,116415059);  
//        mapcontroller.setCenter(point);
	}
	
	

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	

	
	
	private List<GeoPoint> _points = new ArrayList<GeoPoint>();

	public List<GeoPoint> GetDirection()
	{
		
	    String mapAPI = "http://maps.google.com/maps/api/directions/json?origin={0}&destination={1}&language=zh-TW&sensor=true";
	    String url = MessageFormat.format(mapAPI, address, "台北車站");

	    HttpGet get = new HttpGet(url);
	    String strResult = "";
	    try
	    {

	        HttpParams httpParameters = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
	        HttpClient httpClient = new DefaultHttpClient(httpParameters);

	        HttpResponse httpResponse = null;
	        httpResponse = httpClient.execute(get);

	        if (httpResponse.getStatusLine().getStatusCode() == 200)
	        {
	            strResult = EntityUtils.toString(httpResponse.getEntity());

	            JSONObject jsonObject = new JSONObject(strResult);
	            JSONArray routeObject = jsonObject.getJSONArray("routes");
	            String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline").getString("points");

	            if (polyline.length() > 0)
	            {
	                decodePolylines(polyline);
	            }

	        }
	    }
	    catch (Exception e)
	    {
	        Log.e("map", "MapRoute:" + e.toString());
	    }

	    return _points;
	}

	private void decodePolylines(String poly)
	{
	    int len = poly.length();
	    int index = 0;
	    int lat = 0;
	    int lng = 0;

	    while (index < len)
	    {
	        int b, shift = 0, result = 0;
	        do
	        {
	            b = poly.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do
	        {
	            b = poly.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
	        _points.add(p);

	    }
	    
	    
	}
}
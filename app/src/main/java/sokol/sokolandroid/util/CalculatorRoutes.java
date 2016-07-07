package sokol.sokolandroid.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeisondavid on 06/07/2016.
 */
public class CalculatorRoutes {

    public static List<LatLng> getPointsOfRoute(JSONObject jObject)
    {
        List<LatLng> route = new ArrayList<>() ;
        JSONArray jRoutes, jLegs, jSteps;
        try
        {
            jRoutes = jObject.getJSONArray("routes");
            jLegs = ((JSONObject)jRoutes.get(0)).getJSONArray("legs");
            for(int i = 0; i < jLegs.length() ; i++)
            {
                jSteps = ((JSONObject)jLegs.get(i)).getJSONArray("steps");
                for(int j = 0; j < jSteps.length() ; j++)
                {
                    String polyline = "";
                    polyline = (String)((JSONObject)((JSONObject)jSteps.get(j)).get("polyline")).get("points");
                    List<LatLng> list = decodePoly(polyline);
                    for( int k = 0; k < list.size(); k++ ) route.add(list.get(k));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return route;
    }

    public static String getUrl(LatLng origin, LatLng dest) {

        String originS = "origin=" + origin.latitude + "," + origin.longitude;
        String destinationS = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = originS + "&" + destinationS + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    public static String downloadUrl(String strUrl) throws IOException
    {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        }
        catch (Exception e)
        {

        }
        finally
        {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Method to decode polyline points
     * take of -->
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}

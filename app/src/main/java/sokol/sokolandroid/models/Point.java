package sokol.sokolandroid.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fabian on 1/07/2016.
 */
public class Point {
    private LatLng latLng;
    private boolean checkPoint;
    private String name;

    public Point(LatLng latLng, boolean checkPoint, String name) {
        this.latLng = latLng;
        this.checkPoint = checkPoint;
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public boolean isCheckPoint() {
        return checkPoint;
    }

    public void setCheckPoint(boolean checkPoint) {
        this.checkPoint = checkPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatLngString(){
        String lat = String.format("%.4f",latLng.latitude);
        String lng = String.format("%.4f",latLng.longitude);
        return "("+lat+", "+lng+")";
    }
}

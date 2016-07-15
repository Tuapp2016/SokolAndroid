package sokol.sokolandroid.models;

import java.util.ArrayList;

import sokol.sokolandroid.activities.CreateRouteMapsActivity;

/**
 * Created by Fabian on 13/07/2016.
 */
public class Route {

    private String userID;
    private String name;
    private String description;
    private ArrayList<String> pointNames;
    private ArrayList<Double> latitudes;
    private ArrayList<Double> longitudes;
    private ArrayList<Boolean> checkPoints;

    public Route(){

    }

    public Route(String userID, String name, String description, ArrayList<CreateRouteMapsActivity.MarketRoute> markers) {
        this.userID = userID;
        this.name = name;
        this.description = description;
        pointNames = new ArrayList<>(markers.size());
        latitudes = new ArrayList<>(markers.size());
        longitudes = new ArrayList<>(markers.size());
        checkPoints = new ArrayList<>(markers.size());
        for( int i = 0; i < markers.size(); i++){
            CreateRouteMapsActivity.MarketRoute marketRoute = markers.get(i);
            pointNames.add(marketRoute.getName());
            latitudes.add(marketRoute.getMarker().getPosition().latitude);
            longitudes.add(marketRoute.getMarker().getPosition().longitude);
            checkPoints.add(marketRoute.isCheckPoint());
        }
    }

    public Route(String uid, String userID, String name, String description,
                 ArrayList<String> pointNames, ArrayList<Double> latitudes,
                 ArrayList<Double> longitudes, ArrayList<Boolean> checkPoints) {
        this.userID = userID;
        this.name = name;
        this.description = description;
        this.pointNames = pointNames;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.checkPoints = checkPoints;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getPointNames() {
        return pointNames;
    }

    public void setPointNames(ArrayList<String> pointNames) {
        this.pointNames = pointNames;
    }

    public ArrayList<Double> getLatitudes() {
        return latitudes;
    }

    public void setLatitudes(ArrayList<Double> latitudes) {
        this.latitudes = latitudes;
    }

    public ArrayList<Double> getLongitudes() {
        return longitudes;
    }

    public void setLongitudes(ArrayList<Double> longitudes) {
        this.longitudes = longitudes;
    }

    public ArrayList<Boolean> getCheckPoints() {
        return checkPoints;
    }

    public void setCheckPoints(ArrayList<Boolean> checkPoints) {
        this.checkPoints = checkPoints;
    }

}

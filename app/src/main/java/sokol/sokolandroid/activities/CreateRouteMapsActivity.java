package sokol.sokolandroid.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import sokol.sokolandroid.R;
import sokol.sokolandroid.models.Point;

public class CreateRouteMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static String TAG = CreateRouteMapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private ArrayList<MarkerRoute> mMarkers;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public CreateRouteMapsActivity() {
        mMarkers = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        loadPreviousPoints();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
               addMarker(latLng, "", false);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showInfoDialog(marker);
                return false;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) { }
            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                int index = getIndexMarker(marker);
                LatLng position = marker.getPosition();
                marker.setTitle(makeTitleMarker(position, index));
                marker.hideInfoWindow();
            }
        });
    }

    private void addMarker(LatLng latLng, String name, boolean isCheckPoint){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title(makeTitleMarker(latLng, mMarkers.size()));
        Marker marker = mMap.addMarker(markerOptions);
        mMarkers.add(new MarkerRoute(marker, isCheckPoint, name));
        if(isCheckPoint)
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        else
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    private int getIndexMarker(Marker marker){
        for(int i = 0; i < mMarkers.size(); i++)
            if(marker.equals(mMarkers.get(i).getMarker()))
                return i;
        return -1;
    }

    private String makeTitleMarker(LatLng latLng, int id){
        String lat = String.format("%.4f",latLng.latitude);
        String lng = String.format("%.4f",latLng.longitude);
        return "# " + (id + 1) + "\n("+lat+", "+lng+")";
    }

    private void loadPreviousPoints(){
        ArrayList<Point> points = CreateRouteActivity.mPoints;
        for(int i = 0; i < points.size(); i++){
            Point point = points.get(i);
            addMarker(point.getLatLng(), point.getName(), point.isCheckPoint());
        }
    }

    private void showInfoDialog(final Marker marker){
        marker.hideInfoWindow();
        final int markerIndex = getIndexMarker(marker);
        final MarkerRoute markerRoute = mMarkers.get(markerIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRouteMapsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_create_route_info_window, null);
        builder.setView(dialogView);
        final TextView name = (TextView) dialogView.findViewById(R.id.create_route_info_window_name);
        name.setText(markerRoute.getName());
        final SwitchCompat checkPoint = (SwitchCompat) dialogView.findViewById(R.id.create_route_info_window_checkpoint);
        checkPoint.setChecked(markerRoute.isCheckPoint());
        checkPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheckPoint = checkPoint.isChecked();
                markerRoute.setCheckPoint(isCheckPoint);
                if(isCheckPoint)
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                else
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        });

        builder.setTitle(R.string.create_route_info_window_title);
        builder.setMessage(makeTitleMarker(marker.getPosition(), markerIndex));
        builder.setNeutralButton(R.string.create_route_info_window_delete_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();
                mMarkers.remove(markerIndex);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markerRoute.setName(name.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onStart() {
        super.onStart();

/*        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CreateRouteMaps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://sokol.sokolandroid.activities/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();

        /*
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CreateRouteMaps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://sokol.sokolandroid.activities/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ArrayList<Point> points = CreateRouteActivity.mPoints;
        points.clear();

        for (MarkerRoute markerRoute : mMarkers)
            points.add(new Point(markerRoute.getMarker().getPosition(), markerRoute.isCheckPoint(), markerRoute.getName()));
    }


    static class MarkerRoute {
        private Marker marker;
        private String name;
        private boolean checkPoint;

        public MarkerRoute(Marker marker, boolean checkPoint, String name) {
            this.marker = marker;
            this.name = name;
            this.checkPoint = checkPoint;
        }

        public Marker getMarker() {
            return marker;
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MarkerRoute that = (MarkerRoute) o;

            if (checkPoint != that.checkPoint) return false;
            return marker != null ? marker.equals(that.marker) : that.marker == null;

        }

        @Override
        public int hashCode() {
            return  marker.hashCode();
        }
    }
}

package sokol.sokolandroid.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sokol.sokolandroid.R;
import sokol.sokolandroid.models.Route;
import sokol.sokolandroid.util.CalculatorRoutes;


public class CreateRouteMapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    public static String TAG = CreateRouteMapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private ArrayList<MarketRoute> mMarkers;
    private ArrayList<CalculateRoute> mCalculateRoutes;
    private String mRouteId, mRouteUserId, mRouteName, mRouteDesc;
    private List<String> mRoutesUser;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private DatabaseReference mRefRoutes;
    private DatabaseReference mRefuserByRoutes;

    private FloatingActionButton mSaveRoute;
    private FloatingActionButton mUsersRoute;
    private FloatingActionButton mDataRoute;

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

        mSaveRoute = (FloatingActionButton) findViewById(R.id.create_route_map_save);
        mSaveRoute.hide();
        mSaveRoute.setOnClickListener(this);

        mUsersRoute = (FloatingActionButton) findViewById(R.id.create_route_map_users);
        mDataRoute = (FloatingActionButton) findViewById(R.id.create_route_map_data);
        mDataRoute.setOnClickListener(this);
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

        mCalculateRoutes = new ArrayList<>();
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
                marker.setTitle(getString(R.string.create_route_info_window_title, index));
                marker.hideInfoWindow();
            }
        });

        setUpDB();
    }

    private void addMarker(LatLng latLng, String name, boolean isCheckPoint){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title(getString(R.string.create_route_info_window_title, mMarkers.size()));
        Marker marker = mMap.addMarker(markerOptions);
        mMarkers.add(new MarketRoute(marker, isCheckPoint, name));
        if(isCheckPoint)
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        else
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if ( mMarkers.size() > 1){
            String url = CalculatorRoutes.getUrl(mMarkers.get(mMarkers.size()-2).getMarker().getPosition(), latLng);
            mCalculateRoutes.add(new CalculateRoute());
            mCalculateRoutes.get(mCalculateRoutes.size()-1).execute(url);
            updateDoneButton();
        }

    }

    private void updateDoneButton() {
        if(mMarkers.size() > 1 && mRouteName != null && mRouteDesc != null && !mRouteName.isEmpty() && !mRouteDesc.isEmpty())
            mSaveRoute.show();
        else
            mSaveRoute.hide();
    }

    private int getIndexMarker(Marker marker){
        for(int i = 0; i < mMarkers.size(); i++)
            if(marker.equals(mMarkers.get(i).getMarker()))
                return i;
        return -1;
    }

    private String makeTitleMarker(LatLng latLng){
        String lat = String.format("%.4f",latLng.latitude);
        String lng = String.format("%.4f",latLng.longitude);
        String message = getString(R.string.create_route_info_window_latitude_title, lat)  + "\n";
        message += getString(R.string.create_route_info_window_longitude_title, lng);
        return message;
    }

    private void showDataDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRouteMapsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_create_route_data, null);
        builder.setView(dialogView);
        final TextInputLayout nameTI = (TextInputLayout) dialogView.findViewById(R.id.create_route_name_ti);
        final TextInputLayout descTI = (TextInputLayout) dialogView.findViewById(R.id.create_route_desc_ti);

        final EditText nameET = (EditText) dialogView.findViewById(R.id.create_route_name);
        final EditText descET = (EditText) dialogView.findViewById(R.id.create_route_desc);

        nameET.setText( mRouteName != null ? mRouteName : "");
        descET.setText( mRouteDesc != null ? mRouteDesc : "");

        builder.setTitle(R.string.create_route_data_title);
        builder.setPositiveButton(R.string.create_route_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, which+"");
                mRouteName = nameET.getText().toString();
                mRouteDesc = descET.getText().toString();

                if(mRouteName.isEmpty())
                    nameTI.setError(getString(R.string.create_route_invalid_input));

                if(mRouteDesc.isEmpty())
                    descTI.setError(getString(R.string.create_route_invalid_input));

                boolean validInputs = !mRouteName.isEmpty() && !mRouteDesc.isEmpty();
                if(validInputs)
                    dialog.dismiss();
                updateDoneButton();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showInfoDialog(final Marker marker){
        marker.hideInfoWindow();
        final int markerIndex = getIndexMarker(marker);
        final MarketRoute marketRoute = mMarkers.get(markerIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRouteMapsActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_create_route_info_window, null);
        builder.setView(dialogView);
        final TextView name = (TextView) dialogView.findViewById(R.id.create_route_info_window_name);
        name.setText(marketRoute.getName());
        final SwitchCompat checkPoint = (SwitchCompat) dialogView.findViewById(R.id.create_route_info_window_checkpoint);
        checkPoint.setChecked(marketRoute.isCheckPoint());
        checkPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheckPoint = checkPoint.isChecked();
                marketRoute.setCheckPoint(isCheckPoint);
                if(isCheckPoint)
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                else
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        });
        builder.setTitle(getString(R.string.create_route_info_window_title, markerIndex));
        builder.setMessage(makeTitleMarker(marker.getPosition()));
        builder.setNeutralButton(R.string.create_route_info_window_delete_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();
                mMarkers.remove(markerIndex);
                dialog.dismiss();
                updateDoneButton();
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marketRoute.setName(name.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpDB(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mRefRoutes = mRef.child(getString(R.string.db_routes));
        mRefuserByRoutes = mRef.child(getString(R.string.db_user_by_routes));
        mRouteUserId = mFirebaseUser.getUid();

        mRouteId = getIntent().getStringExtra(getString(R.string.create_route_maps_uid));
        if(mRouteId != null)
            getDataRoute();

        getUserRoutes();
    }

    private void getDataRoute(){
        final DatabaseReference dataRoute = mRefRoutes.child(mRouteId);
        dataRoute.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route = dataSnapshot.getValue(Route.class);
                if(route == null) return ;
                mRouteName = route.getName();
                mRouteDesc = route.getDescription();
                int size = route.getPointNames().size();
                for(int i = 0; i < size; i++)
                    addMarker(new LatLng(route.getLatitudes().get(i), route.getLongitudes().get(i)),
                            route.getPointNames().get(i), route.getCheckPoints().get(i));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserRoutes(){
        final DatabaseReference routesUser = mRefuserByRoutes.child(mRouteUserId).child(getString(R.string.db_routes));
        routesUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                mRoutesUser = dataSnapshot.getValue(t);
                if(mRoutesUser == null)
                    mRoutesUser = new ArrayList<String>();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateDB(){
        if(mRouteId == null || mRouteId.equals(""))
            mRouteId = mRefRoutes.push().getKey();

        // Updates routes
        DatabaseReference routeReference = mRefRoutes.child(mRouteId);
        Route route = new Route(mRouteUserId, mRouteName, mRouteDesc, mMarkers);
        routeReference.setValue(route);

        //Updates userByRoutes
        DatabaseReference userByReference = mRefuserByRoutes.child(mRouteUserId).child(getString(R.string.db_routes));
        if(!mRoutesUser.contains(mRouteId))
            mRoutesUser.add(mRouteId);
        userByReference.setValue(mRoutesUser);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch( id ){
            case R.id.create_route_map_data:
                showDataDialog();
                break;
            case R.id.create_route_map_users:
                break;
            case R.id.create_route_map_save:
                Toast.makeText(getApplicationContext(), "Saved in DB", Toast.LENGTH_SHORT).show();
                updateDB();
                break;
        }
    }



    public static class MarketRoute {
        private Marker marker;
        private String name;
        private boolean checkPoint;

        public MarketRoute(Marker marker, boolean checkPoint, String name) {
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

            MarketRoute that = (MarketRoute) o;

            if (checkPoint != that.checkPoint) return false;
            return marker != null ? marker.equals(that.marker) : that.marker == null;

        }

        @Override
        public int hashCode() {
            return  marker.hashCode();
        }
    }

    private class CalculateRoute extends AsyncTask<String, Integer, List<LatLng>>
    {
        @Override
        protected List<LatLng> doInBackground(String... url)
        {
            String jsonData[] = new String[1];
            try
            {
                jsonData[0] = CalculatorRoutes.downloadUrl(url[0]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            JSONObject jObject;
            List<LatLng> route = null;
            try
            {
                jObject = new JSONObject(jsonData[0]);
                route = CalculatorRoutes.getPointsOfRoute(jObject);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return route;
        }

        @Override
        protected void onPostExecute(List<LatLng> result)
        {
            ArrayList<LatLng> points = new ArrayList<>();
            PolylineOptions lineOptions = new PolylineOptions();
            for (int i = 0; i < result.size(); i++) points.add(result.get(i));
            lineOptions.addAll(points);
            lineOptions.width(8);
            lineOptions.color(Color.BLUE);
            mMap.addPolyline(lineOptions);
        }
    }
}

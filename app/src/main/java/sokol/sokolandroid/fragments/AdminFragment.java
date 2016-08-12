package sokol.sokolandroid.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import sokol.sokolandroid.R;
import sokol.sokolandroid.adapters.RouteNamesAdapter;
import sokol.sokolandroid.models.Route;
import sokol.sokolandroid.models.RouteName;

public class AdminFragment extends Fragment implements  View.OnClickListener{

    private ImageView mHeaderImageView;
    private RecyclerView mRecyclerView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private DatabaseReference mRefRoutes;
    private DatabaseReference mRefUserByRoutes;
    private List<DatabaseReference> mRefEachRoute;

    private String mUserUid;

    private List<String> mRoutesUIDs;
    private List<RouteName> mRoutesNames;

    private RouteNamesAdapter mRouteNamesAdapter;
    private RecycleRouteNamesListener mRouteNamesListener;

    public AdminFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUserUid = mAuth.getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference();
        mRefRoutes = mRef.child(getString(R.string.db_routes));
        mRefUserByRoutes = mRef.child("userByRoutes").child(mUserUid).child(getString(R.string.db_routes));
        mRefEachRoute = new ArrayList<>();

        mHeaderImageView = (ImageView) view.findViewById(R.id.base_admin_header);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.base_admin_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mRoutesUIDs = new ArrayList<>();
        mRoutesNames = new ArrayList<>();

        mRouteNamesAdapter = new RouteNamesAdapter(mRoutesNames);
        mRouteNamesAdapter.setRouteNamesListener(new RouteNamesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String uid) {
                mRouteNamesListener.onRouteName(uid);
            }
        });
        mRecyclerView.setAdapter(mRouteNamesAdapter);

        mRefUserByRoutes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> routesUids = (ArrayList<String>) dataSnapshot.getValue();
                if(routesUids == null)
                    mRoutesUIDs = new ArrayList<String>();
                else
                    mRoutesUIDs = routesUids;
                getRouteNames();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void getRouteNames(){
        mRefEachRoute.clear();
        if(mRoutesUIDs != null)
        for (String uid: mRoutesUIDs){
            DatabaseReference ref = mRefRoutes.child(uid);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String uid = dataSnapshot.getKey();
                    Route route = dataSnapshot.getValue(Route.class);
                    String name = route.getName();
                    boolean updated = false;
                    for(RouteName rm: mRoutesNames){
                        if(rm.getUid().equals(uid)){
                            rm.setName(name);
                            updated = true;
                            break;
                        }
                    }
                    if(!updated)
                        mRoutesNames.add(new RouteName(uid, name));
                    mRouteNamesAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
            mRefEachRoute.add(ref);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){

        }
    }

    public interface RecycleRouteNamesListener {
        void onRouteName(String uid);
    }

    public void setOnRouteNameLister(final RecycleRouteNamesListener listener){
        mRouteNamesListener = listener;
    }

}

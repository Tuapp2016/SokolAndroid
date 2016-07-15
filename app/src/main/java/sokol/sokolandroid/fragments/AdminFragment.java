package sokol.sokolandroid.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import sokol.sokolandroid.R;
import sokol.sokolandroid.activities.CreateRouteMapsActivity;
import sokol.sokolandroid.models.UserByRoutes;

public class AdminFragment extends Fragment implements  View.OnClickListener{

    private ImageView mHeaderImageView;
    private RecyclerView mRecyclerView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private DatabaseReference mRefRoutes;

    private String mUserUid;

    private FirebaseRecyclerAdapter<UserByRoutes, RouteHolder> mAdapter;

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
        mRefRoutes = mRef.child("userByRoutes").child(mUserUid);

        mHeaderImageView = (ImageView) view.findViewById(R.id.base_admin_header);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.base_admin_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = new FirebaseRecyclerAdapter<UserByRoutes, RouteHolder>(UserByRoutes.class, android.R.layout.two_line_list_item, RouteHolder.class, mRefRoutes) {
            @Override
            public void populateViewHolder(RouteHolder routeHolder, UserByRoutes route, int position) {
                routeHolder.mName.setText(route.getNames().get(position));
                routeHolder.mUid.setVisibility(View.INVISIBLE);
                routeHolder.mUid.setText(route.getRoutes().get(position));
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = mRecyclerView.indexOfChild(v);
                String item = mAdapter.getItem(itemPosition).getRoutes().get(itemPosition);
                Intent intent = new Intent(getContext(), CreateRouteMapsActivity.class);
                intent.putExtra(getString(R.string.create_route_maps_uid), item);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){

        }
    }

    private static class RouteHolder extends RecyclerView.ViewHolder {
        TextView mUid;
        TextView mName;

        public RouteHolder(View itemView) {
            super(itemView);
            mUid = (TextView) itemView.findViewById(android.R.id.text2);
            mName = (TextView) itemView.findViewById(android.R.id.text1);
         }
    }

}

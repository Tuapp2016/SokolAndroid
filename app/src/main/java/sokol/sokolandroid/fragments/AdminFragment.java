package sokol.sokolandroid.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import sokol.sokolandroid.CreateRouteActivity;
import sokol.sokolandroid.R;

public class AdminFragment extends Fragment implements  View.OnClickListener{

    private ImageView mHeaderImageView;
    private RecyclerView mRecyclerView;

    public AdminFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);
        mHeaderImageView = (ImageView) view.findViewById(R.id.base_admin_header);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.base_admin_recycler);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){

        }
    }
}

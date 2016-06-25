package sokol.sokolandroid.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import sokol.sokolandroid.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {


    private ImageView mHeaderImageView;
    private RecyclerView mRecyclerView;
    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        mHeaderImageView = (ImageView) view.findViewById(R.id.base_user_header);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.base_user_recycler);
        return view;
    }

}

package sokol.sokolandroid.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sokol.sokolandroid.R;

public class NewsFragment extends Fragment {

    private ImageView mHeaderImageView;
    private RecyclerView mRecyclerView;

    public NewsFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mHeaderImageView = (ImageView) view.findViewById(R.id.base_news_header);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.base_news_recycler);
        return view;
    }


}

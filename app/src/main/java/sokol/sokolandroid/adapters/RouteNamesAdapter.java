package sokol.sokolandroid.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.cast.internal.zzl;

import java.util.List;

import sokol.sokolandroid.R;
import sokol.sokolandroid.models.RouteName;

public class RouteNamesAdapter extends RecyclerView.Adapter<RouteNamesAdapter.RouteNameViewHolder>{

    private List<RouteName> names;
    private OnItemClickListener mItemClickListener;

    public RouteNamesAdapter(List<RouteName> names) {
        this.names = names;
    }


    @Override
    public RouteNameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_route, parent, false);
        return new RouteNameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteNameViewHolder holder, int position) {
        RouteName routeName = names.get(position);
        holder.bindRouteName(routeName.getUid(), routeName.getName(), mItemClickListener);
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public static class RouteNameViewHolder extends RecyclerView.ViewHolder{

        private String uid;
        private TextView name;

        public RouteNameViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_name_route);
        }

        public void bindRouteName(final String uid, final String name, final OnItemClickListener listener){
            this.uid = uid;
            this.name.setText(name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(uid);
                }
            });
        }
    }


    public interface OnItemClickListener{
        void onItemClick(String uid);
    }

    public void setRouteNamesListener(OnItemClickListener listener){
        mItemClickListener = listener;
    }
}

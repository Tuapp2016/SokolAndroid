package sokol.sokolandroid.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sokol.sokolandroid.R;
import sokol.sokolandroid.models.Point;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {

    private LayoutInflater inflater;
    private List<Point> points;

    public PointAdapter(LayoutInflater inflater, List<Point> points) {
        this.inflater = inflater;
        this.points = points;
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public PointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = inflater.inflate(R.layout.item_point, parent, false);
        PointViewHolder pointViewHolder = new PointViewHolder(item);
        pointViewHolder.title = (TextView) item.findViewById(R.id.item_point_title);
        pointViewHolder.position = (TextView) item.findViewById(R.id.item_point_position);
        pointViewHolder.checkPoint = (SwitchCompat) item.findViewById(R.id.item_point_checkpoint);
        return pointViewHolder;
    }

    @Override
    public void onBindViewHolder(PointViewHolder holder, int position) {
        Point point = points.get(position);
        holder.title.setText(point.getName());
        holder.position.setText(point.getLatLngString());
        holder.checkPoint.setChecked(point.isCheckPoint());
    }

    @Override
    public int getItemCount() {
        return points.size();
    }


    public static class PointViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private TextView title;
        private TextView position;
        private SwitchCompat checkPoint;

        public PointViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
    }
}

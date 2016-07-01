package sokol.sokolandroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import sokol.sokolandroid.R;
import sokol.sokolandroid.adapters.PointAdapter;
import sokol.sokolandroid.models.Point;

public class CreateRouteActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mName;
    private RecyclerView mRecyclerView;
    private PointAdapter mRecyclerAdapter;
    private FloatingActionButton mFloatingActionButton;
    public static ArrayList<Point> mPoints;

    public CreateRouteActivity() {
        mPoints = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        mName = (EditText) findViewById(R.id.create_route_name);

        mRecyclerView = (RecyclerView) findViewById(R.id.create_route_recycle);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerAdapter = new PointAdapter(this.getLayoutInflater(), mPoints);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        setDragAndDropAndSwipe();

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.create_route_fab);
        mFloatingActionButton.setOnClickListener(this);
        
    }

    private void setDragAndDropAndSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallbackItemTouchHelper = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();

                Point prev = mRecyclerAdapter.getPoints().remove(fromPosition);
                mRecyclerAdapter.getPoints().add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
                mRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);

                mRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mRecyclerAdapter.getPoints().remove(position);
                mRecyclerAdapter.notifyDataSetChanged();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallbackItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void goToCreateRouteMaps(){
        Intent intent = new Intent(this, CreateRouteMapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.create_route_fab:
                goToCreateRouteMaps();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerAdapter.notifyDataSetChanged();
    }
}

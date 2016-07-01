package sokol.sokolandroid;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateRouteActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText mName;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);

        mName = (EditText) findViewById(R.id.create_route_name);
        mRecyclerView = (RecyclerView) findViewById(R.id.create_route_recycle);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.create_route_fab);
        mFloatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.create_route_fab:
                Toast.makeText(this, "Edit markers", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}

package sokol.sokolandroid.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import sokol.sokolandroid.R;
import sokol.sokolandroid.models.User;
import sokol.sokolandroid.util.Util;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = ProfileActivity.class.getName();

    /* Data from the authenticated user */
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mProfileReference;
    private ValueEventListener mProfileValueEventListener;

    private de.hdodenhof.circleimageview.CircleImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileEmail;
    private ImageView mProfileProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        setContentView(R.layout.activity_profile);

        mProfileImage = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.profile_data_user_image);
        mProfileName = (TextView) findViewById(R.id.profile_data_user_name);
        mProfileEmail = (TextView) findViewById(R.id.profile_data_user_email);
        mProfileProvider = (ImageView) findViewById(R.id.profile_data_user_provider);

    }

    private void loadProfileInfo(){
        String uid = mFirebaseUser.getUid();
        mProfileReference = mDatabaseReference.child(getString(R.string.db_users_users)).child(uid);
        mProfileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mProfileName.setText(user.getName());
                mProfileEmail.setText(user.getEmail());
                mProfileProvider.setImageDrawable(getResources().getDrawable(getDrawableIdFromProvider(user.getProvider())));
                String URL = user.getProfileImage();
                Bitmap profileBitmap = null;
                try {
                    profileBitmap = new Util.ImageFromURLTask(URL).execute().get();
                    mProfileImage.setImageBitmap(profileBitmap);
                }catch (Exception e){
                    Log.e(TAG, "Error getting image from: " + URL);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { Log.e(TAG, "Error getting user data"); }
        };
        mProfileReference.addListenerForSingleValueEvent(mProfileValueEventListener);
    }

    private int getDrawableIdFromProvider(String provider){
        int result = 0;
        switch (provider){
            case "facebook.com":
                result = R.drawable.com_facebook_button_icon_blue;
                break;
            case "google.com":
                result = R.drawable.ic_google_logo;
                break;
            case "twitter.com":
                result = R.drawable.tw__composer_logo_blue;
                break;
            default:
                result = R.mipmap.ic_launcher;
                break;
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProfileInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProfileValueEventListener != null) {
            mProfileReference.removeEventListener(mProfileValueEventListener);
        }
    }
}

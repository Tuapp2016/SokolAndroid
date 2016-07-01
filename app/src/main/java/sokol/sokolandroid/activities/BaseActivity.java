package sokol.sokolandroid.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import sokol.sokolandroid.R;
import sokol.sokolandroid.adapters.ViewPagerAdapter;
import sokol.sokolandroid.fragments.AdminFragment;
import sokol.sokolandroid.fragments.NewsFragment;
import sokol.sokolandroid.fragments.UserFragment;
import sokol.sokolandroid.fragments.ViewerFragment;
import sokol.sokolandroid.models.User;
import sokol.sokolandroid.util.Util;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FloatingActionButton mFABTabs;

    private TextView mProfileName;
    private de.hdodenhof.circleimageview.CircleImageView mProfileImage;

    /* Data from the authenticated user */
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mProfileReference;
    private ValueEventListener mProfileValueEventListener;

    private static final int TAB_NEWS = 0;
    private static final int TAB_ADMIN = 1;
    private static final int TAB_USER = 2;
    private static final int TAB_VIEWER = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        setContentView(R.layout.activity_base);

        mToolbar = (Toolbar) findViewById(R.id.base_toolbar);
        setToolbar();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int groupId = menuItem.getGroupId();
                int itemId = menuItem.getItemId();
                if(groupId == R.id.nav_routes_group){
                    menuItem.setChecked(true);
                    int idTab = 0;
                    switch (itemId){
                        case R.id.nav_news:
                            idTab = TAB_NEWS;
                            break;
                        case  R.id.nav_admin:
                            idTab = TAB_ADMIN;
                            break;
                        case  R.id.nav_user:
                            idTab = TAB_USER;
                            break;
                        case  R.id.nav_viewer:
                            idTab = TAB_VIEWER;
                            break;
                        default:
                            idTab = TAB_NEWS;
                            break;
                    }
                    mTabLayout.getTabAt(idTab).select();
                }else if(groupId == R.id.nav_profile_group){
                    switch (itemId){
                        case R.id.nav_profile:
                            goToProfileActivity();
                            break;
                        case R.id.nav_logout:
                            logout();
                            break;
                    }
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_drawer_opened, R.string.nav_drawer_closed){
            @Override
            public void onDrawerOpened(View drawerView) { super.onDrawerOpened(drawerView); }

            @Override
            public void onDrawerClosed(View drawerView) { super.onDrawerClosed(drawerView); }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mViewPager = (ViewPager) findViewById(R.id.base_viewpager);
        setViewPager();

        mTabLayout = (TabLayout) findViewById(R.id.base_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                mNavigationView.getMenu().getItem(position).setChecked(true);
                if(position == TAB_NEWS)
                    mFABTabs.hide();
                else
                    mFABTabs.show();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        setTabIcons();

        mFABTabs = (FloatingActionButton) findViewById(R.id.base_fab);
        mFABTabs.setOnClickListener(this);
        mFABTabs.hide();

        mProfileName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_name);
        mProfileImage = (de.hdodenhof.circleimageview.CircleImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_image);
    }

    private void setToolbar(){
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), true);
        viewPagerAdapter.addFragment(new NewsFragment(), getString(R.string.nav_news_title));
        viewPagerAdapter.addFragment(new AdminFragment(), getString(R.string.nav_admin_title));
        viewPagerAdapter.addFragment(new UserFragment(), getString(R.string.nav_user_title));
        viewPagerAdapter.addFragment(new ViewerFragment(), getString(R.string.nav_viewer_title));
        mViewPager.setAdapter(viewPagerAdapter);
    }

    private void setTabIcons(){
        mTabLayout.getTabAt(TAB_NEWS).setIcon(R.drawable.ic_action_home);
        mTabLayout.getTabAt(TAB_ADMIN).setIcon(R.drawable.ic_action_admin);
        mTabLayout.getTabAt(TAB_USER).setIcon(R.drawable.ic_action_user);
        mTabLayout.getTabAt(TAB_VIEWER).setIcon(R.drawable.ic_action_viewer);
    }

    private void goToProfileActivity(){
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    @Override
    public void onBackPressed() {
        goToLoginActivity();
    }

    private void logout() {
        goToLoginActivity();
    }

    private void forceLogout(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.preference_logout), true).commit();
        BaseActivity.super.onBackPressed();
    }

    private  void goToLoginActivity(){
        AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
        builder.setTitle(R.string.base_logout_title);
        builder.setMessage(getString(R.string.base_logout_message));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                forceLogout();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadProfileInfo(){
        String uid = mFirebaseUser.getUid();
        mProfileReference = mDatabaseReference.child(getString(R.string.db_users_users)).child(uid);
        mProfileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user == null) {
                    //forceLogout();
                    //return ;
                }else {
                    Log.d(TAG, "Data has been changed for user " + user.getName());
                    mProfileName.setText(user.getName());
                    String URL = user.getProfileImage();
                    try {
                        new Util.ImageFromURLTask(URL, mProfileImage).execute();
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting image from: " + URL);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mProfileReference.addValueEventListener(mProfileValueEventListener);
    }

    private void goToAddRoute(){
        int idTab = mTabLayout.getSelectedTabPosition();
        Intent intent = null;
        switch (idTab){
            case TAB_ADMIN:
                intent = new Intent(this, CreateRouteActivity.class);
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            getMenuInflater().inflate(R.menu.main, menu);
            return  true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.base_fab:
                goToAddRoute();
                break;
        }
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

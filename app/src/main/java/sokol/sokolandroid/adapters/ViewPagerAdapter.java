package sokol.sokolandroid.adapters;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabian on 25/06/2016.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;
    private List<String> fragmentTitleList;
    private boolean showOnlyIcons;

    public ViewPagerAdapter(FragmentManager fragmentManager, boolean showOnlyIcons){
        super(fragmentManager);
        fragmentList = new ArrayList<>();
        fragmentTitleList = new ArrayList<>();
        this.showOnlyIcons = showOnlyIcons;
    }

    public void addFragment(Fragment fragment, String title){
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence title = null;
        if(!showOnlyIcons)
            title = fragmentTitleList.get(position);
        return title;
    }
}

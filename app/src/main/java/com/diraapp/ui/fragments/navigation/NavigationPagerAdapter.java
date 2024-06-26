package com.diraapp.ui.fragments.navigation;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.diraapp.ui.fragments.navigation.account.AccountFragment;
import com.diraapp.ui.fragments.navigation.explore.ExploreFragment;
import com.diraapp.ui.fragments.navigation.selector.RoomSelectorFragment;

/**
 * @noinspection deprecation
 */
public class NavigationPagerAdapter extends FragmentPagerAdapter {


    public NavigationPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ExploreFragment();
            case 1:
                return new RoomSelectorFragment();
            default:
                return new AccountFragment();
        }
    }

    @Override
    public int getCount() {
        return 3; // number of pages
    }
}

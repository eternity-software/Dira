package com.diraapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.diraapp.R;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.notifications.Notifier;
import com.diraapp.services.UpdaterService;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.fragments.NavigationPagerAdapter;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.utils.CacheUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;

import com.diraapp.databinding.ActivityNavigationBinding;

public class NavigationActivity extends AppCompatActivity {

    public static final String PENDING_ROOM_SECRET = "pendingRoomSecret";
    public static final String PENDING_ROOM_NAME = "pendingRoomName";
    public static final String CAN_BE_BACK_PRESSED = "canBackPressed";
    private ActivityNavigationBinding binding;

    public static String lastOpenedRoomId = null;
    private boolean canBackPress = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startService(new Intent(this, UpdaterService.class));

        setupViewPager();

        if (getIntent().hasExtra(PENDING_ROOM_SECRET)) {
            if (getIntent().getExtras().getString(PENDING_ROOM_SECRET) != null) {
                Intent notificationIntent = new Intent(this, RoomActivity.class);

                lastOpenedRoomId = getIntent().getExtras().getString(PENDING_ROOM_SECRET);


                RoomActivity.putRoomExtrasInIntent(notificationIntent,
                        lastOpenedRoomId, getIntent().getExtras().getString(PENDING_ROOM_NAME));
                startActivity(notificationIntent);
            }
        }
        if (getIntent().hasExtra(CAN_BE_BACK_PRESSED)) {
            canBackPress = getIntent().getExtras().getBoolean(CAN_BE_BACK_PRESSED);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        Notifier.cancelAllNotifications(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        if (canBackPress) {
            super.onBackPressed();
        }
    }



    private void setupViewPager() {
        NavigationPagerAdapter adapter = new NavigationPagerAdapter(getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // Sync bottom navigation view with viewpager
                binding.navView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        binding.navView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_explore:
                    binding.viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_room_selector:
                    binding.viewPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_account:
                    binding.viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        });

        binding.navView.setSelectedItemId(R.id.navigation_room_selector);
    }

}
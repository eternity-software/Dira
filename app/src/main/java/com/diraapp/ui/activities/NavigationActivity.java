package com.diraapp.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.diraapp.R;
import com.diraapp.databinding.ActivityNavigationBinding;
import com.diraapp.exceptions.LanguageParsingException;
import com.diraapp.notifications.Notifier;
import com.diraapp.res.Theme;
import com.diraapp.services.UpdaterService;
import com.diraapp.ui.activities.fragments.NavigationPagerAdapter;
import com.diraapp.ui.activities.room.RoomActivity;

public class NavigationActivity extends DiraActivity {

    public static final String PENDING_ROOM_SECRET = "pendingRoomSecret";
    public static final String PENDING_ROOM_NAME = "pendingRoomName";
    public static final String CAN_BE_BACK_PRESSED = "canBackPressed";
    private ActivityNavigationBinding binding;

    public static String lastOpenedRoomId = null;
    private boolean canBackPress = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Theme.loadCurrentTheme(this);
        } catch (LanguageParsingException e) {

        }
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

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
        binding.navView.setSelectedItemId(R.id.navigation_room_selector);
        binding.viewPager.setCurrentItem(1);


    }



    @Override
    protected void onResume() {
        super.onResume();
        setNavigationColors();
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


    }


    private void setNavigationColors()
    {



        ViewCompat.setOnApplyWindowInsetsListener(binding.navView, (v, insets) -> {

            Insets insetsSystemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Get the bottom inset (navigation bar height)
            int navBarHeight = insetsSystemBars.bottom;

            // Apply the bottom inset as padding to the BottomNavigationView
            binding.navView.setPadding(0, 0, 0, navBarHeight);

            // Return insets with any additional insets consumed
            return insets.consumeSystemWindowInsets();
        });

        int selectedColor = Theme.getColor(this, R.color.accent); // Change to your desired color
        int defaultColor = Theme.getColor(this, R.color.navigation_light_gray); // Change to your desired color

        ColorStateList iconColors = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        selectedColor,
                        defaultColor
                }
        );
        binding.navView.setItemIconTintList(iconColors);
        binding.navView.setItemTextColor(iconColors);


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


    }

}
package com.nesmelov.alexey.gpstracker.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.widget.Toast;

import com.nesmelov.alexey.gpstracker.R;
import com.nesmelov.alexey.gpstracker.ui.adapters.ViewPagerAdapter;
import com.nesmelov.alexey.gpstracker.ui.fragments.AlarmFragment;
import com.nesmelov.alexey.gpstracker.ui.fragments.MapFragment;
import com.nesmelov.alexey.gpstracker.ui.fragments.TracksFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity, that manages pages.
 */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 0;
    private static final int MAP_PAGE = 0;
    private static final int ALARM_PAGE = 1;
    private static final int PATH_PAGE = 2;

    private static final SparseIntArray FRAGMENT_MAP = new SparseIntArray();
    static {
        FRAGMENT_MAP.append(R.id.menu_item_map, MAP_PAGE);
        FRAGMENT_MAP.append(R.id.menu_item_alarm, ALARM_PAGE);
        FRAGMENT_MAP.append(R.id.menu_item_path, PATH_PAGE);
    }

    private static final SparseIntArray TAB_MAP = new SparseIntArray();
    static {
        TAB_MAP.append(MAP_PAGE, R.id.menu_item_map);
        TAB_MAP.append(ALARM_PAGE, R.id.menu_item_alarm);
        TAB_MAP.append(PATH_PAGE, R.id.menu_item_path);
    }

    @BindView(R.id.navigationView) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.viewPager) ViewPager mViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        ButterKnife.bind(this);
        setupViewPager(mViewPager);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this::selectFragment);

        if ( Build.VERSION.SDK_INT > 24 && ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE &&
                (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, R.string.location_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Selects current fragment by menu item.
     *
     * @param menuItem menu item to select tab.
     */
    private boolean selectFragment(final MenuItem menuItem) {
        return selectFragment(FRAGMENT_MAP.get(menuItem.getItemId()));
    }

    /**
     * Selects current fragment by page num.
     *
     * @param pageNum page number to select tab.
     */
    private boolean selectFragment(final int pageNum) {
        mViewPager.setCurrentItem(pageNum, true);
        return true;
    }

    /**
     * Setups view pager.
     *
     * @param viewPager view pager to setup.
     */
    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(MapFragment.getInstance(), getString(R.string.map));
        adapter.addFragment(AlarmFragment.getInstance(), getString(R.string.alarms));
        adapter.addFragment(TracksFragment.getInstance(), getString(R.string.my_tracks));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                // we don't need this callback
            }

            @Override
            public void onPageSelected(final int position) {
                mBottomNavigationView.setSelectedItemId(TAB_MAP.get(position));
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                // we don't need this callback
            }
        });
    }
}

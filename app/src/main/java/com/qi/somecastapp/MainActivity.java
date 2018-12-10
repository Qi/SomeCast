package com.qi.somecastapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private Toolbar toolbar;
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                //TODO: change icons
                case R.id.navigation_home:
                    toolbar.setTitle(R.string.title_home);
                    fragment = new SubscriptionFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_discover:
                    toolbar.setTitle(R.string.title_discover);
                    if (haveStoragePermission(1)) {
                        fragment = new DiscoverFragment();
                        loadFragment(fragment);
                        return true;
                    }
                    return false;
                case R.id.navigation_downloads:
                    toolbar.setTitle(R.string.title_downloads);
                    if (haveStoragePermission(2)) {
                        fragment = new DownloadsFragment();
                        loadFragment(fragment);
                        return true;
                    }
                    return false;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0));
    }

    private boolean haveStoragePermission(int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG,"Have permission");
                return true;
            } else {
                Log.d(TAG,"Asking for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, id);
                return false;
            }
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(requestCode));
        } else {
            mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0));
        }
    }

}

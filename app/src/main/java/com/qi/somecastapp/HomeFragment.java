package com.qi.somecastapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class HomeFragment extends Fragment {
    public HomeFragment() {
    }

    public static HomeFragment getInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

}

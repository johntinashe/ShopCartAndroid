package com.shopcart.shopcart.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.shopcart.shopcart.LoginFragment;
import com.shopcart.shopcart.RegisterFragment;

public class SectionsAdapter extends FragmentPagerAdapter {

    public SectionsAdapter(FragmentManager supportFragmentManager) {
        super(supportFragmentManager);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {

        switch (position){
            case 0:
                return new LoginFragment();
            case 1:
                return new RegisterFragment();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Sign In";
            case 1:
                return "Register";
            default:
                return null;
        }
    }
}

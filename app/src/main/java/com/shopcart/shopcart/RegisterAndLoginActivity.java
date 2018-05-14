package com.shopcart.shopcart;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shopcart.shopcart.adapters.SectionsAdapter;

public class RegisterAndLoginActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    private SectionsAdapter sectionsAdapter;
    private View loginIndicator,registerIndicator;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_and_login);

        viewPager = findViewById(R.id.viewPager);
        sectionsAdapter = new SectionsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsAdapter);

        title = findViewById(R.id.title_textView);
        loginIndicator = findViewById(R.id.loginIndicator);
        registerIndicator = findViewById(R.id.registerIndicator);

        //setting the title
        viewPager.setOnPageChangeListener(this);
        viewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        switch (position){
            case 0:{
                title.setText(R.string.login);
                loginIndicator.setBackgroundColor(getResources().getColor(R.color.green_bg));
                registerIndicator.setBackgroundColor(getResources().getColor(R.color.light_gray));
                break;
            }
            case 1:{
                title.setText(R.string.register);
                loginIndicator.setBackgroundColor(getResources().getColor(R.color.light_gray));
                registerIndicator.setBackgroundColor(getResources().getColor(R.color.green_bg));
                break;
            }
            default:break;
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}

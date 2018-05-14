package com.shopcart.shopcart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shopcart.shopcart.adapters.SliderAdapter;

public class GetStartedActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private SliderAdapter adapter;
    private Button finish,btn_nxt,btn_prev;
    private TextView[] dots;

    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        btn_nxt = findViewById(R.id.button_next);
        btn_prev = findViewById(R.id.button_previous);
        finish = findViewById(R.id.button_finish);

        viewPager = findViewById(R.id.viewPager);
        linearLayout = findViewById(R.id.linear_layout);
        adapter = new SliderAdapter(this);
        viewPager.setAdapter(adapter);
        addDot(0);
        viewPager.setOnPageChangeListener(pageChangeListener);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewPager.getCurrentItem()==2){
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("key", "true");
                    editor.apply();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
            }
        });

        btn_nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(currentPage +1);
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(currentPage -1);
            }
        });



    }

    public void addDot(int position){
        dots = new TextView[3];
        linearLayout.removeAllViews();

        for(int i=0;i<dots.length;i++){
            dots[i] =new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            dots[i].setTextSize(35);
            linearLayout.addView(dots[i]);
        }

        if(dots.length>0){
            dots[position].setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    ViewPager.OnPageChangeListener pageChangeListener =new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDot(position);
            currentPage=position;

            if(position==0){
                btn_prev.setEnabled(false);
                btn_prev.setVisibility(View.INVISIBLE);
                btn_nxt.setEnabled(true);
                btn_nxt.setText("Next");
                finish.setVisibility(View.INVISIBLE);
                finish.setEnabled(false);
                btn_nxt.setVisibility(View.VISIBLE);
            }else if(position==1){
                btn_prev.setEnabled(true);
                btn_prev.setVisibility(View.VISIBLE);
                btn_nxt.setEnabled(true);
                btn_nxt.setVisibility(View.VISIBLE);
                finish.setVisibility(View.INVISIBLE);
                finish.setEnabled(false);
            }else if(position==2){
                btn_prev.setEnabled(true);
                btn_prev.setVisibility(View.VISIBLE);
                btn_nxt.setEnabled(false);
                btn_nxt.setText("Next");
                btn_nxt.setVisibility(View.INVISIBLE);
                finish.setVisibility(View.VISIBLE);
                finish.setEnabled(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}

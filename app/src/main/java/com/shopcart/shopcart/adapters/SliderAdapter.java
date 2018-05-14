package com.shopcart.shopcart.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shopcart.shopcart.R;


public class SliderAdapter extends PagerAdapter {
    private Context context;

    public SliderAdapter(Context context){
        this.context = context;
    }

    private int imagesArray [] ={
            R.drawable.carrefour,
            R.drawable.shutterstock,
            R.drawable.carrefour
    };

    private String headings []={
            "Welcome to Shop",
            "The Best on Market",
            "Best Recipes"
    };

    private String description[]= {
            "Introducing Shop Cart, with more than 6 thousand recipes and amazing features  is your best choice to make any cook!",
             "Dear guests, you are welcomed to dine with us at Good Food restaurant. We will serve you with the mouth watering dishes. Have a pleasant dining experience.",
            " Shop Cart has the experience and know how to provide you excellent customer service when it comes to restaurant mystery shopping. We provide mystery shopping services to restaurants and other business types which serve food. From quick serve to 5-star "
    };



    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = layoutInflater.inflate(R.layout.content,container,false);

        TextView des = view.findViewById(R.id.description);
        TextView hd  = view.findViewById(R.id.heading);
        ImageView im = view.findViewById(R.id.icon_image);

        des.setText(description[position]);
        hd.setText(headings[position]);
        //im.setImageResource(imagesArray[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}

package com.shopcart.shopcart.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopcart.shopcart.R;
import com.shopcart.shopcart.Utils.GetLastSeen;
import com.shopcart.shopcart.models.Review;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RecyclerAdapterCustom extends RecyclerView.Adapter<RecyclerAdapterCustom.ReviewHolder> {
    private ArrayList<Review> list;
    private Context context;

    public RecyclerAdapterCustom(ArrayList<Review> list ,Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_layout,parent,false);
        return new ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ReviewHolder holder, int position) {
        final Review review = list.get(position);
        holder.name.setText(review.getUser());
        holder.review.setText(review.getReview());
        holder.ratingBar.setRating(review.getRating());
        holder.time.setText(GetLastSeen.getTimeAgo(review.getTime(),context));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

     class ReviewHolder extends RecyclerView.ViewHolder {
        TextView review,name,time;
        MaterialRatingBar ratingBar;
        View view;

        ReviewHolder(View itemView) {
            super(itemView);
            view = itemView;

            review = view.findViewById(R.id.review_message_tv);
            name = view.findViewById(R.id.review_user);
            time = view.findViewById(R.id.review_time);
            ratingBar = view.findViewById(R.id.materialRatingBar);
        }
    }



}

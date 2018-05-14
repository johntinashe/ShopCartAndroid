package com.shopcart.shopcart;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shopcart.shopcart.Utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactUsFragment extends Fragment {


    public ContactUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        CardView message = view.findViewById(R.id.contact_us_btn_message);
        CardView track  = view.findViewById(R.id.trackCardView);

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if(activity != null)
                    if(Utils.testForConnection(activity))
                        startActivity(new Intent(getActivity(),TrackOrderActivity.class));
                    else
                        Toast.makeText(activity, "Make Sure you have an active Internet Connection", Toast.LENGTH_SHORT).show();

            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity activity = getActivity();
                if(activity != null)
                    if(Utils.testForConnection(activity))
                        startActivity(new Intent(getActivity(),ChatActivity.class));
                    else
                        Toast.makeText(activity, "Make Sure you have an active Internet Connection", Toast.LENGTH_SHORT).show();


            }
        });
        return view;
    }

}

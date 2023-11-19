package com.example.desicuisine.User.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.UserSpecialOrderActivity;
import com.example.desicuisine.User.Activities.ViewKitchenActivity;
import com.example.desicuisine.User.Activities.ViewOrderUserActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment implements View.OnClickListener {


    private View view;

    private CardView myInProcesssCv, myAllOrderCV, newSpecialOrderUserCV, pendingSpecialOrderCv, pendingOrderUserCV, viewKitchenCV;

    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order, container, false);

        connectivity();

        myInProcesssCv.setOnClickListener(this);
        myAllOrderCV.setOnClickListener(this);
        newSpecialOrderUserCV.setOnClickListener(this);
        pendingSpecialOrderCv.setOnClickListener(this);
        pendingOrderUserCV.setOnClickListener(this);
        viewKitchenCV.setOnClickListener(this);
        return view;

    }

    private void connectivity() {
        CollapsingToolbarLayout layout = getActivity().findViewById(R.id.colapseToolbarUser);
        layout.setTitle("Orders");
        newSpecialOrderUserCV = view.findViewById(R.id.newSpecialOrderUserCV);
        myInProcesssCv = view.findViewById(R.id.myInProcesssCv);
        pendingSpecialOrderCv = view.findViewById(R.id.myPendingSpecialOrderCv);
        myAllOrderCV = view.findViewById(R.id.myAllOrderCV);
        viewKitchenCV = view.findViewById(R.id.viewKitchenCv);
        pendingOrderUserCV = view.findViewById(R.id.pendingOrderUserCV);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        Bundle bundle;
        Intent i;
        switch (id) {
            case R.id.myAllOrderCV:

                bundle = new Bundle();
                bundle.putString("CHOISE", "My All Orders");
                i = new Intent(getContext(), ViewOrderUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;

            case R.id.myInProcesssCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "My In Process Orders");
                i = new Intent(getContext(), ViewOrderUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.pendingOrderUserCV:
                bundle = new Bundle();
                bundle.putString("CHOISE", "My Pending Orders");
                i = new Intent(getContext(), ViewOrderUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;


            case R.id.newSpecialOrderUserCV:
                i = new Intent(getContext(), UserSpecialOrderActivity.class);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.myPendingSpecialOrderCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "Special Orders");
                i = new Intent(getContext(), ViewOrderUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();

                break;
            case R.id.viewKitchenCv:
                i = new Intent(getContext(), ViewKitchenActivity.class);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();

                break;

        }
    }


}


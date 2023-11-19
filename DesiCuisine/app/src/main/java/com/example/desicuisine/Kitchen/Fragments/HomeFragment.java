package com.example.desicuisine.Kitchen.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.Kitchen.Activities.ViewOrderKitchenActivity;
import com.example.desicuisine.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private View view;

    private CardView pendingOrderCv, inProcesssCv, allOrderCV, cencelOrderCV, pendingSpecialOrdersCV, inProcessSpecialCV;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        connectivity();

        pendingOrderCv.setOnClickListener(this);
        inProcesssCv.setOnClickListener(this);
        allOrderCV.setOnClickListener(this);
        cencelOrderCV.setOnClickListener(this);
        pendingSpecialOrdersCV.setOnClickListener(this);
        inProcessSpecialCV.setOnClickListener(this);

        return view;

    }

    private void connectivity() {

        allOrderCV = view.findViewById(R.id.allOrderCV);
        pendingOrderCv = view.findViewById(R.id.pendingOrderCv);
        cencelOrderCV = view.findViewById(R.id.cencelOrderCV);
        inProcesssCv = view.findViewById(R.id.inProcesssCv);
        pendingSpecialOrdersCV = view.findViewById(R.id.pendingSpecialOrderCV);
        inProcessSpecialCV = view.findViewById(R.id.pendingSpecialOrderCv);
        getActivity().setTitle("Orders");

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        Bundle bundle;
        Intent i;
        switch (id) {
            case R.id.pendingOrderCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "Pending Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();

                break;
            case R.id.allOrderCV:
                bundle = new Bundle();
                bundle.putString("CHOISE", "All Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.inProcesssCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "In Process Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.cencelOrderCV:
                bundle = new Bundle();
                bundle.putString("CHOISE", "Cenceled Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.pendingSpecialOrderCV:
                bundle = new Bundle();
                bundle.putString("CHOISE", "Pending Special Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.pendingSpecialOrderCv:
                bundle = new Bundle();
                bundle.putString("CHOISE", "In Process Special Orders");
                i = new Intent(getContext(), ViewOrderKitchenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
        }
    }
}

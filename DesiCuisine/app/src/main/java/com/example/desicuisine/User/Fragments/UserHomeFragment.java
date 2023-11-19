package com.example.desicuisine.User.Fragments;


import android.app.ProgressDialog;
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
import com.example.desicuisine.User.Activities.ProductsUserActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserHomeFragment extends Fragment implements View.OnClickListener {
    private View view;

    private CardView goshtCv, chawalCv, daalCV, sabziCV, seasonalCV, drinkCV, otherCategoryCv, chineesCv;

    private ProgressDialog progressDialog;


    public UserHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_home, container, false);

        connectivity();

        goshtCv.setOnClickListener(this);
        chawalCv.setOnClickListener(this);
        daalCV.setOnClickListener(this);
        sabziCV.setOnClickListener(this);
        seasonalCV.setOnClickListener(this);
        drinkCV.setOnClickListener(this);
        otherCategoryCv.setOnClickListener(this);
        chineesCv.setOnClickListener(this);

        return view;

    }



    private void connectivity() {

        CollapsingToolbarLayout layout = getActivity().findViewById(R.id.colapseToolbarUser);
        layout.setTitle("Categories");
        goshtCv = view.findViewById(R.id.goshtCv);
        chawalCv = view.findViewById(R.id.chawalCv);
        daalCV = view.findViewById(R.id.daalCV);
        sabziCV = view.findViewById(R.id.sabziCv);
        seasonalCV = view.findViewById(R.id.seasonalCv);
        drinkCV = view.findViewById(R.id.traditionalCv);
        otherCategoryCv = view.findViewById(R.id.otherCatgryCv);
        chineesCv = view.findViewById(R.id.chineesCv);

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        Bundle bundle;
        Intent i;
        switch (id) {
            case R.id.goshtCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Gosht");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.chawalCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Chawal");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.daalCV:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Daal");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.sabziCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Sabzi");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.seasonalCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Seasonal");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.traditionalCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Traditional");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.otherCatgryCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Other");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
            case R.id.chineesCv:

                bundle = new Bundle();
                bundle.putString("CHOISE", "Chinees");
                i = new Intent(getContext(), ProductsUserActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                Animatoo.animateCard(getContext());
                getActivity().finish();
                break;
        }
    }
}

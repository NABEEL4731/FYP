package com.example.desicuisine.User.Fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.desicuisine.R;
import com.example.desicuisine.User.Activities.ViewWeeklyOfferUserActivity;
import com.example.desicuisine.User.Activities.ViewWeeklyOrderUserActivity;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeeklySubscriptionUserFragment extends Fragment implements View.OnClickListener {
    private CardView pendingCV, subsrbCv, rejectCV, allOfferCv;
    private View view;
    private SharedPreferences sp;
    private FirebaseAuth mAuth;

    private String mySp = "MYSP";


    public WeeklySubscriptionUserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_weekly_subscription_user, container, false);
        connectivity();

        pendingCV.setOnClickListener(this);
        subsrbCv.setOnClickListener(this);
        rejectCV.setOnClickListener(this);
        allOfferCv.setOnClickListener(this);

        return view;
    }

    private void connectivity() {
        CollapsingToolbarLayout layout = getActivity().findViewById(R.id.colapseToolbarUser);
        layout.setTitle("Weekly Menu");
        pendingCV = view.findViewById(R.id.pendingWkOfersUserCv);
        subsrbCv = view.findViewById(R.id.mySubsUserCv);
        rejectCV = view.findViewById(R.id.viewRejectedOfersCv);
        allOfferCv = view.findViewById(R.id.viewAllOfersUserCv);
        sp = getActivity().getSharedPreferences(mySp, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {

        Intent intent;
        int id = v.getId();
        switch (id) {
            case R.id.pendingWkOfersUserCv:
                intent = new Intent(getActivity(), ViewWeeklyOrderUserActivity.class);
                intent.putExtra("CHOISE", "Pending Offers");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();

                break;
            case R.id.mySubsUserCv:
                intent = new Intent(getActivity(), ViewWeeklyOrderUserActivity.class);
                intent.putExtra("CHOISE", "Accepted");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.viewRejectedOfersCv:
                intent = new Intent(getActivity(), ViewWeeklyOrderUserActivity.class);
                intent.putExtra("CHOISE", "Rejected");
                startActivity(intent);
                Animatoo.animateCard(getActivity());
                getActivity().finish();
                break;
            case R.id.viewAllOfersUserCv:

                Intent intent1 = new Intent(getActivity(), ViewWeeklyOfferUserActivity.class);
                startActivity(intent1);
                Animatoo.animateCard(getActivity());
                getActivity().finish();

                break;
        }
    }

}

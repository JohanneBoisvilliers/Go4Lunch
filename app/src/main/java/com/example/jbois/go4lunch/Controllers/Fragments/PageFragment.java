package com.example.jbois.go4lunch.Controllers.Fragments;


import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jbois.go4lunch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageFragment extends Fragment {

    private static final String KEY_POSITION="position";

    public PageFragment() {}

    public static PageFragment newInstance(int position) {

        //Create new fragment
        PageFragment frag = new PageFragment();

        //Create bundle and add it some data
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        frag.setArguments(args);

        return(frag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get layout of PageFragment
        View result = inflater.inflate(R.layout.fragment_page, container, false);

        //Get widgets from layout and serialise it
        LinearLayout rootView= (LinearLayout) result.findViewById(R.id.fragment_page_rootview);
        TextView textView= (TextView) result.findViewById(R.id.fragment_page_title);

        //Get data from Bundle (created in method newInstance)
        int position = getArguments().getInt(KEY_POSITION, -1);

        //Update widgets with it
        textView.setText("Page numéro "+position);


        return result;
    }

}

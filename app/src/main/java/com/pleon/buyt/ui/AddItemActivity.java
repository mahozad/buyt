package com.pleon.buyt.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.net.Uri;
import android.os.Bundle;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.AddItemFragment.OnFragmentInteractionListener;

public class AddItemActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment addItemFragment = fragMgr.findFragmentById(R.id.container_fragment_add_item);
        if (addItemFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_add_item, new AddItemFragment())
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //
    }
}

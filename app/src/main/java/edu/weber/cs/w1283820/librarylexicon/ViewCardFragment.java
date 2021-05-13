package edu.weber.cs.w1283820.librarylexicon;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

public class ViewCardFragment extends Fragment {

    private View root;
    private String imageUrl;

    public ViewCardFragment(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return root = inflater.inflate(R.layout.fragment_view_card, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SimpleDraweeView draweeView = (SimpleDraweeView) root.findViewById(R.id.searchedCardCardDisplay);
        draweeView.setImageURI(imageUrl);



    }
}
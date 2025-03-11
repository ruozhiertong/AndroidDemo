package com.example.safetywalk2.ui.fragmet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.safetywalk2.R;

public class IntroPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_page, container, false);
        Bundle args = getArguments();
        if (args != null) {
            ((ImageView) view.findViewById(R.id.image)).setImageResource(args.getInt("image"));
            ((TextView) view.findViewById(R.id.title)).setText(args.getString("title"));
            ((TextView) view.findViewById(R.id.description)).setText(args.getString("desc"));
        }
        return view;
    }
}
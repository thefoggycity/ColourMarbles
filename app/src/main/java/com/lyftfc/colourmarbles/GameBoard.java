package com.lyftfc.colourmarbles;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Locale;
import java.util.Vector;

public class GameBoard extends Fragment {

    private final int GB_SIZE = 9;

    private GameBoardViewModel mViewModel;
    private Vector<LinearLayout> rows;
    private Vector<Button> btns;

    public static GameBoard newInstance() {
        return new GameBoard();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.game_board_fragment, container, false);
        rows = new Vector<>();
        btns = new Vector<>();
        LinearLayout gbMainLayout = root.findViewById(R.id.gbMainLayout);

        LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        // Create horizontal linear layouts for the grid
        for (int i = 0; i < GB_SIZE; i++) {
            LinearLayout l = new LinearLayout(getActivity());
            l.setId(View.generateViewId());
            l.setOrientation(LinearLayout.HORIZONTAL);
            l.setLayoutParams(rowLayoutParams);
            rows.add(l);
            // Create buttons in this row
            for (int j = 0; j < GB_SIZE; j++) {
                Button b = new Button(getActivity(), null,
                        android.R.style.DeviceDefault_SegmentedButton);
                b.setId(View.generateViewId());
                b.setText(String.format(Locale.ENGLISH,"%d, %d", i, j));
                b.setLayoutParams(btnLayoutParams);
                b.setOnClickListener(v -> {
                    Button btn = (Button)v;
                    Toast.makeText(getActivity(), btn.getText(), Toast.LENGTH_SHORT).show();
                });
                btns.add(b);
                l.addView(b);
            }
            gbMainLayout.addView(l);
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GameBoardViewModel.class);
        // TODO: Use the ViewModel
    }

}

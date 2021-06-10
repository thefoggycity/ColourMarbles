package com.lyftfc.colourmarbles;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.ArrayList;
import java.util.Stack;

public class GameBoard extends Fragment {

    private final int GB_SIZE = 9;  // Game board is 9-by-9
    private final int NUM_COLOUR = 5;   // Number of colours of marbles
    private final int M_PER_TURN = 3;   // Every turn adds 3 marbles
    private final int MIN_ERASE = 5;    // At least 5 marbles in a line to be erased

    private GameBoardViewModel mViewModel;
    private ArrayList<LinearLayout> rows;
    private ArrayList<Button> btns;
    private Pair<Integer, Integer> selBtn;
    private Random rnd;
    private ArrayList< ArrayList<Integer> > btnContent;

    private Integer score;

    public static GameBoard newInstance() {
        return new GameBoard();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.game_board_fragment, container, false);
        rows = new ArrayList<>();
        btns = new ArrayList<>();
        rnd = new Random();
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
                Button b = new Button(getActivity(), null);
//                        android.R.style.DeviceDefault_SegmentedButton);
                b.setId(View.generateViewId());
                b.setText(String.format(Locale.ENGLISH,"%d, %d", i, j));
                b.setLayoutParams(btnLayoutParams);
                b.setTag(new Integer[] {i, j});
                b.setOnClickListener(this::gameButtonOnClick);
                btns.add(b);
                l.addView(b);
            }
            gbMainLayout.addView(l);
        }

        selBtn = null;
        btnContent = new ArrayList<>();
        for (int i = 0; i < GB_SIZE; i++) {
            ArrayList<Integer> ar = new ArrayList<>();
            btnContent.add(ar);
            for (int j = 0; j < GB_SIZE; j++) {
                ar.add(0);
                setBtnContent(new Pair<>(i, j), 0);
            }
        }

        score = 0;
        randomAddMarbles(M_PER_TURN);

        return root;
    }

    private int getMarbleColour(int clrId) {
        int res;
        switch (clrId) {
            case 0:
            default:
                res = getResources().getColor(R.color.gbBlank); break;
            case 1:
                res = getResources().getColor(R.color.gbBlue); break;
            case 2:
                res = getResources().getColor(R.color.gbGreen); break;
            case 3:
                res = getResources().getColor(R.color.gbOrange); break;
            case 4:
                res = getResources().getColor(R.color.gbPurple); break;
            case 5:
                res = getResources().getColor(R.color.gbRed); break;
        }
        return res;
    }

    private void setBtnContent(Pair<Integer, Integer> coord, int clrId) {
        Button b = getButton(coord);
        b.setBackgroundColor(getMarbleColour(clrId));
        btnContent.get(coord.first).set(coord.second, clrId);
    }

    private int getBtnContent(Pair<Integer, Integer> coord) {
        return btnContent.get(coord.first).get(coord.second);
    }

    private Button getButton(Pair<Integer, Integer> coord) {
        return btns.get(coord.first * GB_SIZE + coord.second);
    }

    private boolean randomAddMarbles(int count) {
        ArrayList< Pair<Integer, Integer> > emptyBlocks = new ArrayList<>();
        for (int i = 0; i < GB_SIZE; i++) {
            for (int j = 0; j < GB_SIZE; j++) {
                if (getBtnContent(new Pair<>(i, j)) == 0)
                    emptyBlocks.add(new Pair<>(i, j));
            }
        }

        boolean boardFull = false;
        if (emptyBlocks.size() <= count) {
            boardFull = true;
            count = emptyBlocks.size();
        }
        Collections.shuffle(emptyBlocks);
        for (int i = 0; i < count; i++)
            setBtnContent(emptyBlocks.get(i), rnd.nextInt(NUM_COLOUR) + 1);
        return boardFull;
    }

    private void gameButtonOnClick(View v) {
        Button btn = (Button)v;
        Pair<Integer, Integer> coord = new Pair<>(
                ((Integer[])btn.getTag())[0], ((Integer[])btn.getTag())[1]);

        if (selBtn == null) {   // Select a button
            if (getBtnContent(coord) != 0) {
                selBtn = coord;
                btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        } else if (selBtn.equals(coord)) {  // Deselect the button
            selBtn = null;
            btn.setBackgroundColor(getMarbleColour(getBtnContent(coord)));
        } else {    // Move the marble
            if (getBtnContent(coord) == 0 && hasPath(selBtn, coord)) {
                setBtnContent(coord, getBtnContent(selBtn));
                setBtnContent(selBtn, 0);
                selBtn = null;
                settleTurn();
            } else {    // Destination is already occupied, deselect
                getButton(selBtn).setBackgroundColor(getMarbleColour(getBtnContent(selBtn)));
                selBtn = null;
            }
        }
    }

    private boolean hasPath(Pair<Integer, Integer> src, Pair<Integer, Integer> dst) {
        boolean[][] flood = new boolean[GB_SIZE][GB_SIZE];
        Stack< Pair<Integer, Integer> > traverse = new Stack<>();
        traverse.push(src);
        for (int i = 0; i < GB_SIZE; i++) {
            for (int j = 0; j < GB_SIZE; j++) {
                flood[i][j] = getBtnContent(new Pair<>(i, j)) != 0;
            }
        }
        while (!traverse.isEmpty()) {
            Integer x = traverse.peek().first;
            Integer y = traverse.peek().second;
            flood[x][y] = true;
            if (x < GB_SIZE - 1 && !flood[x + 1][y]) {  // Move right
                traverse.push(new Pair<>(x + 1, y));
            } else if (y < GB_SIZE - 1 && !flood[x][y + 1]) {   // Move down
                traverse.push(new Pair<>(x, y + 1));
            } else if (x > 0 && !flood[x - 1][y]) { // Move left
                traverse.push(new Pair<>(x - 1, y));
            } else if (y > 0 && !flood[x][y - 1]) { // Move up
                traverse.push(new Pair<>(x, y - 1));
            } else {    // No way, jump back
                traverse.pop();
            }
        }
        return flood[dst.first][dst.second];
    }

    private boolean eraseMarbles() {
        // Erase marbles that are in lines
        boolean[][] eraseMap = new boolean[GB_SIZE][GB_SIZE];
        for (int i = 0; i < GB_SIZE; i++) {
            for (int j = 0; j < GB_SIZE; j++) {
                eraseMap[i][j] = false;
            }
        }
        for (int i = 0; i < GB_SIZE; i++) {
            for (int j = 0; j < GB_SIZE; j++) {
                int colour = getBtnContent(new Pair<>(i, j));
                if (colour == 0)
                    continue;
                int count;
                // Extend to right
                for (count = 1; count < GB_SIZE - i; count++)
                    if (getBtnContent(new Pair<>(i + count, j)) != colour) break;
                if (count >= MIN_ERASE)
                    for (int k = 0; k < count; k++)
                        eraseMap[i + k][j] = true;
                // Extend to right down
                for (count = 1; count < GB_SIZE - i && count < GB_SIZE - j; count++)
                    if (getBtnContent(new Pair<>(i + count, j + count)) != colour) break;
                if (count >= MIN_ERASE)
                    for (int k = 0; k < count; k++)
                        eraseMap[i + k][j + k] = true;
                // Extend to down
                for (count = 1; count < GB_SIZE - j; count++)
                    if (getBtnContent(new Pair<>(i, j + count)) != colour) break;
                if (count >= MIN_ERASE)
                    for (int k = 0; k < count; k++)
                        eraseMap[i][j + k] = true;
                // Extend to left down
                for (count = 1; count < GB_SIZE - j && count <= i; count++)
                    if (getBtnContent(new Pair<>(i - count, j + count)) != colour) break;
                if (count >= MIN_ERASE)
                    for (int k = 0; k < count; k++)
                        eraseMap[i - k][j + k] = true;
            }
        }
        int numErased = 0;
        for (int i = 0; i < GB_SIZE; i++) {
            for (int j = 0; j < GB_SIZE; j++) {
                if (eraseMap[i][j]) {
                    numErased++;
                    setBtnContent(new Pair<>(i, j), 0);
                }
            }
        }

        score += numErased * 2;
        return numErased != 0;
    }

    private void settleTurn() {
        // Add extra marbles
        if (!eraseMarbles()) {
            boolean boardFull = randomAddMarbles(M_PER_TURN);
            if (boardFull) {
                Toast.makeText(getActivity(),
                        getText(R.string.game_over), Toast.LENGTH_SHORT).show();
                return;
            }
            eraseMarbles();
        } else {
            Toast.makeText(getActivity(),
                    String.format(Locale.ENGLISH, "Current score: %d", score),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(GameBoardViewModel.class);
        // TODO: Use the ViewModel
    }

}

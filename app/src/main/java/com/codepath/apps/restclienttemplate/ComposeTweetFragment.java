package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ComposeTweetFragment extends DialogFragment implements TextView.OnEditorActionListener {
    private EditText et;

    public interface ComposeTweetListener {
        void onFinishedTweet(String inputText);
    }

    public ComposeTweetFragment() {

    }

    public static ComposeTweetFragment newInstance(String title) {
        ComposeTweetFragment frag = new ComposeTweetFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_compose, container);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        et = (EditText) view.findViewById(R.id.etCompose);
        String title = getArguments().getString("content", "");
        getDialog().setTitle(title);
        et.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        );

        et.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_SEND == actionId) {
            ComposeTweetListener listener = (ComposeTweetListener) getActivity();
            listener.onFinishedTweet(et.getText().toString());
            dismiss();
            return true;
        }
        return false;
    }
}

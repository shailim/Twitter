package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

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

        et.setImeOptions(EditorInfo.IME_ACTION_SEND);
        et.setRawInputType(InputType.TYPE_CLASS_TEXT);

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

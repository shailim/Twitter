package com.codepath.apps.restclienttemplate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeTweetListener {

    public static final String TAG = "TimelineActivity";

    Context context;

    TwitterClient client;
    RecyclerView rvTweets;
    FloatingActionButton fabCompose;
    FloatingActionButton toTop;

    List<Tweet> tweets;
    TweetsAdapter adapter;
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);

    private SwipeRefreshLayout swipeContainer;

    MenuItem pb;

    private long max_id;

    private EndlessRecyclerViewScrollListener scrollListener;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
                        tweets.add(0, tweet);
                        adapter.notifyItemInserted(0);
                        rvTweets.smoothScrollToPosition(0);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        context = this.getBaseContext();

        client = TwitterApp.getRestClient(this);

        //find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        fabCompose = findViewById(R.id.fabCompose);
        toTop = findViewById(R.id.toTop);

        //initialize list of tweets
        tweets = new ArrayList<>();
        //adapter = new TweetsAdapter(this, tweets);
        adapter = new TweetsAdapter(this, tweets);

        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(context, ComposeActivity.class);
                //resultLauncher.launch(intent);
                showComposeDialog();
            }
        });

        toTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvTweets.smoothScrollToPosition(0);
            }
        });

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "onLoadMore");
                loadNextData(page);
            }
        };
        rvTweets.addOnScrollListener(scrollListener);
        populateHomeTimeline();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.getHomeTimeline(0, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        adapter.clear();
                        JSONArray jsonArray = json.jsonArray;
                        try {
                            adapter.addAll(Tweet.fromJsonArray(jsonArray));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        swipeContainer.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure: " + response, throwable);
                    }
                });
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        pb = menu.findItem(R.id.progressBar);
        showProgressBar();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       if (item.getItemId() == R.id.logoutBtn) {
            TwitterApp.getRestClient(this).clearAccessToken();

            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        return false;
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess: " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    hideProgressBar();;
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    max_id = tweets.get(tweets.size()-1).id;
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: " + response, throwable);
            }
        });
    }

    public void loadNextData(int page) {
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess: " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.i(TAG, "loading next data");
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    max_id = tweets.get(tweets.size()-1).id;
                    adapter.notifyDataSetChanged();
                    scrollListener.setLoadingFalse();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure: " + response, throwable);
            }
        });
    }

    public void showProgressBar() {
        pb.setVisible(true);
    }

    public void hideProgressBar() {
        pb.setVisible(false);
    }

    @Override
    public void onFinishedTweet(String inputText) {
        Log.i(TAG, "onFinishedTweet");
        Log.i(TAG, inputText);
        client.publishTweet(inputText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    Log.i(TAG, tweet.body);
//                    Intent intent = new Intent();
//                    intent.putExtra("tweet", Parcels.wrap(tweet));
//                    setResult(RESULT_OK);
//                    finish();  //closes activity, passes data to parent
                    tweets.add(0, tweet);
                    adapter.notifyItemInserted(0);
                    rvTweets.smoothScrollToPosition(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        });
    }

    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetFragment composeFrag = ComposeTweetFragment.newInstance("Compose Tweet");
        composeFrag.show(fm, "fragment_compose_tweet");
    }
}
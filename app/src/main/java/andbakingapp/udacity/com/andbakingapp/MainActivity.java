package andbakingapp.udacity.com.andbakingapp;

import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

import andbakingapp.udacity.com.andbakingapp.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String RECIPES_JSON_RESULT = "recipes";
    
    private RecyclerView mRecipeRecyclerView;
    private GridLayoutManager layoutManager;
    private RecipeAdapter mRecipeAdapter;
    private String mJsonResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecipeRecyclerView = (RecyclerView)findViewById(R.id.rv_recipes);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            layoutManager = new GridLayoutManager(this, numberOfColumns());
        } else {
            // In portrait
            layoutManager = new GridLayoutManager(this, 1);
        }

        mRecipeRecyclerView.setLayoutManager(layoutManager);
        mRecipeRecyclerView.setHasFixedSize(true);

        if (isOnline() && savedInstanceState == null) {
            makeRecipeQuery();
            mRecipeAdapter = new RecipeAdapter(mJsonResult);
            mRecipeRecyclerView.setAdapter(mRecipeAdapter);
        } else if (savedInstanceState != null) {
            mJsonResult = savedInstanceState.getString(RECIPES_JSON_RESULT);
            mRecipeAdapter = new RecipeAdapter(mJsonResult);
            mRecipeRecyclerView.setAdapter(mRecipeAdapter);
        } else {
            String message = "Please connect to the Internet";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2; //to keep the grid aspect
        return nColumns;
    }

    private void makeRecipeQuery() {
        try {
            URL recipeUrl = new URL(NetworkUtils.RECIPES_URL);
            mJsonResult = new RecipeQueryTask().execute(recipeUrl).get();
            Log.d(TAG, "makeRecipeQuery: " + mJsonResult.length());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class RecipeQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            URL recipeUrl = urls[0];
            String recipeJson = null;

            try {
                recipeJson = NetworkUtils.getResponseFromHttpUrl(recipeUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return recipeJson;
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mJsonResult != null) {
            outState.putString(RECIPES_JSON_RESULT, mJsonResult);
        } else {
            Log.d(TAG, "onSaveInstanceState: Recipes Json result is null");
        }
            
    }
}

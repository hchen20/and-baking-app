package andbakingapp.udacity.com.andbakingapp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

import andbakingapp.udacity.com.andbakingapp.utils.NetworkUtils;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecipeGrid;
    private RecipeAdapter mRecipeAdapter;
    private String mJsonResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecipeGrid = (RecyclerView)findViewById(R.id.rv_recipes);

        int numOfColumns = 1;

        GridLayoutManager layoutManager = new GridLayoutManager(this,numOfColumns);
        mRecipeGrid.setLayoutManager(layoutManager);
        mRecipeGrid.setHasFixedSize(true);

        if (isOnline()) {
            makeRecipeQuery();
            mRecipeAdapter = new RecipeAdapter(mJsonResult);
            mRecipeGrid.setAdapter(mRecipeAdapter);
        } else {
            String message = "Please connect to the Internet";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
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
}

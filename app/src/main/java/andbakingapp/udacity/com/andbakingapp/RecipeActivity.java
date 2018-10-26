package andbakingapp.udacity.com.andbakingapp;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

// This activity will display a specific recipe and
public class RecipeActivity extends AppCompatActivity
                implements RecipeStepInstructionFragment.OnButtonClickListener,
                           RecipeStepsFragment.OnStepClickListener  {
    private static final String TAG = RecipeActivity.class.getSimpleName();

    private static final String RECIPE_DETAIL = "detail";

    // Class variable to store recipe detail
    private String mRecipeDetail;

    // Track whether to display a two-pane or single-pane UI
    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        mRecipeDetail = getIntent().getStringExtra(RECIPE_DETAIL);
        Log.d(TAG, "onCreate: In RecipeActivity" + mRecipeDetail);
        // Retrieve the specific recipe string from intent

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Determine if the activity is two-pane or single-pane display
        if (findViewById(R.id.recipe_steps_linear_layout) != null) {
            Log.d(TAG, "onCreate: two-pane" + findViewById(R.id.recipe_steps_linear_layout).getId());
            // This LinearLayout will only initially exist in the two-pane tablet case
            mTwoPane = true;

            // Getting rid of the "Next" and "Previous" button that appears on the phone
            if (savedInstanceState == null) {
                // In two-pane mode, add initial RecipesStepsFragment and
                // RecipeStepInstructionFragment to the screen

                RecipeStepsFragment stepsFragment = new RecipeStepsFragment();
                stepsFragment.setRecipeDetail(mRecipeDetail);

                // Add recipe steps fragment to its container using FragmentManager and a Transaction
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .add(R.id.recipe_steps_container, stepsFragment)
                        .commit();

                // Todo (5) Get clicked position
                RecipeStepInstructionFragment newFragment = new RecipeStepInstructionFragment();
                newFragment.setRecipeStepDetail(mRecipeDetail);
                newFragment.setStepIdx(0);
                newFragment.setTwoPane();


                getSupportFragmentManager().beginTransaction()
                        .add(R.id.recipe_steps_linear_layout, newFragment)
                        .commit();

            }
        } else {
            // In single-pane one and displaying fragment in separate activities
            mTwoPane = false;

            if (savedInstanceState != null) {
                mRecipeDetail = savedInstanceState.getString(RECIPE_DETAIL);
            }

            // Only set the RecipeStepsFragment
            RecipeStepsFragment stepsFragment = new RecipeStepsFragment();
            stepsFragment.setRecipeDetail(mRecipeDetail);

            // Add recipe steps fragment to its container using FragmentManager and a Transaction
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.recipe_steps_container, stepsFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            // Close activity or go back to the previous activity
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(RECIPE_DETAIL, mRecipeDetail);
    }

    @Override
    public void onButtonClicked(int id, int index) {
        if (id == R.id.btn_next) {
            Toast.makeText(this, "Next button is clicked", Toast.LENGTH_LONG).show();
            // Start next step
            RecipeStepInstructionFragment newFragment = new RecipeStepInstructionFragment();
            newFragment.setStepIdx(index+1);
            newFragment.setRecipeStepDetail(mRecipeDetail);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_steps_container, newFragment)
                    .commit();
        } else if (id == R.id.btn_previous) {
            Toast.makeText(this, "Previous button is clicked", Toast.LENGTH_LONG).show();
            // Start previous step
            RecipeStepInstructionFragment newFragment = new RecipeStepInstructionFragment();
            newFragment.setStepIdx(index-1);
            newFragment.setRecipeStepDetail(mRecipeDetail);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_steps_container, newFragment)
                    .commit();
        } else {
            // Says error
        }
    }


    // Define the behavior for onStepSelected
    @Override
    public void onStepSelected(int position) {
        // Create a Toast that displays the position that was clicked
        Toast.makeText(this, "Position clicked = " + position, Toast.LENGTH_LONG).show();

        // Create a new RecipeStepInstructionFragment and replace the existing one
        RecipeStepInstructionFragment newFragment = new RecipeStepInstructionFragment();
        newFragment.setStepIdx(position);
        newFragment.setRecipeStepDetail(mRecipeDetail);

        FragmentManager fm = getSupportFragmentManager();

        if (mTwoPane) {
            newFragment.setTwoPane();
            fm.beginTransaction()
                    .replace(R.id.recipe_steps_linear_layout, newFragment)
                    .commit();
        } else {
            fm.beginTransaction()
                    .replace(R.id.recipe_steps_container, newFragment)
                    .commit();
        }
    }
}

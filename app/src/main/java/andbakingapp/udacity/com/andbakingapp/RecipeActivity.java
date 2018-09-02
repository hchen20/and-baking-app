package andbakingapp.udacity.com.andbakingapp;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

// This activity will display a specific recipe and
public class RecipeActivity extends AppCompatActivity
                implements RecipeStepInstructionFragment.OnButtonClickListener,
                           RecipeStepsFragment.OnStepClickListener  {
    private static final String TAG = RecipeActivity.class.getSimpleName();

    private static final String RECIPE_DETAIL = "detail";

    private String mRecipeDetail;
    private int mMaxStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        mRecipeDetail = getIntent().getStringExtra(RECIPE_DETAIL);
        Log.d(TAG, "onCreate: In RecipeActivity" + mRecipeDetail);
        // Retrieve the specific recipe string from intent
        RecipeStepsFragment stepsFragment = new RecipeStepsFragment();
        stepsFragment.setRecipeDetail(mRecipeDetail);

        // Add recipe steps fragment to its container using FragmentManager and a Transaction
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.recipe_steps_container, stepsFragment)
                .commit();
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

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.recipe_steps_container, newFragment)
                .commit();
    }
}

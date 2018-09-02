package andbakingapp.udacity.com.andbakingapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecipeStepsAdapter extends RecyclerView.Adapter<RecipeStepsAdapter.RecipeStepsViewHolder> {
    private static final String TAG = RecipeStepsAdapter.class.getSimpleName();

    private Context mContext;
    private String mStepsJson;
    private RecipeStepsFragment.OnStepClickListener mClickCallback;
    private ArrayList<String> mSteps;


    public RecipeStepsAdapter(Context context,
                              String steps,
                              RecipeStepsFragment.OnStepClickListener callback) {
        this.mContext = context;
        this.mStepsJson = steps;
        this.mClickCallback = callback;
        mSteps = new ArrayList<String>();
        getRecipeSteps();
    }

    private void getRecipeSteps() {
        try {
            JSONObject theRecipe = new JSONObject(mStepsJson);
            JSONArray theSteps = theRecipe.getJSONArray("steps");

            for (int i = 0; i < theSteps.length(); i++) {
                JSONObject row = theSteps.getJSONObject(i);
                String stepShortDescription = row.getString("shortDescription");
                Log.d(TAG, "getRecipeSteps: " + stepShortDescription);
                mSteps.add(stepShortDescription);
            }
        } catch (Exception e) {
            Log.d(TAG, "getRecipeSteps: " + e.getMessage());
        }
    }

    @NonNull
    @Override
    public RecipeStepsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate the recipe_step layout to a view
        View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.recipe_steps, viewGroup,false);

        return new RecipeStepsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeStepsViewHolder recipeStepsViewHolder, int i) {
        String currentStep = mSteps.get(i);
        recipeStepsViewHolder.stepNameView.setText(currentStep);
        Log.d(TAG, "onBindViewHolder: bindViewHolder" + currentStep);
    }

    @Override
    public int getItemCount() {
        if (mSteps.isEmpty())
            return 0;
        return mSteps.size();
    }

    class RecipeStepsViewHolder extends RecyclerView.ViewHolder {
        // Class variable for step name
        TextView stepNameView;

        public RecipeStepsViewHolder(@NonNull View itemView) {
            super(itemView);

            stepNameView = (TextView) itemView.findViewById(R.id.tv_step_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    mClickCallback.onStepSelected(position);
                    Log.d(TAG, "onClick: position = " + position);
                }
            });
        }
    }
}

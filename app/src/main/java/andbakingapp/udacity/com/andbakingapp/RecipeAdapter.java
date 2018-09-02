package andbakingapp.udacity.com.andbakingapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.spec.ECField;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>{
    private static final String TAG = RecipeAdapter.class.getSimpleName();

    private String mRecipeJson;
    private JSONArray mRecipeArray;

    public RecipeAdapter(String json) {
        mRecipeJson = json;

        try {
            mRecipeArray = new JSONArray(mRecipeJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recipe_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, attachParentImmediately);
        RecipeViewHolder viewHolder = new RecipeViewHolder(view, context);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder recipeViewHolder, int i) {
        try {
            JSONObject recipeJson = mRecipeArray.getJSONObject(i);
            String recipeName = recipeJson.getString("name");
            recipeViewHolder.bind(recipeName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        if (mRecipeArray == null) {
            return 0;
        }
        return mRecipeArray.length();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private static final String RECIPE_DETAIL = "detail";

        TextView mRecipeDescription;
        Context mContext;

        public RecipeViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            mRecipeDescription = (TextView) itemView.findViewById(R.id.tv_item_recipe);
            mContext = context;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    try {
                        JSONObject specificRecipe = mRecipeArray.getJSONObject(position);
                        Intent startRecipeIntent = new Intent(mContext, RecipeActivity.class);
                        String recipeDetail = specificRecipe.toString();
                        Log.d(TAG, "onClick: specific recipe detail" + recipeDetail);
                        startRecipeIntent.putExtra(RECIPE_DETAIL, recipeDetail);
                        mContext.startActivity(startRecipeIntent);
                    } catch(Exception e) {
                        Log.d(TAG, "onClick: " + e.getMessage());
                    }
                }
            });
        }

        public void bind(String recipeName) {
            mRecipeDescription.setText(recipeName);
        }
    }
}

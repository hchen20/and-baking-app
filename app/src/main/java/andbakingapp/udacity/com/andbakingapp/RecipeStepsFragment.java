package andbakingapp.udacity.com.andbakingapp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

// This fragment displays all of the recipe steps in one large RecyclerView
public class RecipeStepsFragment extends Fragment {
    private static final String TAG = RecipeStepsFragment.class.getSimpleName();

    private String mRecipeDetail;

    // Define a new interface OnStepClickListener that triggers a callback in the host activity
    OnStepClickListener mCallback;

    public void setRecipeDetail(String recipeDetail) {
        this.mRecipeDetail = recipeDetail;
    }

    // OnStepClickListener interface calls a method in the host activity named onStepSelected
    public interface OnStepClickListener {
        void onStepSelected(int position);
    }

    // Override onAttach to make sure the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnStepClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                        + " must implment OnStepClickListener");
        }
    }

    // Required empty public constructor
    public RecipeStepsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the root view
        final View rootView = inflater.inflate(R.layout.fragment_recipe_steps, container, false);

        // Get a reference to the RecylerView in the fragment_recipe_steps xml
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recipe_steps);

        // Create the adapter that takes in the context and the step string
        RecipeStepsAdapter adapter = new RecipeStepsAdapter(getContext(), mRecipeDetail, mCallback);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);


        // Set the adapter on the RecyclerView
        rv.setAdapter(adapter);

        // Inflate the layout for this fragment
        return rootView;
    }

}

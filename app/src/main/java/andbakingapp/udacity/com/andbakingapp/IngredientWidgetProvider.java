package andbakingapp.udacity.com.andbakingapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.exoplayer2.ExoPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Implementation of App Widget functionality.
 */
public class IngredientWidgetProvider extends AppWidgetProvider {
    private static final String TAG = IngredientWidgetProvider.class.getSimpleName();
    private static final String RECIPE_DETAIL = "detail";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredient_widget_provider);
        views.setTextViewText(R.id.ingredient_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        final String recipeDetail = intent.getStringExtra(RECIPE_DETAIL);
        Log.d(TAG, "onReceive: ");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ingredient_widget_provider);

        views.setTextViewText(R.id.ingredient_text, processIngredients(recipeDetail));
        // This time we dont have widgetId. Reaching our widget with that way.
        ComponentName appWidget = new ComponentName(context, IngredientWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidget, views);
    }

    private String processIngredients(String recipe) {
        String formatted_ingredients = null;

        try {
            JSONObject recipeJson = new JSONObject(recipe);
            formatted_ingredients = recipeJson.getString("name") + "\n";
            JSONArray ingredients = recipeJson.getJSONArray("ingredients");
            for (int i = 0; i < ingredients.length(); i++) {
                JSONObject row = ingredients.getJSONObject(i);
                String quantity = row.getString("quantity");
                String measure = row.getString("measure");
                String ingredient = row.getString("ingredient");

                formatted_ingredients = formatted_ingredients + "* " + quantity + " " +
                        measure + " of " + ingredient + "\n";
            }

        } catch (Exception e) {
            Log.d(TAG, "processIngredient: " + e.getMessage());
        }
        return  formatted_ingredients;
    }
}


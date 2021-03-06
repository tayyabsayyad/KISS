package fr.neamar.kiss.searcher;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import fr.neamar.kiss.KissApplication;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.forwarder.Favorites;
import fr.neamar.kiss.pojo.Pojo;

/**
 * Retrieve pojos from history
 */
public class HistorySearcher extends Searcher {
    private final SharedPreferences prefs;

    public HistorySearcher(MainActivity activity) {
        super(activity, "<history>");
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    int getMaxResultCount() {
        // Convert `"number-of-display-elements"` to double first before truncating to int to avoid
        // `java.lang.NumberFormatException` crashes for values larger than `Integer.MAX_VALUE`
        return (Double.valueOf(prefs.getString("number-of-display-elements", String.valueOf(DEFAULT_MAX_RESULTS)))).intValue();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Ask for records
        boolean smartHistory = !prefs.getString("history-mode", "recency").equals("recency");
        boolean excludeFavorites = prefs.getBoolean("exclude-favorites", false);

        MainActivity activity = activityWeakReference.get();
        if (activity == null)
            return null;

        // Gather favorites
        ArrayList<Pojo> favoritesPojo = new ArrayList<>(0);
        if (excludeFavorites) {
            favoritesPojo = KissApplication.getApplication(activity).getDataHandler().getFavorites(Favorites.TRY_TO_RETRIEVE);
        }

        List<Pojo> pojos = KissApplication.getApplication(activity).getDataHandler().getHistory(activity, getMaxResultCount(), smartHistory, favoritesPojo);

        int size = pojos.size();
        for(int i = 0; i < size; i += 1) {
            pojos.get(i).relevance = size - i;
        }

        this.addResult(pojos.toArray(new Pojo[0]));
        return null;
    }
}

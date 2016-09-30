package jibiki.fr.shishito.Tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.SearchActivity;
import jibiki.fr.shishito.Util.HTTPUtils;
import jibiki.fr.shishito.Util.XMLUtils;

import static jibiki.fr.shishito.Util.HTTPUtils.updateContribution;

public class UpdateContribution extends AsyncTask<String, Void, ListEntry> {

    private ContributionUpdatedListener cul;
    private Volume volume;

    @SuppressWarnings("unused")
    private static final String TAG = XMLUtils.class.getSimpleName();

    public UpdateContribution(ContributionUpdatedListener cul, Volume volume) {
        this.cul = cul;
        this.volume = volume;
    }

    @Override
    protected ListEntry doInBackground(String... params) {
        return updateContribution(params[0], params[1], params[2], volume);
    }

    @Override
    protected void onPostExecute(ListEntry entry) {
        cul.onContributionUpdated(entry);
    }

    public interface ContributionUpdatedListener {
        void onContributionUpdated(ListEntry entry);
    }
}
package jibiki.fr.shishito.Tasks;

import android.os.AsyncTask;

import java.io.InputStream;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.SearchActivity;
import jibiki.fr.shishito.Util.HTTPUtils;
import jibiki.fr.shishito.Util.XMLUtils;

public class UpdateContribution extends AsyncTask<String, Void, ListEntry> {

    ContributionUpdatedListener cul;
    Volume volume;

    public UpdateContribution(ContributionUpdatedListener cul, Volume volume) {
        this.cul = cul;
        this.volume = volume;
    }

    @Override
    protected ListEntry doInBackground(String... params) {
        String url = SearchActivity.SERVER_API_URL + "Cesselin/jpn/" + params[0] + "/" + params[1];
        InputStream is = HTTPUtils.doPut(url, params[2]);
        return XMLUtils.handleListEntryStream(is, volume);
    }

    @Override
    protected void onPostExecute(ListEntry entry) {
        cul.onContributionUpdated(entry);
    }

    public interface ContributionUpdatedListener {
        void onContributionUpdated(ListEntry entry);
    }
}
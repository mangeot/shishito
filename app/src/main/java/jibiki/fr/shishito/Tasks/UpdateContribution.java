package jibiki.fr.shishito.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
import jibiki.fr.shishito.SearchActivity;
import jibiki.fr.shishito.Util.HTTPUtils;
import jibiki.fr.shishito.Util.XMLUtils;

public class UpdateContribution extends AsyncTask<String, Void, ListEntry> {

    ContributionUpdatedListener cul;
    Volume volume;
    private static final String TAG = XMLUtils.class.getSimpleName();

    public UpdateContribution(ContributionUpdatedListener cul, Volume volume) {
        this.cul = cul;
        this.volume = volume;
    }

    @Override
    protected ListEntry doInBackground(String... params) {
        String param0 = params[0];
        String param1 = params[1];
        try {
            param0 = java.net.URLEncoder.encode(param0,"UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            Log.d(TAG, "Error:", e);
        }
        try {
            param1 = java.net.URLEncoder.encode(param1,"UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            Log.d(TAG, "Error:", e);
        }

        String url = SearchActivity.VOLUME_API_URL + param0 + "/" + param1;
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
/* Copyright (C) 2016 Thibaut Le Guilly et Mathieu Mangeot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package jibiki.fr.shishito.Tasks;

import android.os.AsyncTask;

import jibiki.fr.shishito.Models.ListEntry;
import jibiki.fr.shishito.Models.Volume;
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
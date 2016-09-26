package jibiki.fr.shishito;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by tibo on 28/06/16.
 * A class used to display a charging wheel while the search request to the server is pending.
 */
public class ShishitoProgressDialog extends DialogFragment {

    public ShishitoProgressDialog() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull final Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.load_message));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or inflate a view

        return dialog;
    }
}

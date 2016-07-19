package jibiki.fr.shishito;

import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by tibo on 28/06/16.
 */
public class ShishitoProgressDialog extends DialogFragment {

    public ShishitoProgressDialog() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setMessage("Chargement en cours...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.setStyle(STYLE_NO_TITLE, getTheme()); // You can use styles or inflate a view

        return dialog;
    }
}

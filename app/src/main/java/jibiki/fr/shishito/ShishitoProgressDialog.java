package jibiki.fr.shishito;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by tibo on 28/06/16.
 * A class used to display a charging wheel while the search request to the server is pending.
 */
public class ShishitoProgressDialog extends DialogFragment {

    public ShishitoProgressDialog() {
    }

    public static void display(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment newFragment = new ShishitoProgressDialog();
        newFragment.show(ft, "dialog");
    }

    public static void remove(FragmentManager fm) {
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null && prev instanceof ShishitoProgressDialog) {
            ShishitoProgressDialog pd = (ShishitoProgressDialog) prev;
            pd.dismiss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

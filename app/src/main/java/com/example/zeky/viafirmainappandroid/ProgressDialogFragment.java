package com.example.zeky.viafirmainappandroid;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by Zeky on 27/7/15.
 */
public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance(){
        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        return progressDialogFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialogo = new ProgressDialog(getActivity());
        dialogo.setMessage("Por favor espere...");
        dialogo.setTitle("Realizando firma");
        dialogo.setIndeterminate(true);


        return dialogo;
    }
}

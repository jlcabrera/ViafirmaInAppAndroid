package com.example.zeky.viafirmainappandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Zeky on 14/7/15.
 */
public class Selector extends DialogFragment implements TextView.OnEditorActionListener{

    private Spinner claseFirma;
    private Spinner tipoFirma;



    public interface SelectorListener{
        void onFinishEditDialog(String typeFromat);
    }

    public Selector(){

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View view = layoutInflater.inflate(R.layout.selector,null);

        claseFirma = (Spinner)view.findViewById(R.id.sClaseFirma);
        tipoFirma = (Spinner)view.findViewById(R.id.sTipoFirma);

        final ArrayAdapter<CharSequence> adapterClaseFirma = ArrayAdapter.createFromResource(getActivity(), R.array.clasesFirma, android.R.layout.simple_dropdown_item_1line);
        adapterClaseFirma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        claseFirma.setAdapter(adapterClaseFirma);

        claseFirma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String clase = (String) parent.getSelectedItem();
                ArrayAdapter<CharSequence> adapterTipoFirma;

                if (clase.equalsIgnoreCase("PAdES")) {

                    adapterTipoFirma = ArrayAdapter.createFromResource(getActivity(), R.array.firmaPDF, android.R.layout.simple_dropdown_item_1line);
                } else if (clase.equalsIgnoreCase("XAdES")) {
                    adapterTipoFirma = ArrayAdapter.createFromResource(getActivity(), R.array.firmaXML, android.R.layout.simple_dropdown_item_1line);
                } else {
                    adapterTipoFirma = ArrayAdapter.createFromResource(getActivity(), R.array.firmaCADES, android.R.layout.simple_dropdown_item_1line);
                }

                adapterTipoFirma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                tipoFirma.setAdapter(adapterTipoFirma);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Seleccione firma")
                .setView(view)
                .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectorListener activity = (SelectorListener)getActivity();
                        activity.onFinishEditDialog((String)tipoFirma.getSelectedItem());
                    }
                })
                .setNegativeButton("Cancelar", null);

        return alertDialogBuilder.create();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(EditorInfo.IME_ACTION_DONE == actionId){
            SelectorListener activity = (SelectorListener)getActivity();
            activity.onFinishEditDialog((String)tipoFirma.getSelectedItem());
        }

        return false;
    }


}

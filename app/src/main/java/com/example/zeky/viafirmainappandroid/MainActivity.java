package com.example.zeky.viafirmainappandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.viafirma.client.sign.TypeFile;
import org.viafirma.client.sign.TypeFormatSign;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import viafirma.mobile.api.ViafirmaAPILoginCallBack;
import viafirma.mobile.api.ViafirmaAndroidLib;
import viafirma.mobile.api.model.CertificateEntity;
import viafirma.mobile.vo.DocumentVO;


public class MainActivity extends Activity implements Selector.SelectorListener{

    private final Context CONTEXTO = this;
    private EditText editUrl;
    private EditText editKey;
    private EditText editPass;
    private Credenciales credenciales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editUrl = (EditText) findViewById(R.id.url);
        editKey = (EditText) findViewById(R.id.appKey);
        editPass = (EditText) findViewById(R.id.passKey);

        BufferedReader config = null;
        File fileConfig = new File(getFilesDir().getPath() + "/configuration/data.json");
        if(fileConfig.exists()) {
            try {
                config = new BufferedReader(new FileReader(fileConfig));
                Gson gson = new Gson();
                this.credenciales = gson.fromJson(config, Credenciales.class);
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", "Han ocurrido errores al leer el json");
            }finally{
                if(config != null){
                    try {
                        config.close();
                    } catch (IOException e) {
                        Log.e("Cerrar Stream", "Se ha producido un error al cerrar el stream");
                    }
                }
            }
        }else{
            this.credenciales = new Credenciales();
        }

        this.editUrl.setText(credenciales.getUrl());
        this.editKey.setText(credenciales.getKey());
        this.editPass.setText(credenciales.getPass());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void authenticate(View view) {
        String url = editUrl.getText().toString();
        String key = editKey.getText().toString();
        String pass = editPass.getText().toString();

        ViafirmaAndroidLib api = ViafirmaAndroidLib.initWithOptions(this, null, url, key, pass);


        if (api.isKeyChainSupported()) {
            CertificateEntity certificado = api.getCertificateKeyChainRef();
            api.login(certificado, null, new ViafirmaAPILoginCallBack() {
                @Override
                public void loginOk(CertificateEntity entity) {
                    AlertDialog dialogo = new AlertDialog.Builder(CONTEXTO).setTitle(R.string.autenticacionCorrecta)
                            .setMessage(entity.getCertificateFullName() + " - " + entity.getCertificateNationalId()).create();
                    dialogo.show();
                }

                @Override
                public void fail(String message) {
                    if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_CA_NOT_SUPPORTED)) {
                        Toast.makeText(CONTEXTO, R.string.CAnoSoportada, Toast.LENGTH_LONG).show();

                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_EXPIRED_CERTIFICATE)) {
                        Toast.makeText(CONTEXTO, R.string.certificadoExpirado, Toast.LENGTH_LONG).show();
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_VIAFIRMA_CONNECTION)) {
                        Toast.makeText(CONTEXTO, R.string.ErrorEnLaConexcion, Toast.LENGTH_LONG).show();
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_WITH_CERTIFICATE)) {
                        Toast.makeText(CONTEXTO, R.string.ErrorAlLeerCertificado, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CONTEXTO, R.string.passIncorrecta, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //----------------------------------------

    public void sign(String tipoDeFirma) {


        FragmentManager fm = getFragmentManager();
        final ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance();
        progressDialogFragment.show(fm,"progressDialog");
        final  String url = editUrl.getText().toString();
        String key = editKey.getText().toString();
        String pass = editPass.getText().toString();

        List<DocumentVO> listaDocumentos = new ArrayList<>();
        byte[] dataToSign;

        try {

            dataToSign = IOUtils.toByteArray(getResources().openRawResource(R.raw.viafirmasdkinappandroiddoc));

            DocumentVO documento = new DocumentVO("DocumentSigned.pdf", dataToSign, TypeFile.PDF);
            //documento.setTypeFormatSign(TypeFormatSign.valueOf(tipoDeFirma));

            documento.setTypeFormatSign(TypeFormatSign.DIGITALIZED_SIGN);

            listaDocumentos.add(documento);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CONTEXTO, R.string.hanOcurridoErrores, Toast.LENGTH_LONG).show();
        }

        ViafirmaAndroidLib api = ViafirmaAndroidLib.initWithOptions(this, null, url, key, pass);

        Map<String, String> policyParams = new HashMap<String, String>();

        if (api.isKeyChainSupported()) {
            CertificateEntity certificado = api.getCertificateKeyChainRef();
            api.sign(listaDocumentos, certificado, "12345", policyParams, new ViafirmaAPILoginCallBack() {
                @Override
                public void signOk(CertificateEntity cert, final String idSign) {
                    progressDialogFragment.dismiss();

                    final String idFirma = idSign;
                    Log.i("Viafirma", "Se ha realizado la firma con el siguiente id: " + idSign);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog dialog = new AlertDialog.Builder(CONTEXTO).setTitle(R.string.seHaFirmadoUnDocumento)
                                    .setMessage(R.string.idsign + idFirma)
                                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "/v/" + idSign + "?d"));
                                            startActivity(i);
                                        }
                                    })
                                    .create();
                            dialog.show();
                        }
                    });
                }

                @Override
                public void fail(String message) {
                    progressDialogFragment.dismiss();
                    final String mensaje = message;
                    Log.e("Viafirma", "Se ha producido un error: " + message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CONTEXTO, R.string.hanOcurridoErrores + mensaje, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    //-----------------------------------------------------

    public void selectTypeSignature(View vista) {
        FragmentManager fm = getFragmentManager();
        Selector selector = new Selector();
        selector.show(fm,"test");
    }

    //--------------------------------

    @Override
    public void onFinishEditDialog(String typeFromat) {
        sign(typeFromat);
    }


    @Override
    protected void onDestroy() {
        this.credenciales.setUrl(this.editUrl.getText().toString());
        this.credenciales.setKey(this.editKey.getText().toString());
        this.credenciales.setPass(this.editPass.getText().toString());

        Gson gson = new Gson();
        File config = new File(getFilesDir().getPath() + "/configuration");
        config.mkdir();
        BufferedWriter bw = null;
        try {
            String data = gson.toJson(credenciales);
            bw = new BufferedWriter(new FileWriter(new File(config, "data.json")));
            bw.write(data);
        } catch (IOException e) {
            Log.e("error", "Error al escribir en el fichero de configuración");
            e.printStackTrace();
        }finally {
            if(bw != null){
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.e("Error", "Error al cerrar el stream para guardar los datos de las credenciales");
                }
            }
        }
        super.onDestroy();
    }
}
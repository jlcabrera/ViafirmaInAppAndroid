package com.example.zeky.viafirmainappandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.viafirma.client.sign.TypeFile;
import org.viafirma.client.sign.TypeFormatSign;

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
    private String url;
    private String key;
    private String pass;
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public void autenticar(View view) {
        this.url = ((EditText) findViewById(R.id.url)).getText().toString();
        this.key = ((EditText) findViewById(R.id.appKey)).getText().toString();
        this.pass = ((EditText) findViewById(R.id.passKey)).getText().toString();

        ViafirmaAndroidLib api = ViafirmaAndroidLib.initWithOptions(this, null, url, key, pass);


        if (api.isKeyChainSupported()) {
            CertificateEntity certificado = api.getCertificateKeyChainRef();
            api.login(certificado, "julio", new ViafirmaAPILoginCallBack() {
                @Override
                public void loginOk(CertificateEntity entity) {
                    AlertDialog dialogo = new AlertDialog.Builder(CONTEXTO).setTitle("Autentición correcta")
                            .setMessage(entity.getCertificateFullName() + " - " + entity.getCertificateNationalId()).create();
                    dialogo.show();
                }

                @Override
                public void fail(String message) {
                    if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_CA_NOT_SUPPORTED)) {
                        Toast.makeText(CONTEXTO, "CA no soportada", Toast.LENGTH_LONG).show();
                        Log.e("Viafirma", "CA no soportada");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_EXPIRED_CERTIFICATE)) {
                        Toast.makeText(CONTEXTO, "Certificado Expirado", Toast.LENGTH_LONG).show();
                        Log.e("Viafirma", "Certificado Expirado");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_VIAFIRMA_CONNECTION)) {
                        Toast.makeText(CONTEXTO, "Error en la conexión", Toast.LENGTH_LONG).show();
                        Log.e("Viafirma", "Error en la conexión");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_WITH_CERTIFICATE)) {
                        Toast.makeText(CONTEXTO, "Error al leer el certificado", Toast.LENGTH_LONG).show();
                        Log.e("Viafirma", "Error al leer el certificado");
                    } else {
                        Toast.makeText(CONTEXTO, "La contraseña no es correcta", Toast.LENGTH_LONG).show();
                        Log.e("Viafirma", "La contraseña no es correcta");
                    }
                }
            });
        }
    }

    //----------------------------------------

    public void firmar(String tipoDeFirma) {

        progressBar = ProgressDialog.show(CONTEXTO, "Realizando Firma", "Por favor espere ...");

        this.url = ((EditText) findViewById(R.id.url)).getText().toString();
        this.key = ((EditText) findViewById(R.id.appKey)).getText().toString();
        this.pass = ((EditText) findViewById(R.id.passKey)).getText().toString();

        List<DocumentVO> listaDocumentos = new ArrayList<>();
        byte[] dataToSign;

        try {

            dataToSign = IOUtils.toByteArray(getResources().openRawResource(R.raw.viafirmasdkinappandroiddoc));

            DocumentVO documento = new DocumentVO("DocumentSigned", dataToSign, TypeFile.PDF);
            documento.setTypeFormatSign(TypeFormatSign.valueOf(tipoDeFirma));
            listaDocumentos.add(documento);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CONTEXTO, "Han ocurrido errores", Toast.LENGTH_LONG).show();
        }

        ViafirmaAndroidLib api = ViafirmaAndroidLib.initWithOptions(this, null, url, key, pass);

        Map<String, String> policyParams = new HashMap<String, String>();

        if (api.isKeyChainSupported()) {
            CertificateEntity certificado = api.getCertificateKeyChainRef();
            api.sign(listaDocumentos, certificado, "12345", policyParams, new ViafirmaAPILoginCallBack() {
                @Override
                public void signOk(CertificateEntity cert, final String idSign) {
                    progressBar.dismiss();
                    final String idFirma = idSign;
                    Log.i("Viafirma", "Se ha realizado la firma con el siguiente id: " + idSign);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog dialog = new AlertDialog.Builder(CONTEXTO).setTitle("SE HA FIRMADO UN DOCUMENTO")
                                    .setMessage("Id de firma: " + idFirma)
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
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
                    progressBar.dismiss();
                    final String mensaje = message;
                    Log.e("Viafirma", "Se ha producido un error: " + message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CONTEXTO, "Han ocurrido errores: " + mensaje, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }

    //-----------------------------------------------------

    public void seleccionarTipoFirma(View vista) {
        FragmentManager fm = getFragmentManager();
        Selector selector = new Selector();
        selector.show(fm,"test");
    }

    //--------------------------------

    @Override
    public void onFinishEditDialog(String typeFromat) {
        firmar(typeFromat);
    }
}
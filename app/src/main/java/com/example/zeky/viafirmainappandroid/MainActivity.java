package com.example.zeky.viafirmainappandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.viafirma.client.sign.TypeFile;
import org.viafirma.client.sign.TypeFormatSign;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import viafirma.mobile.api.ViafirmaAPILoginCallBack;
import viafirma.mobile.api.ViafirmaAndroidLib;
import viafirma.mobile.api.model.CertificateEntity;
import viafirma.mobile.vo.DocumentVO;


public class MainActivity extends Activity {

    private final Context CONTEXTO = this;
    private String url;
    private String key;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
                    Toast.makeText(CONTEXTO, "Autenticación correcta: " + entity.getCertificateFullName() + " - " + entity.getCertificateNationalId(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void fail(String message) {
                    if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_CA_NOT_SUPPORTED)) {
                        Log.e("Viafirma", "CA no soportada");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_EXPIRED_CERTIFICATE)) {
                        Log.e("Viafirma", "Certificado Expirado");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_VIAFIRMA_CONNECTION)) {
                        Log.e("Viafirma", "Error en la conexión");
                    } else if (message.equalsIgnoreCase(ViafirmaAndroidLib.ERROR_WITH_CERTIFICATE)) {
                        Log.e("Viafirma", "Error al leer el certificado");
                    } else {
                        Log.e("Viafirma", "La contraseña no es correcta");
                    }
                }
            });
        }
    }

    public void firmar(String tipoDeFirma) {

        this.url = ((EditText) findViewById(R.id.url)).getText().toString();
        this.key = ((EditText) findViewById(R.id.appKey)).getText().toString();
        this.pass = ((EditText) findViewById(R.id.passKey)).getText().toString();


        List<DocumentVO> listaDocumentos = new ArrayList<>();
        byte[] dataToSign;

        try {

            dataToSign = IOUtils.toByteArray(getResources().openRawResource(R.raw.viafirmasdkinappandroiddoc));

            DocumentVO documento = new DocumentVO("test", dataToSign, TypeFile.PDF);
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
                public void signOk(CertificateEntity cert, String idSign) {
                    final String idFirma = idSign;
                    Log.i("Viafirma", "Se ha rºelizado la firma con el siguiente id: " + idSign);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog dialog = new AlertDialog.Builder(CONTEXTO).setTitle("SE HA FIRMADO UN DOCUMENTO")
                                    .setMessage("Id de firma: " + idFirma)
                                    .setPositiveButton("Aceptar", null)
                                    .create();
                            dialog.show();
                            Toast.makeText(CONTEXTO, "firma realizada con exito", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void fail(String message) {
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

        LayoutInflater inflater = LayoutInflater.from(CONTEXTO);
        final View view = inflater.inflate(R.layout.selector, null);

        final Spinner claseFirma = (Spinner) view.findViewById(R.id.sClaseFirma);
        final Spinner tipoFirma = (Spinner) view.findViewById(R.id.sTipoFirma);

        final ArrayAdapter<CharSequence> adapterClaseFirma = ArrayAdapter.createFromResource(CONTEXTO, R.array.clasesFirma, android.R.layout.simple_dropdown_item_1line);
        adapterClaseFirma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        claseFirma.setAdapter(adapterClaseFirma);

        claseFirma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String clase = (String) parent.getSelectedItem();
                ArrayAdapter<CharSequence> adapterTipoFirma = null;

                if (clase.equalsIgnoreCase("PAdES")) {

                    adapterTipoFirma = ArrayAdapter.createFromResource(CONTEXTO, R.array.firmaPDF, android.R.layout.simple_dropdown_item_1line);
                } else if (clase.equalsIgnoreCase("XAdES")) {
                    adapterTipoFirma = ArrayAdapter.createFromResource(CONTEXTO, R.array.firmaXML, android.R.layout.simple_dropdown_item_1line);
                } else {
                    adapterTipoFirma = ArrayAdapter.createFromResource(CONTEXTO, R.array.firmaCADES, android.R.layout.simple_dropdown_item_1line);
                }

                adapterTipoFirma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                tipoFirma.setAdapter(adapterTipoFirma);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tipoFirma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CONTEXTO, (String) tipoFirma.getSelectedItem(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        AlertDialog dialogo = new AlertDialog.Builder(CONTEXTO)
                .setView(view)
                .setTitle("Seleccione firma")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firmar((String) tipoFirma.getSelectedItem());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialogo.show();
    }

}
package com.elisoft.serere.mi_perfil.recarga;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.elisoft.serere.Suceso;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.elisoft.serere.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Recargar extends AppCompatActivity implements View.OnClickListener {
    EditText et_telefono, et_monto;
    private final static int MY_PERMISSIONS_REQUEST_CALL_PHONE = 123;
    private TelephonyManager telephonyManager;

    Button bt_guardar,bt_recargas;

    int operador=0;
    String empresa="";

    RadioButton rb_tigo;
    RadioButton rb_viva;
    RadioButton rb_entel;


    RequestQueue queue=null;
    Suceso suceso=null;

    private ProgressDialog pDialog;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recargar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        et_telefono=findViewById(R.id.et_telefono);
        et_monto=findViewById(R.id.et_monto);
        bt_guardar=findViewById(R.id.bt_guardar);
        bt_recargas=findViewById(R.id.bt_recargas);

        rb_tigo=findViewById(R.id.rb_tigo);
        rb_viva=findViewById(R.id.rb_viva);
        rb_entel=findViewById(R.id.rb_entel);


        bt_guardar.setOnClickListener(this);
        bt_recargas.setOnClickListener(this);

        SharedPreferences perfil=getSharedPreferences("perfil",MODE_PRIVATE);

        if(perfil.getString("celular","").length()>7)
        {
            et_telefono.setText(perfil.getString("celular",""));
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_guardar:
                if(rb_tigo.isChecked())
                {
                    empresa="TIGO";
                }else if(rb_viva.isChecked()){
                    empresa="VIVA";
                }else if(rb_entel.isChecked()){
                    empresa="ENTEL";
                }

               servicio_volley_insertar();

                break;
            case R.id.bt_recargas:
                startActivity(new Intent(this,Lista_recargas.class));
                break;
        }
    }


    private void servicio_volley_insertar() {
        SharedPreferences perfil=getSharedPreferences("perfil",MODE_PRIVATE);

        try {
            String v_url= getString(R.string.servidor)+"frmRecarga.php?opcion=insertar";


            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_usuario", perfil.getString("id_usuario",""));
            jsonParam.put("telefono",  et_telefono.getText().toString().trim());
            jsonParam.put("monto", et_monto.getText().toString().trim());
            jsonParam.put("empresa", empresa);
            if (queue == null) {
                queue = Volley.newRequestQueue(this);
                Log.e("volley","Setting a new request queue");
            }


            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    v_url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {
                            pDialog.dismiss();//ocultamos proggress dialog

                            try {
                                suceso=new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));


                                if (suceso.getSuceso().equals("1")) {
                                    mensaje_error_final(suceso.getMensaje());
                                    }else{
                                    mensaje_error(suceso.getMensaje());
                                }



                                //...final de final....................

                            } catch (JSONException e) {
                                e.printStackTrace();
                                mensaje_error("Falla en tu conexión a Internet.");
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.dismiss();//ocultamos proggress dialog
                    mensaje_error("Falla en tu conexión a Internet.");
                }
            }
            ){
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> parametros= new HashMap<>();
                    parametros.put("content-type","application/json; charset=utf-8");
                    parametros.put("Authorization","apikey 849442df8f0536d66de700a73ebca-us17");
                    parametros.put("Accept", "application/json");

                    return  parametros;
                }
            };

            pDialog = new ProgressDialog(Recargar.this);
            pDialog.setMessage("Registrando en recargas .");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.show();

            myRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(myRequest);

        } catch (Exception e) {

        }


    }

    public void mensaje_error(String mensaje)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Importante");
        builder.setMessage(mensaje);
        builder.setPositiveButton("OK", null);
        builder.create();
        builder.show();
    }

    public void mensaje_error_final(String mensaje)
    {
            try {
            AlertDialog.Builder builder = new AlertDialog.Builder(Recargar.this);
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(mensaje);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    finish();
                }
            });
            builder.create();
            builder.show();
        }catch (Exception e)
        {
            Log.e("mensaje error final",e.toString());
        }
    }

}
package com.elisoft.serere.chat;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.elisoft.serere.R;
import com.elisoft.serere.SqLite.AdminSQLiteOpenHelper;
import com.elisoft.serere.Suceso;
import com.elisoft.serere.notificaciones.SharedPrefManager;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import okio.ByteString;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Chat_pedido extends AppCompatActivity {
    //AUDIO
    private static final String LOG_TAG = "AudioRecordTest";
    public static final int RC_RECORD_AUDIO = 1000;
    public static String sRecordedFileName;
    private MediaRecorder mRecorder;
    MediaPlayer mp;
    ImageView im_grabar;
    ImageView im_camara;
    boolean sw_grabacion=false;
    private MediaPlayer mPlayer;

    //MENSAJE

    LinearLayout.LayoutParams cero = new LinearLayout.LayoutParams(0, 0);
    LinearLayout.LayoutParams wrap_52  = new LinearLayout.LayoutParams(52, 52);


    private ListView listView;
    private MessageAdapter messageAdapter;
    private EditText et_mensaje;
    private ImageView fb_send;
    ImageView im_perfil;
    TextView tv_nombre;
    Suceso suceso;
    String  ruta_imagen="";
    private boolean side =true;
    SharedPreferences perfil;
    Integer id_conductor;
    String id_usuario,nombre;
    public static String uniqueId;

    public static final String mBroadcastStringAction = "com.elisoft.string";
    private IntentFilter mIntentFilter;
    private Boolean hasConnection = false;

    private Thread thread2;
    private boolean startTyping = false;
    private int time = 2;
    public static final String TAG  = "MainActivity";


//CAMARA Y GALERIA
    private String mPath;
    private   String CARPETA_RAIZ="";
    private   String RUTA_IMAGEN="";
    final int COD_SELECCIONA=10;
    final int COD_FOTO=20;
    String path2;
    AlertDialog alert2 = null;


//SOCKET IO
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://64.227.10.80:5000/");
        } catch (URISyntaxException e) {}
    }

    @SuppressLint("HandlerLeak")
    Handler handler2=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage: escritura detenida " + startTyping);
            if(time == 0){
                setTitle("SocketIO");
                Log.i(TAG, "handleMessage: typing stopped time is " + time);
                startTyping = false;
                time = 2;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uniqueId = SharedPrefManager.getInstance(this).mostrarToken();
        Log.i(TAG, "onCreate: " + uniqueId);

        fb_send = findViewById(R.id.bt_enviar);
        listView = findViewById(R.id.msgview);
        et_mensaje = findViewById(R.id.msg);
        im_perfil = findViewById(R.id.im_perfil);
        tv_nombre = findViewById(R.id.tv_nombre);




//audio
        im_grabar=(ImageView)findViewById(R.id.im_grabar);
        im_camara=(ImageView)findViewById(R.id.im_camara);
        sRecordedFileName = getCacheDir().getAbsolutePath() + "/"+new Date().getTime()+".3gp";

        im_grabar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(LOG_TAG, "onTouch: " + event.getAction());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    preparar_mensaje_canal();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    detener_grabacion();
                }
                return true;
            }
        });


        im_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(Chat_pedido.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    verificar_permiso_camara();
                }
                else if ((ActivityCompat.checkSelfPermission(Chat_pedido.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||(ActivityCompat.checkSelfPermission(Chat_pedido.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                    verificar_permiso_almacenamiento();
                } else {
                    ruta_imagen="";
                    path2="";
                    cargarImagen();
                }
            }
        });




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CMensaje audio=messageAdapter.getItem(i);
                if(audio.getTipo().equals("AUDIO"))
                {
                    Intent intent = new Intent(getApplicationContext(), Servicio_reproducir.class);
                    intent.putExtra("id_chat",audio.getId());
                    intent.putExtra("mensaje",audio.getMensaje());
                    startService(intent);
                }
            }
        });




        et_mensaje.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return enviar_mensaje();
                }
                return false;
            }
        });
        fb_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                enviar_mensaje();
            }
        });





        try{
            Bundle bundle=getIntent().getExtras();
            id_conductor=Integer.parseInt(bundle.getString("id_conductor",""));
            tv_nombre.setText(bundle.getString("titulo",""));

            getImage(String.valueOf(id_conductor));
        }catch (Exception e)
        {
            finish();
        }


        perfil=getSharedPreferences("perfil",MODE_PRIVATE);
        id_usuario=perfil.getString("id_usuario", "0");
        nombre=perfil.getString("nombre", "0");



        if(savedInstanceState != null){
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if(hasConnection){

        }else {
            mSocket.connect();
            // mSocket.on("connect user", onNewUser);
            mSocket.on("conexion"+id_usuario+":"+id_conductor, onNewUser);
            mSocket.on("chat message", onNewMessage);
            mSocket.on("chat"+id_usuario+":"+id_conductor,onNewMessage);
            mSocket.on("on typing", onTyping);

            JSONObject userId = new JSONObject();
            try {
                userId.put("titulo", nombre + " Connected");
                userId.put("id_usuario", id_usuario );
                userId.put("id_conductor", id_conductor );
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "onCreate: " + hasConnection);
        hasConnection = true;

        //Intent filter para recibir datos del servicio.
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);


        List<CMensaje> messageFormatList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.item_message, messageFormatList);
        listView.setAdapter(messageAdapter);

        wrap_52  = new LinearLayout.LayoutParams(im_grabar.getLayoutParams().width, im_grabar.getLayoutParams().height);
        fb_send.setLayoutParams(cero);
        onTypeButtonEnable();


        lista_de_chat(id_usuario,String.valueOf(id_conductor));

        //UBICACION DE LAS CARPETAS
        CARPETA_RAIZ=getString(R.string.app_name)+"/";
        RUTA_IMAGEN=CARPETA_RAIZ+"misFotos";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }



    public void onTypeButtonEnable(){
        et_mensaje.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                JSONObject onTyping = new JSONObject();
                try {
                    onTyping.put("typing", true);
                    onTyping.put("titulo", nombre);
                    onTyping.put("uniqueId", uniqueId);
                    mSocket.emit("on typing", onTyping);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (charSequence.toString().trim().length() > 0) {
                    fb_send.setLayoutParams(wrap_52);
                } else {
                    fb_send.setLayoutParams(cero);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    // Aqui se recepciona los mensajes mediante socket.io
    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: ");
                    Log.i(TAG, "run: " + args.length);
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String id;
                    try {
                        username = data.getString("titulo");
                        message = data.getString("message");
                        id = data.getString("uniqueId");

                        String id_chat="";
                        String id_cond="";
                        String id_usu="";
                        String titulo="";
                        String mensaje="";
                        String fecha="";
                        String hora="";
                        String tipo="";
                        int yo=0;

                        id_chat = data.getString("id_chat");
                        id_cond = data.getString("id_conductor");
                        id_usu = data.getString("id_usuario");
                        titulo = data.getString("titulo");
                        mensaje = data.getString("mensaje");
                        fecha = data.getString("fecha");
                        hora = data.getString("hora");
                        tipo = data.getString("tipo");


                        Log.i("MENSAJE", "Usuario: " + username +" mesnaje: "+ message+"  id:" + id);

                        if(id.equals(SharedPrefManager.getInstance(Chat_pedido.this).mostrarToken())==true) {
                            yo=1;
                        }else{
                            yo=0;
                        }


                        CMensaje  format = new CMensaje(false, mensaje, titulo, fecha, hora,1, Integer.parseInt(id_usu), Integer.parseInt(id_cond),yo,tipo,Integer.parseInt(id_chat));
                        messageAdapter.add(format);
                        Log.i(TAG, "run:5 ");


                            listView.smoothScrollToPosition(messageAdapter.getCount()-1);
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int length = args.length;

                    if(length == 0){
                        return;
                    }
                    //Aquí recibo un error extraño
                    //Here i'm getting weird error..................///////run :1 and run: 0
                    Log.i(TAG, "run: ");
                    Log.i(TAG, "run: " + args.length);
                    String titulo =args[0].toString();
                    try {
                        JSONObject object = new JSONObject(titulo);
                        titulo = object.getString("titulo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    CMensaje format = new CMensaje(false, "", titulo, "", "",1, 0, 0,1,"TEXTO",0);
                    messageAdapter.add(format);

                  //  listView.smoothScrollToPosition(0);
                    //listView.scrollTo(0, messageAdapter.getCount()-1);
                    Log.i(TAG, "run: " + titulo);
                }
            });
        }
    };


    Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, "run: " + args[0]);
                    try {
                        Boolean typingOrNot = data.getBoolean("typing");
                        String userName = data.getString("titulo") + " is Typing......";
                        String id = data.getString("uniqueId");

                        if(id.equals(uniqueId)){
                            typingOrNot = false;
                        }else {
                            setTitle(userName);
                        }

                        if(typingOrNot){

                            if(!startTyping){
                                startTyping = true;
                                thread2=new Thread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                while(time > 0) {
                                                    synchronized (this){
                                                        try {
                                                            wait(1000);
                                                            Log.i(TAG, "run: typing " + time);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        time--;
                                                    }
                                                    handler2.sendEmptyMessage(0);
                                                }

                                            }
                                        }
                                );
                                thread2.start();
                            }else {
                                time = 2;
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public void sendMessage(String id,
                            String id_conductor,
                            String id_usuario,
                            String titulo,
                            String mensaje,
                            String fecha,
                            String hora,
                            String tipo){
        Log.i(TAG, "sendMessage: ");

        if(TextUtils.isEmpty(mensaje)){
            Log.i(TAG, "sendMessage:2 ");
            return;
        }
        et_mensaje.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", mensaje);
            jsonObject.put("uniqueId", uniqueId);

            jsonObject.put("id_chat", id);
            jsonObject.put("id_conductor", id_conductor );
            jsonObject.put("id_usuario", id_usuario );
            jsonObject.put("titulo", titulo);
            jsonObject.put("mensaje", mensaje);
            jsonObject.put("fecha", fecha);
            jsonObject.put("hora", hora);
            jsonObject.put("tipo", tipo);
            jsonObject.put("yo", 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendMessage: 1"+ mSocket.emit("mensaje_conductor", jsonObject));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isFinishing()){
            Log.i(TAG, "onDestroy: ");

            JSONObject userId = new JSONObject();
            try {
                userId.put("titulo", nombre + " DisConnected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("chat message", onNewMessage);
            mSocket.off("chat"+id_usuario+":"+id_conductor, onNewMessage);
            mSocket.off("mensaje_conductor", onNewMessage);
            mSocket.off("connect user", onNewUser);
            mSocket.off("on typing", onTyping);
            messageAdapter.clear();
        }else {
            Log.i(TAG, "onDestroy: is rotating.....");
        }

    }

    private boolean enviar_mensaje() {
        if(et_mensaje.getText().toString().trim().length()>0) {
            servicio_enviar_pasajero(id_usuario,String.valueOf(id_conductor),nombre,et_mensaje.getText().toString(),"TEXTO");

        }
        et_mensaje.setText("");

        return true;
    }

    public static String now() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    public static String hora() {
        String DATE_FORMAT_NOW = "HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }




    private void getImage(String id)//
    {
        String  url=  getString(R.string.servidor_web)+"public/Imagen_Conductor/Perfil-"+id+".png";
        Picasso.with(this).load(url).into(target);
    }

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            Drawable dw = new BitmapDrawable(getResources(), bitmap);
            //se edita la imagen para ponerlo en circulo.

            if( bitmap==null)
            { dw = getResources().getDrawable(R.drawable.ic_logo_carrito);}

            imagen_circulo(dw,im_perfil);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };


    public void imagen_circulo(Drawable id_imagen, ImageView imagen) {
        Bitmap originalBitmap = ((BitmapDrawable) id_imagen).getBitmap();
        if (originalBitmap.getWidth() > originalBitmap.getHeight()) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getHeight(), originalBitmap.getHeight());
        } else if (originalBitmap.getWidth() < originalBitmap.getHeight()) {
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getWidth());
        }

//creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);

//asignamos el CornerRadius
        roundedDrawable.setCornerRadius(originalBitmap.getWidth());
        try {
            imagen.setImageDrawable(roundedDrawable);
        }catch (Exception e)
        {

        }
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

    }




    @AfterPermissionGranted(RC_RECORD_AUDIO)
    private void startRecording() {
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {

            sRecordedFileName=getCacheDir().getAbsolutePath() + "/"+new Date().getTime()+".3gp";
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(sRecordedFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            mRecorder.start();
        } else {
            EasyPermissions.requestPermissions(this, "Debes otorgar permisos de acceso al Microfono para enviar audio", RC_RECORD_AUDIO, perms);
        }
    }

    private void stopRecording() {
         try {
             mRecorder.stop();
             mRecorder.release();
         }catch (Exception e)
         {
         }

        mRecorder = null;
    }

    private void setRecordIcon(boolean record) {
        if (record) {
            im_grabar.setImageDrawable(
                    VectorDrawableCompat.create(getResources(), R.drawable.ic_enviando_audio_conexion, getTheme()));
        } else {
            im_grabar.setImageDrawable(
                    VectorDrawableCompat.create(getResources(), R.drawable.ic_error_conexion, getTheme()));
        }
    }


    private void preparar_mensaje_canal()
    {
        im_grabar.setImageDrawable(
                VectorDrawableCompat.create(getResources(), R.drawable.ic_preparando_conexion, getTheme()));
        SharedPreferences prefe = getSharedPreferences("perfil_conductor", MODE_PRIVATE);
        inicia_grabacion();
    }

    private void inicia_grabacion()
    {
        mp= MediaPlayer.create(this, R.raw.sonido_conexion);
        mp.start();
        sw_grabacion=true;
        setRecordIcon(true);
        startRecording();
    }

    private void detener_grabacion()
    {
        if(sw_grabacion==true) {
            sw_grabacion=false;

            setRecordIcon(false);
            stopRecording();
            send();

        }
    }



    public void send() {
        sendAudio();
    }

    public void sendAudio() {
        FileChannel in = null;

        try {
            File f = new File(sRecordedFileName);
            in = new FileInputStream(f).getChannel();

            //mSocket.send(START);

            sendAudioBytes(in);

            //mSocket.send(END);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendAudioBytes(FileChannel in) throws IOException {
        ByteBuffer buff = ByteBuffer.allocateDirect(32);

        String audio="";

        while (in.read(buff) > 0) {

            buff.flip();
            String bytes = ByteString.of(buff).toString();
            //mSocket.send(bytes);

            try {
                String hexValue = bytes.substring(bytes.indexOf("hex=") + 4, bytes.length() - 1);

                audio+=hexValue;
                /*
                servicio_enviar_audio(String.valueOf(id_chat),
                        hexValue,
                        String.valueOf(numero),
                        getString(R.string.servidor) + "frmAudio.php?opcion=enviar_audio");
                */

                ByteString d = ByteString.decodeHex(hexValue);
                byte[] bytes1 = d.toByteArray();


            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            buff.clear();
        }

        servicio_enviar_pasajero_audio(id_usuario,String.valueOf(id_conductor),nombre,audio,"AUDIO");

    }


    private void servicio_enviar_pasajero(final String id_usuario,
                                          final String id_conductor,
                                          final String titulo,
                                          final String mensaje,
                                          final String tipo_mensaje) {


        try {

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_usuario", id_usuario);
            jsonParam.put("id_conductor", id_conductor);
            jsonParam.put("titulo", titulo);
            jsonParam.put("mensaje", mensaje);

            String url=getString(R.string.servidor) + "frmChat.php?opcion=enviar_pasajero";
            RequestQueue queue = Volley.newRequestQueue(this);


            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {
                            String id,fecha="",hora="";
                            try {
                                suceso= new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

                                if (suceso.getSuceso().equals("1")) {
                                    id=respuestaJSON.getString("id");
                                    fecha=respuestaJSON.getString("fecha");
                                    hora=respuestaJSON.getString("hora");


                                    ///////////////////// 8 final //////////////////
                                    Intent serviceIntent = new Intent(Chat_pedido.this, Servicio_guardar_mensaje_enviado.class);
                                    serviceIntent.putExtra("id_chat", id);
                                    serviceIntent.putExtra("id_conductor",id_conductor);
                                    serviceIntent.putExtra("id_usuario", id_usuario);
                                    serviceIntent.putExtra("titulo", titulo);
                                    serviceIntent.putExtra("mensaje",mensaje);
                                    serviceIntent.putExtra("fecha", fecha);
                                    serviceIntent.putExtra("hora", hora);
                                    serviceIntent.putExtra("tipo", tipo_mensaje);
                                    startService(serviceIntent);

                                    sendMessage(id,id_conductor,id_usuario,titulo,mensaje,fecha,hora,tipo_mensaje);


                                }
                                else
                                {
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
            ){
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> parametros= new HashMap<>();
                    parametros.put("content-type","application/json; charset=utf-8");
                    parametros.put("Authorization","apikey 849442df8f0536d66de700a73ebca-us17");
                    parametros.put("Accept", "application/json");

                    return  parametros;
                }
            };


            // TIEMPO DE ESPERA
            myRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(myRequest);


        } catch (Exception e) {

        }
    }

    private void servicio_enviar_pasajero_audio(final String id_usuario,
                                          final String id_conductor,
                                          final String titulo,
                                          final String mensaje,
                                          final String tipo) {


        try {

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_usuario", id_usuario);
            jsonParam.put("id_conductor", id_conductor);
            jsonParam.put("titulo", titulo);
            jsonParam.put("mensaje", mensaje);
            jsonParam.put("tipo", tipo);

            String url=getString(R.string.servidor) + "frmChat.php?opcion=enviar_pasajero_audio";
            RequestQueue queue = Volley.newRequestQueue(this);


            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {
                            String id,fecha="",hora="";
                            try {
                                suceso= new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

                                if (suceso.getSuceso().equals("1")) {
                                    id=respuestaJSON.getString("id");
                                    fecha=respuestaJSON.getString("fecha");
                                    hora=respuestaJSON.getString("hora");


                                    ///////////////////// 8 final //////////////////
                                    Intent serviceIntent = new Intent(Chat_pedido.this, Servicio_guardar_mensaje_enviado.class);
                                    serviceIntent.putExtra("id_chat", id);
                                    serviceIntent.putExtra("id_conductor",id_conductor);
                                    serviceIntent.putExtra("id_usuario", id_usuario);
                                    serviceIntent.putExtra("titulo", titulo);
                                    serviceIntent.putExtra("mensaje",mensaje);
                                    serviceIntent.putExtra("fecha", fecha);
                                    serviceIntent.putExtra("hora", hora);
                                    serviceIntent.putExtra("tipo", tipo);
                                    startService(serviceIntent);

                                    sendMessage(id,id_conductor,id_usuario,titulo,mensaje,fecha,hora,tipo);


                                }
                                else
                                {
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
            ){
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> parametros= new HashMap<>();
                    parametros.put("content-type","application/json; charset=utf-8");
                    parametros.put("Authorization","apikey 849442df8f0536d66de700a73ebca-us17");
                    parametros.put("Accept", "application/json");

                    return  parametros;
                }
            };


            // TIEMPO DE ESPERA
            myRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(myRequest);


        } catch (Exception e) {

        }
    }

    public  void lista_de_chat(String id_usuario, String id_conductor)
    {

        try {

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, getString(R.string.nombre_sql), null, Integer.parseInt(getString(R.string.version_sql)));

            SQLiteDatabase bd = admin.getWritableDatabase();
            Cursor fila = bd.rawQuery("select id,id_conductor,id_usuario,titulo,mensaje,fecha,hora,estado,yo,tipo from chat where id_usuario=" + id_usuario + " and id_conductor=" + id_conductor + " order by id asc ", null);

            if (fila.moveToFirst()) {  //si ha devuelto 1 fila, vamos al primero (que es el unico)

                do {

                    String id =  fila.getString(0);
                    String id_cond = fila.getString(1);
                    String id_usu = fila.getString(2);
                    String titulo = fila.getString(3);
                    String mensaje = fila.getString(4);
                    String fecha = fila.getString(5);
                    String hora = fila.getString(6);
                    String estado = fila.getString(7);
                    String yo = fila.getString(8);
                    String tipo = fila.getString(9);

                    boolean sw_left = false;
                    if (yo.equals("0")) {
                        sw_left = true;

                    }else{
                        yo="1";
                    }


                    CMensaje format = new CMensaje(sw_left, mensaje, titulo, fecha, hora, Integer.parseInt(estado), Integer.parseInt(id_usu), Integer.parseInt(id_cond),Integer.parseInt(yo),tipo,Integer.parseInt(id));
                    messageAdapter.add(format);

                    listView.smoothScrollToPosition(messageAdapter.getCount()-1);


                } while (fila.moveToNext());

            } else {

            }

            bd.close();

        }catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    //obtener imagen de camara y galeria

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            //ubicacion de la imagen

            switch (requestCode){

                case COD_SELECCIONA:
                    Uri uri=data.getData();
                        path2=uri.getPath();
                        savebitmap_seleccionado(uri);
                    break;

                case COD_FOTO:
                    MediaScannerConnection.scanFile(this, new String[]{path2}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Ruta de almacenamiento","Path: "+path2);

                                        path2=uri.getPath();
                                        savebitmap_seleccionado(uri);
                                }
                            });





                    break;


            }


        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100){
            if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
            }else{
                showExplanation();
            }
        } else{
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        try {
            builder.show();
        }catch (Exception e){
            Log.e("Permiso Explanation","sin permiso en configuracion");
        }
    }


    public void  cargarImagen()
    {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.seleccionar_opcion, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);


        final LinearLayout ll_camara= promptView.findViewById(R.id.ll_camara);
        final LinearLayout ll_galeria= promptView.findViewById(R.id.ll_galeria);


        ll_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tomarFotografia();
                alert2.cancel();
            }
        });

        ll_galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrir_galeria();
                alert2.cancel();
            }
        });

        // create an alert dialog
        alert2 = alertDialogBuilder.create();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.x=0;
        lp.y=0;


        alert2.getWindow().setAttributes(lp);

        alert2.getWindow().getAttributes().gravity= Gravity.BOTTOM;
        alert2.getWindow().getAttributes().horizontalMargin=0.01F;
        alert2.getWindow().getAttributes().verticalMargin=0.01F;
        alert2.show();


    }



    //agregar camara  y galeria



    private void tomarFotografia() {
        File fileImagen=new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        boolean isCreada=fileImagen.exists();
        String nombreImagen="";
        if(isCreada==false){
            isCreada=fileImagen.mkdirs();
        }

        if(isCreada==true){
            nombreImagen=(System.currentTimeMillis()/1000)+".jpg";
        }


        path2=Environment.getExternalStorageDirectory()+
                File.separator+RUTA_IMAGEN+File.separator+nombreImagen;

        File imagen=new File(path2);

        Intent intent=null;
        intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ////
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
        {
            String authorities=getApplicationContext().getPackageName()+".provider";
            Uri imageUri= FileProvider.getUriForFile(this,authorities,imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }else
        {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }
        startActivityForResult(intent,COD_FOTO);

        ////
    }

    public void abrir_galeria()
    {
        File fileImagen=new File(Environment.getExternalStorageDirectory(),RUTA_IMAGEN);
        boolean isCreada=fileImagen.exists();
        String nombreImagen="";
        if(isCreada==false){
            isCreada=fileImagen.mkdirs();
        }

        if(isCreada==true){
            nombreImagen=(System.currentTimeMillis()/1000)+".jpg";
        }


        path2=Environment.getExternalStorageDirectory()+
                File.separator+RUTA_IMAGEN+File.separator+nombreImagen;

        File imagen=new File(path2);

        Intent pictureActionIntent = null;
        pictureActionIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pictureActionIntent.setType("image/*");
        startActivityForResult(
                pictureActionIntent,
                COD_SELECCIONA);
        //startActivityForResult(intent.createChooser(intent,"Seleccione la Aplicación"),COD_SELECCIONA);
    }




    public boolean savebitmap() {

        boolean result = false;
        // nombre = fecha + valoresGenerales.isTipoArchivoImagenExtencionPng;

        String filename = path2;

        OutputStream outStream = null;

        File file = new File(filename );


        int m_inSampleSize = 0;
        int m_compress = 80;

        // 100 dejarlo original
        //  0  comprimir al maximo .. no se recomeinda.


        try {

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inPurgeable = true;
            bmOptions.inSampleSize = m_inSampleSize;
            //Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
            Bitmap bitmap = BitmapFactory.decodeFile(filename, bmOptions);
            // make a new bitmap from your file
            //Bitmap bitmap = BitmapFactory.decodeFile(file.getName());

            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, m_compress, outStream);
            outStream.flush();
            outStream.close();
            result = true;



        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        Log.e("file", "" + file);
        return result;

    }


    private void subir_imagen_servidor( ) {

                serverUpdate(getString(R.string.servidor) + "frmCorporativo.php?opcion=insertar_imagen_chat");
    }

    class ServerUpdate extends AsyncTask<String,String,String> {

        ProgressDialog pDialog;
        String resultado="";
        @Override
        protected String doInBackground(String... arg0) {
            resultado=uploadFoto(arg0[0]  );



            if(suceso.getSuceso().equals("1"))
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {

                        // TODO Auto-generated method stub
                        Toast.makeText(Chat_pedido.this, suceso.getMensaje(),Toast.LENGTH_LONG).show();
                        //ENVIAR MENSAJE DE IMAGEN
                        servicio_enviar_pasajero_audio(id_usuario,String.valueOf(id_conductor),nombre,ruta_imagen,"IMAGEN");
                    }
                });
            else
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        AlertDialog.Builder builder = new AlertDialog.Builder(Chat_pedido.this);
                        builder.setTitle("Importante");
                        builder.setMessage(suceso.getMensaje());
                        builder.setPositiveButton("OK", null);
                        builder.create();
                        builder.show();
                    }
                });
            return null;
        }
        protected void onPreExecute() {
            super.onPreExecute();
            try{
            pDialog = new ProgressDialog(Chat_pedido.this);
            pDialog.setMessage("Subiendo la imagen, espere..." );
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pDialog.dismiss();


            if(resultado.equals("2"))
            {
                File newFile = new File(path2);
                Bitmap bitmap =  BitmapFactory.decodeFile(newFile.getAbsolutePath());
                //Convertir Bitmap a Drawable.
                Drawable dw = new BitmapDrawable(getResources(), bitmap);
                //se edita la imagen para ponerlo en circulo.

                if( bitmap==null)
                { dw = getResources().getDrawable(R.drawable.ic_perfil_negro);}

                imagen_circulo(dw,null);
                guardar_en_memoria_BITMAP(bitmap);



            }else if(resultado.equals("500"))
            {
                mensaje(suceso.getMensaje());
            }else
            {
                mensaje("Falla en tu conexion a internet.");
            }
        }

    }



    private void serverUpdate(String url){
        File file = new File(path2);
        if (file.exists()){
            new  ServerUpdate().execute(url);
        }
        else
        {
            Log.e("Imagen","No se pudo localizar la imagen");
        }
    }


    ///PRUEBA DE INSERTAR IMAGEN

    private String uploadFoto(String url ){
        String devuelve="";
        File file = new File(path2);

        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost(url);

        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        ContentBody foto = new FileBody(file);
        mpEntity.addPart("imagen", foto);


        try {
            mpEntity.addPart("id_usuario", new StringBody(id_usuario));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httppost.setEntity(mpEntity);


        String resultado;
        HttpResponse response;
        try {
            response=httpclient.execute(httppost);
            HttpEntity entity =response.getEntity();

            InputStream inputStream= entity.getContent();
            resultado=convertStreamToString(inputStream);

            JSONObject respuestaJSON = new JSONObject(resultado);//Creo un JSONObject a partir del
            suceso=new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

            if (suceso.getSuceso().equals("1")) {
                devuelve = "1";
                ruta_imagen=respuestaJSON.getString("ruta");

            }else
            {
                devuelve = "500";
            }

            httpclient.getConnectionManager().shutdown();


            return devuelve;
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devuelve;
    }

    public String convertStreamToString(InputStream is) throws IOException{
        if(is!=null)
        {
            StringBuilder sb= new StringBuilder();
            String line;
            try{
                BufferedReader reader=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                while ((line=reader.readLine())!=null){
                    sb.append(line).append("\n");
                }

            }finally {
                is.close();
            }
            return  sb.toString();
        }else {
            return "";
        }
    }


    private void guardar_en_memoria_BITMAP(Bitmap bitmapImage)
    {

        if(bitmapImage!=null) {

            try {
                File file=null;
                FileOutputStream fos = null;
                String APP_DIRECTORY = getString(R.string.app_name)+"/";//nombre de directorio
                String MEDIA_DIRECTORY = APP_DIRECTORY + "Imagen";//nombre de la carpeta
                file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
                File mypath = new File(file, id_usuario+ "_perfil.jpg");//nombre del archivo imagen

                boolean isDirectoryCreated = file.exists();//pregunto si esxiste el directorio creado
                if (!isDirectoryCreated)
                    isDirectoryCreated = file.mkdirs();

                if (isDirectoryCreated) {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    //permiso de camra y galaeria

    public void verificar_permiso_camara()
    {
        final String[] CAMERA_PERMISSIONS = { Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_NETWORK_STATE };

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            //YA LO CANCELE Y VOUELVO A PERDIR EL PERMISO.

            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Atención!");
            dialogo1.setMessage("Debes otorgar permisos de acceso a CAMARA.");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Solicitar permiso", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();
                    ActivityCompat.requestPermissions(Chat_pedido.this,
                            CAMERA_PERMISSIONS,
                            1);

                }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();

                }
            });
            dialogo1.show();
        } else {
            ActivityCompat.requestPermissions(Chat_pedido.this,
                    CAMERA_PERMISSIONS,
                    1);
        }
    }

    public void verificar_permiso_almacenamiento()
    {
        final String[] CAMERA_PERMISSIONS = { Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE };

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //YA LO CANCELE Y VOUELVO A PERDIR EL PERMISO.

            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Atención!");
            dialogo1.setMessage("Debes otorgar permisos de acceso a ALMACENAMIENTO.");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Solicitar permiso", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();
                    ActivityCompat.requestPermissions(Chat_pedido.this,
                            CAMERA_PERMISSIONS,
                            1);

                }
            });
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    dialogo1.cancel();

                }
            });
            dialogo1.show();
        } else {
            ActivityCompat.requestPermissions(Chat_pedido.this,
                    CAMERA_PERMISSIONS,
                    1);
        }
    }


    //mensajes

    public void mensaje(String mensaje)
    {
        Toast toast = Toast.makeText(this,mensaje, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
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

//guardar en memoria
public void savebitmap_seleccionado(Uri uri) {




    int m_inSampleSize = 0;
    int m_compress = 80;

    // 100 dejarlo original
    //  0  comprimir al maximo .. no se recomeinda.


    try {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPurgeable = true;
        bmOptions.inSampleSize = m_inSampleSize;
        //Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);

        guardar_imagen(bitmap);

    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(this,"Vuelve a intentar",Toast.LENGTH_LONG).show();
    }



}

//guardar imagen en memoria
private void guardar_imagen(Bitmap bitmapImage)
{

    if(bitmapImage!=null) {

        try {
            File file=null;
            FileOutputStream fos = null;
            String APP_DIRECTORY = getString(R.string.app_name)+"/";//nombre de directorio
            String MEDIA_DIRECTORY = APP_DIRECTORY + "imagen";//nombre de la carpeta
            file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
            File mypath = new File(file, (System.currentTimeMillis()/1000)+"_mensaje.jpg");//nombre del archivo imagen
            path2=mypath.getPath();

            boolean isDirectoryCreated = file.exists();//pregunto si esxiste el directorio creado
            if (!isDirectoryCreated)
                isDirectoryCreated = file.mkdirs();

            if (isDirectoryCreated) {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            }



         //cargar la imagen al servidor
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    subir_imagen_servidor();
                }
            });




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

}

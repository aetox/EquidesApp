package com.example.equides;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import android.content.ActivityNotFoundException;




public class InterfaceActivity extends AppCompatActivity {

    private TextView welcomeMailTextView;

    private String email;

    private Button PDFBtn;
    private DatabaseManager databaseManager;

    private String id_detenteur;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        welcomeMailTextView = findViewById(R.id.welcomeMailTextView);
        PDFBtn = findViewById(R.id.PDFBtn);

        id_detenteur = getIntent().getStringExtra("idDetenteur");

        email = getIntent().getStringExtra("mail");

        welcomeMailTextView.setText(email);




        PDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://equides.eu/API_Equides/getPDF.php";
                downloadPdf(url);
            }
        });

    }

    private void downloadPdf(String url) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("idDetenteur", id_detenteur);
        String urlWithUserId = urlBuilder.build().toString();

                okhttp3.Request request = new okhttp3.Request.Builder()
                .url(urlWithUserId)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    savePdf(response.body().bytes());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(InterfaceActivity.this, "Erreur de téléchargement", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }



    private void savePdf(byte[] bytes) {

        // Créer un objet Date avec la date du jour
                Date now = new Date();

        // Créer un objet SimpleDateFormat pour formater la date
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        // Formater la date en une chaîne de caractères
                String dateStr = format.format(now);

        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Carnet_Transport_du_"+ dateStr +".pdf");
        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(bytes);
            fos.close();

            // Créez une Uri sécurisée à partir du fichier PDF
            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.equides.fileprovider", pdfFile);

            // Démarrer une intent pour afficher le fichier PDF
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(pdfUri, "application/pdf");
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(pdfIntent);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(InterfaceActivity.this, "PDF Téléchargé", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void onApiResponse(JSONObject response){
        Boolean success = null;
        String error ="";
        String idLogin = "";

        try {
            success = response.getBoolean("success");

            if (success){

                idLogin = response.getString("id_login"); // Recupere l'id_login de la personne connectée


            }else {
                error = response.getString("error");
            }

        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

        }

    }

    public void displayUserInfo() {

        // Envoie la requête POST
        String url = "http://10.0.2.2/API_Equides/connectUser.php";

        Map<String, String> params = new HashMap<>();
        params.put("id_login", id_detenteur);
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, url, parameters, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onApiResponse(response);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Une erreur inconnue s'est produite", Toast.LENGTH_LONG).show();
                }
            }
        });

        databaseManager.queue.add(jsonObjectRequest);

    }
}

package com.example.equides;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class InterfaceActivity extends AppCompatActivity {

    private TextView welcomeMailTextView;
    private String email;
    private Button PDFBtn;
    private DatabaseManager databaseManager;
    private String id_detenteur;

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Ajoutez cet appel à la méthode super
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // La permission a été accordée, vous pouvez maintenant télécharger et sauvegarder le PDF.
            } else {
                Toast.makeText(this, "Permission refusée. Vous ne pourrez pas télécharger le PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);


        welcomeMailTextView = findViewById(R.id.welcomeMailTextView);
        PDFBtn = findViewById(R.id.PDFBtn);

        id_detenteur = getIntent().getStringExtra("idDetenteur");
        email = getIntent().getStringExtra("mail");
        welcomeMailTextView.setText(email);

        PDFBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://equides.eu/API_Equides/getPDF.php";
                requestStoragePermission();
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
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String dateStr = format.format(now);

        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Carnet_Transport_du_" + dateStr + ".pdf");
        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(bytes);
            fos.close();

            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.equides.fileprovider", pdfFile);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(InterfaceActivity.this, "PDF Téléchargé", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onApiResponse(JSONObject response) {
        Boolean success = null;
        String error = "";
        String idLogin = "";
        try {
            success = response.getBoolean("success");

            if (success) {
                idLogin = response.getString("id_login"); // Récupère l'id_login de la personne connectée
            } else {
                error = response.getString("error");
            }

        } catch (JSONException e) {
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

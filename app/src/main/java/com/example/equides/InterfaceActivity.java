package com.example.equides;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InterfaceActivity extends AppCompatActivity {

    private TextView welcomeMailTextView;
    private TextView idLoginTextview;
    private String email;
    private DatabaseManager databaseManager;

    private String id_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        welcomeMailTextView = findViewById(R.id.welcomeMailTextView);
        idLoginTextview = findViewById(R.id.idLoginTextview);

        id_login = getIntent().getStringExtra("idLogin");

        email = getIntent().getStringExtra("mail");

        welcomeMailTextView.setText(email);
        idLoginTextview.setText("ID_login :" + id_login);

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
        params.put("id_login", id_login);
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                onApiResponse(response);
            }
        }, new Response.ErrorListener() {
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
}
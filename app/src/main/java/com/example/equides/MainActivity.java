package com.example.equides;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    private TextView errorConnectAccountTextView;
    private EditText mailEditText;
    private EditText passwordEditText;
    private Button connectBtn;
    private String mail;
    private String password;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        errorConnectAccountTextView = findViewById(R.id.errorConnectAccountTextView);
        mailEditText = findViewById(R.id.mailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        connectBtn = findViewById(R.id.connectBtn);
        databaseManager = new DatabaseManager(getApplicationContext());

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mail = mailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                connectUser();
            }
        });
    }

    public void onApiResponse(JSONObject response){
        Boolean success;
        String error ="";
        String idDetenteur = "";

        try {
            success = response.getBoolean("success");

            if (success){
                idDetenteur = response.getString("id_detenteur");
                Log.d("DEBUG", "idDetenteur : " + idDetenteur);

                Intent interfaceActivity = new Intent(getApplicationContext(), com.example.equides.InterfaceActivity.class);
                interfaceActivity.putExtra("mail",mail);
                interfaceActivity.putExtra("idDetenteur",idDetenteur);

                startActivity(interfaceActivity);
                finish();
            } else {
                error = response.getString("error");
                Log.d("DEBUG", "error : " + error);
                errorConnectAccountTextView.setVisibility(View.VISIBLE);
                errorConnectAccountTextView.setText(error);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void connectUser() {
        mail = mailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "https://equides.eu/API_Equides/connectUser.php";

        Map<String, String> params = new HashMap<>();
        params.put("mail", mail);
        params.put("password", password);
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

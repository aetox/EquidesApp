package com.example.equides;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
                mail = mailEditText.getText().toString(); // Recupere la valeur du input mail.
                password = passwordEditText.getText().toString();

                connectUser();
            }
        });
    }

    public void connectUser() {
        mail = mailEditText.getText().toString();
        password = passwordEditText.getText().toString();

        if (mail.isEmpty() || password.isEmpty()) {
            // Affiche un message d'erreur si les champs sont vides
            Toast.makeText(getApplicationContext(), "Veuillez remplir tous les champs", Toast.LENGTH_LONG).show();
            return;
        }

        // Envoie la requête POST
        String url = "http://10.0.2.2/API_Equides/action/connectUser.php";

        Map<String, String> params = new HashMap<>();
        params.put("mail", mail);
        params.put("password", password);
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "OPERATION SUCCESSFULL", Toast.LENGTH_LONG).show();
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
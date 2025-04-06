package com.example.projetws;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import beans.Etudiant;

public class addEtudiant extends AppCompatActivity implements View.OnClickListener {

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private ImageView studentImage;
    private Button btnSelectImage;
    private Bitmap selectedImageBitmap;
    private String selectedImageUrl = "";

    RequestQueue requestQueue;
    String insertUrl = "http://10.0.2.2/projet/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);

        // Initialisation des vues existantes
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        add = findViewById(R.id.add);
        m = findViewById(R.id.m);
        f = findViewById(R.id.f);
        studentImage = findViewById(R.id.student_image);
        btnSelectImage = findViewById(R.id.btn_select_image);

        // Ajout du bouton Liste
        Button btnList = findViewById(R.id.btn_list);

        // Définition des listeners
        btnSelectImage.setOnClickListener(this);
        add.setOnClickListener(this);
        btnList.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSelectImage) {
            // Ouvrir la galerie pour sélectionner une image
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        }
        else if (v == add) {
            // Vérification des champs vides
            if(nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            requestQueue = Volley.newRequestQueue(getApplicationContext());

            // Si une image a été sélectionnée, l'envoyer d'abord au serveur
            if (selectedImageBitmap != null) {
                uploadImage();
            } else {
                // Créer l'étudiant sans image
                String sexe = m.isChecked() ? "homme" : "femme";

                // Récupération de la date depuis le DatePicker
                DatePicker datePicker = findViewById(R.id.datePicker);
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1; // Les mois commencent à 0
                int year = datePicker.getYear();
                String dateNaissance = String.format("%04d-%02d-%02d", year, month, day);

                HashMap<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString());
                params.put("prenom", prenom.getText().toString());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", sexe);
                params.put("dateNaissance", dateNaissance); // Ajoutez cette ligne
                params.put("photoUrl", "");

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        insertUrl,
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    String message = response.getString("message");
                                    Toast.makeText(addEtudiant.this, message, Toast.LENGTH_SHORT).show();

                                    if ("success".equals(status)) {
                                        // Vider les champs après succès
                                        nom.setText("");
                                        prenom.setText("");
                                        ville.setSelection(0);
                                        m.setChecked(true);
                                        studentImage.setImageResource(R.drawable.ic_person);
                                        selectedImageBitmap = null;
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Erreur de parsing JSON", e);
                                    Toast.makeText(addEtudiant.this, "Erreur de format de réponse", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String errorMessage = "Erreur réseau";
                                if (error instanceof NetworkError) {
                                    errorMessage = "Pas de connexion réseau";
                                } else if (error instanceof ServerError) {
                                    errorMessage = "Erreur serveur";
                                } else if (error instanceof AuthFailureError) {
                                    errorMessage = "Erreur d'authentification";
                                } else if (error instanceof ParseError) {
                                    errorMessage = "Erreur de parsing";
                                } else if (error instanceof NoConnectionError) {
                                    errorMessage = "Pas de connexion Internet";
                                } else if (error instanceof TimeoutError) {
                                    errorMessage = "Timeout de connexion";
                                }

                                Toast.makeText(addEtudiant.this, errorMessage, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Erreur Volley: " + error.toString());
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                requestQueue.add(request);
            }
        }
        else if (v instanceof Button && v.getId() == R.id.btn_list) {
            // Redirection vers l'activité de liste
            Intent intent = new Intent(this, ListEtudiantActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Afficher l'image sélectionnée avec Glide
                Glide.with(this)
                        .load(selectedImageBitmap)
                        .circleCrop()
                        .into(studentImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        // Convertir proprement en base64 avec le préfixe
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        String encodedImage = "data:image/jpeg;base64," +
                Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);

        // Utilisez JsonObjectRequest pour plus de clarté
        JsonObjectRequest uploadRequest = new JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2/projet/ws/uploadImage.php",
                null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Réponse complète: " + response.toString());

                            if(response.getString("status").equals("success")) {
                                String imageUrl = response.getString("url");
                                createStudent(imageUrl);
                            } else {
                                Toast.makeText(addEtudiant.this,
                                        response.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur parsing JSON", e);
                            Toast.makeText(addEtudiant.this,
                                    "Format de réponse inattendu",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorDetails = "Erreur: ";
                        if(error.networkResponse != null) {
                            errorDetails += "Code " + error.networkResponse.statusCode + " - ";
                            try {
                                errorDetails += new String(error.networkResponse.data, "UTF-8");
                            } catch (Exception e) {
                                errorDetails += "Impossible de lire la réponse";
                            }
                        }
                        Log.e(TAG, errorDetails);
                        Toast.makeText(addEtudiant.this, errorDetails, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject params = new JSONObject();
                    params.put("image", encodedImage);
                    return params.toString().getBytes("UTF-8");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        requestQueue.add(uploadRequest);
    }

    private void createStudent(String imageUrl) {
        // Récupérer la date depuis le DatePicker
        DatePicker datePicker = findViewById(R.id.datePicker);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // Les mois commencent à 0
        int year = datePicker.getYear();
        String dateNaissance = String.format("%04d-%02d-%02d", year, month, day);

        // Créer le corps de la requête JSON
        JSONObject jsonBody = new JSONObject();
        try {
            String sexe = m.isChecked() ? "homme" : "femme";
            jsonBody.put("nom", nom.getText().toString());
            jsonBody.put("prenom", prenom.getText().toString());
            jsonBody.put("ville", ville.getSelectedItem().toString());
            jsonBody.put("sexe", sexe);
            jsonBody.put("photoUrl", imageUrl);
            jsonBody.put("dateNaissance", dateNaissance);

            // Log pour débogage
            Log.d("CreateStudent", "Données envoyées: " + jsonBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de création des données", Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer la requête
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                insertUrl, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            Toast.makeText(addEtudiant.this, message, Toast.LENGTH_SHORT).show();

                            if ("success".equals(status)) {
                                // Réinitialiser les champs après succès
                                nom.setText("");
                                prenom.setText("");
                                ville.setSelection(0);
                                m.setChecked(true);
                                studentImage.setImageResource(R.drawable.ic_person);
                                selectedImageBitmap = null;

                                // Réinitialiser la date
                                DatePicker datePicker = findViewById(R.id.datePicker);
                                datePicker.updateDate(2000, 0, 1); // Remettre à une date par défaut (1er janvier 2000)
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(addEtudiant.this, "Erreur de format de réponse", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorDetails = "Erreur: ";
                        if(error.networkResponse != null) {
                            errorDetails += "Code " + error.networkResponse.statusCode + " - ";
                            try {
                                errorDetails += new String(error.networkResponse.data, "UTF-8");
                            } catch (Exception e) {
                                errorDetails += "Impossible de lire la réponse";
                            }
                        }
                        Log.e("CreateStudent", errorDetails);
                        Toast.makeText(addEtudiant.this, "Erreur de création: " + errorDetails, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Ajouter la requête à la file d'attente
        requestQueue.add(request);
    }

}
package com.example.projetws;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import beans.Etudiant;
import beans.adapter.EtudiantAdapter;

public class ListEtudiantActivity extends AppCompatActivity implements EtudiantAdapter.OnItemClickListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final String UPLOAD_URL = "http://10.0.2.2/projet/ws/uploadImage.php";

    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private List<Etudiant> etudiantList = new ArrayList<>();
    private RequestQueue requestQueue;
    private String loadUrl = "http://10.0.2.2/projet/ws/loadEtudiant.php";
    private String deleteUrl = "http://10.0.2.2/projet/ws/deleteEtudiant.php";
    private String updateUrl = "http://10.0.2.2/projet/ws/updateEtudiant.php";

    private Uri selectedImageUri;
    private String currentPhotoUrl;
    private ImageView ivEtudiantUpdate;

    private SearchView searchView;
    private TextView toolbarTitle;
    private ImageView searchIcon;
    private boolean isSearchVisible = false;
    private ImageView closeIcon;
    private View searchContainer;
    private EditText searchEditText;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiant);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize all views properly (remove 'View' and 'EditText' type declarations)
        searchIcon = findViewById(R.id.search_icon);
        closeIcon = findViewById(R.id.close_icon);
        toolbarTitle = findViewById(R.id.title);
        searchContainer = findViewById(R.id.search_container);  // Use class-level variable
        searchEditText = findViewById(R.id.search_edit_text);   // Use class-level variable
        recyclerView = findViewById(R.id.recyclerView);

        // Rest of your initialization code...
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EtudiantAdapter(etudiantList, this);
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        // Configuration des icônes
        searchIcon.setOnClickListener(v -> toggleSearchVisibility(true));
        closeIcon.setOnClickListener(v -> toggleSearchVisibility(false));


        // Configuration du champ de recherche
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadEtudiants();
    }
    @Override
    public void onItemClick(Etudiant etudiant) {
        showActionDialog(etudiant);
    }

    private void showActionDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir une action");
        builder.setItems(new CharSequence[]{"Modifier", "Supprimer"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showUpdateDialog(etudiant);
                        break;
                    case 1:
                        showDeleteConfirmation(etudiant);
                        break;
                }
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showUpdateDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_etudiant, null);
        builder.setView(dialogView);

        // Initialiser les vues
        ivEtudiantUpdate = dialogView.findViewById(R.id.iv_etudiant_update);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        final EditText etNom = dialogView.findViewById(R.id.et_nom);
        final EditText etPrenom = dialogView.findViewById(R.id.et_prenom);
        final Spinner etVille = dialogView.findViewById(R.id.et_ville);
        final RadioGroup etSexeGroup = dialogView.findViewById(R.id.et_sexe_group);
        final DatePicker etDatePicker = dialogView.findViewById(R.id.et_date_picker);

        // Remplir les champs avec les données actuelles de l'étudiant
        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());
        currentPhotoUrl = etudiant.getPhotoUrl();

        // Charger l'image actuelle
        Glide.with(this)
                .load(currentPhotoUrl)
                .placeholder(R.drawable.ic_person)
                .into(ivEtudiantUpdate);

        // Gestion du bouton de sélection d'image
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        // Sélectionner la ville dans le Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.villes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etVille.setAdapter(adapter);
        if (etudiant.getVille() != null) {
            int spinnerPosition = adapter.getPosition(etudiant.getVille());
            etVille.setSelection(spinnerPosition);
        }

        // Sélectionner le sexe dans le RadioGroup
        if (etudiant.getSexe() != null) {
            if (etudiant.getSexe().equalsIgnoreCase("homme")) {
                etSexeGroup.check(R.id.et_sexe_m);
            } else {
                etSexeGroup.check(R.id.et_sexe_f);
            }
        }

        // Gestion de la date de naissance
        if (etudiant.getDateNaissance() != null && !etudiant.getDateNaissance().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(etudiant.getDateNaissance());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                etDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            } catch (ParseException e) {
                e.printStackTrace();
                etDatePicker.updateDate(2000, 0, 1);
            }
        } else {
            etDatePicker.updateDate(2000, 0, 1);
        }

        builder.setTitle("Modifier l'étudiant");
        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            // Récupérer les nouvelles valeurs
            String nom = etNom.getText().toString();
            String prenom = etPrenom.getText().toString();
            String ville = etVille.getSelectedItem().toString();

            int selectedId = etSexeGroup.getCheckedRadioButtonId();
            String sexe = (selectedId == R.id.et_sexe_m) ? "homme" : "femme";

            String dateNaissance = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    etDatePicker.getYear(),
                    etDatePicker.getMonth() + 1,
                    etDatePicker.getDayOfMonth());

            // Si une nouvelle image a été sélectionnée, uploader d'abord
            if (selectedImageUri != null) {
                uploadImageAndUpdateEtudiant(etudiant.getId(), nom, prenom, ville, sexe, dateNaissance);
            } else {
                // Sinon, mettre à jour directement avec l'URL existante
                updateEtudiant(etudiant.getId(), nom, prenom, ville, sexe, dateNaissance, currentPhotoUrl);
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    ivEtudiantUpdate.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur de chargement de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadImageAndUpdateEtudiant(int id, String nom, String prenom, String ville, String sexe, String dateNaissance) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("image", "data:image/jpeg;base64," + encodedImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    UPLOAD_URL,
                    jsonBody,
                    response -> {
                        try {
                            String imageUrl = response.getString("url");
                            updateEtudiant(id, nom, prenom, ville, sexe, dateNaissance, imageUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Erreur de traitement de la réponse", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Erreur d'upload de l'image", Toast.LENGTH_SHORT).show();
                    });

            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de traitement de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEtudiant(int id, String nom, String prenom, String ville, String sexe, String dateNaissance, String photoUrl) {
        // Garantir que photoUrl n'est jamais null
        String finalPhotoUrl = (photoUrl != null && !photoUrl.isEmpty()) ?
                photoUrl : "http://10.0.2.2/projet/ws/uploads/default.jpg";

        StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                response -> {
                    Toast.makeText(ListEtudiantActivity.this, "Étudiant mis à jour", Toast.LENGTH_SHORT).show();
                    loadEtudiants(); // Recharger les données après mise à jour
                    selectedImageUri = null;
                },
                error -> {
                    Toast.makeText(ListEtudiantActivity.this, "Erreur de mise à jour: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("nom", nom);
                params.put("prenom", prenom);
                params.put("ville", ville);
                params.put("sexe", sexe);
                params.put("dateNaissance", dateNaissance);
                params.put("photoUrl", finalPhotoUrl);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void showDeleteConfirmation(final Etudiant etudiant) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer cet étudiant?")
                .setPositiveButton("Oui", (dialog, which) -> deleteEtudiant(etudiant.getId()))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteEtudiant(int id) {
        StringRequest request = new StringRequest(Request.Method.POST, deleteUrl,
                response -> {
                    Toast.makeText(ListEtudiantActivity.this, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                    loadEtudiants();
                },
                error -> Toast.makeText(ListEtudiantActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void loadEtudiants() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, loadUrl, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");
                            List<Etudiant> etudiants = new Gson().fromJson(data.toString(),
                                    new TypeToken<List<Etudiant>>(){}.getType());

                            runOnUiThread(() -> {
                                etudiantList.clear();
                                etudiantList.addAll(etudiants); // Stocker la liste complète
                                adapter.updateList(etudiants);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("LoadError", "Parsing error: " + e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(this, "Erreur de données", Toast.LENGTH_LONG).show());
                    }
                },
                error -> {
                    Log.e("LoadError", "Network error: " + error.toString());
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_LONG).show());
                });

        requestQueue.add(request);
    }

    private void setupSearchView() {
        // Configurer le SearchView
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Rechercher un étudiant...");

        // Personnaliser l'apparence du texte
        int searchTextId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = searchView.findViewById(searchTextId);
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.parseColor("#B3FFFFFF"));
            searchEditText.setTextSize(16);
        }

        // Gestion des requêtes de recherche
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadEtudiants();
                } else {
                    performSearch(newText);
                }
                return true;
            }
        });
    }

    private void toggleSearchVisibility(boolean show) {
        if (searchContainer == null || searchEditText == null) {
            Log.e("ToggleSearch", "Search views not initialized");
            return;
        }

        if (show) {
            // Afficher le champ de recherche et l'icône de fermeture
            if (toolbarTitle != null) toolbarTitle.setVisibility(View.GONE);
            searchContainer.setVisibility(View.VISIBLE);
            if (searchIcon != null) searchIcon.setVisibility(View.GONE);
            if (closeIcon != null) closeIcon.setVisibility(View.VISIBLE);

            // Donner le focus au champ et ouvrir le clavier
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            // Cacher le champ de recherche et l'icône de fermeture
            if (toolbarTitle != null) toolbarTitle.setVisibility(View.VISIBLE);
            searchContainer.setVisibility(View.GONE);
            if (searchIcon != null) searchIcon.setVisibility(View.VISIBLE);
            if (closeIcon != null) closeIcon.setVisibility(View.GONE);

            // Effacer le texte et enlever le focus
            searchEditText.setText("");
            searchEditText.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }

            // Recharger la liste complète
            loadEtudiants();
        }
    }
    private void performSearch(String query) {
        if (query.isEmpty()) {
            loadEtudiants();
            return;
        }

        // Filtrer localement d'abord
        List<Etudiant> filteredList = new ArrayList<>();
        for (Etudiant etudiant : etudiantList) {
            if (etudiant.getNom().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(etudiant);
            }
        }

        if (filteredList.isEmpty()) {
            // Si aucun résultat local, essayer une recherche côté serveur
            searchOnServer(query);
        } else {
            adapter.updateList(filteredList);
        }
    }

    private void filterStudents(String query) {
        List<Etudiant> filteredList = new ArrayList<>();
        for (Etudiant etudiant : etudiantList) {
            if (etudiant.getNom().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(etudiant);
            }
        }
        adapter.updateList(filteredList);
    }

    private void searchOnServer(String query) {
        String searchUrl = "http://10.0.2.2/projet/ws/searchEtudiant.php";

        StringRequest request = new StringRequest(Request.Method.POST, searchUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONArray data = jsonResponse.getJSONArray("data");
                            List<Etudiant> searchResults = new Gson().fromJson(data.toString(),
                                    new TypeToken<List<Etudiant>>(){}.getType());

                            runOnUiThread(() -> {
                                if (searchResults.isEmpty()) {
                                    Toast.makeText(this, "Aucun résultat trouvé", Toast.LENGTH_SHORT).show();
                                } else {
                                    adapter.updateList(searchResults);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de traitement des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Erreur de recherche", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("query", query);
                return params;
            }
        };

        requestQueue.add(request);
    }
}
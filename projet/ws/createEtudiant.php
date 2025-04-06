<?php

/*
 * Hadi fiya la methode li khdmna biha flwle avant mansifto donner as format json
 * if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once '../racine.php';
    include_once RACINE . '/service/EtudiantService.php';
    create();
}

function create() {
    extract($_POST);
    $es = new EtudiantService();
    $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe));
}
 */



header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

// Activer les erreurs mais les envoyer vers les logs, pas vers la sortie
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

// Inclure les fichiers nécessaires
include_once '../racine.php';
include_once RACINE . '/service/EtudiantService.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    try {
        // Check content type
        $contentType = isset($_SERVER["CONTENT_TYPE"]) ? $_SERVER["CONTENT_TYPE"] : '';
        
        if (strpos($contentType, 'application/json') !== false) {
            $json = file_get_contents('php://input');
            $data = json_decode($json, true);
        } else {
            $data = $_POST; // Fallback to form data
        }

        // Créer l'étudiant
        $es = new EtudiantService();
        $etudiant = new Etudiant(
                1,
                $data['nom'],
                $data['prenom'],
                $data['ville'],
                $data['sexe'],
                $data['photoUrl'] ?? '',
                $data['dateNaissance'] ?? null
        );

        $es->create($etudiant);

        // Réponse JSON
        echo json_encode([
            "status" => "success",
            "message" => "Étudiant créé avec succès"
        ]);
    } catch (Exception $e) {
        http_response_code(400);
        echo json_encode([
            "status" => "error",
            "message" => $e->getMessage()
        ]);
    }
} else {
    http_response_code(405);
    echo json_encode([
        "status" => "error",
        "message" => "Méthode non autorisée"
    ]);
}




/*
   * Hadi la methode  2 li khdmna biha bache nsifto donner as format json
   if ($_SERVER["REQUEST_METHOD"] == "POST") {
    chdir("..");
    include_once 'service/EtudiantService.php';
    create();
}

function create() {
    extract($_POST);
    $es = new EtudiantService();
    //insertion d’un étudiant 
    $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe));

    //chargement de la liste des étudiants sous format json 
    header('Content-type: application/json');
    echo json_encode($es->findAllApi());
}*/

<?php

include_once '../racine.php';
include_once RACINE . '/service/EtudiantService.php';

file_put_contents('update_log.txt', print_r($_POST, true), FILE_APPEND);

$es = new EtudiantService();
$etudiant = $es->findById($_POST['id']);

if ($etudiant != null) {
    // Vérifier chaque champ avant mise à jour
    $etudiant->setNom($_POST['nom'] ?? $etudiant->getNom());
    $etudiant->setPrenom($_POST['prenom'] ?? $etudiant->getPrenom());
    $etudiant->setVille($_POST['ville'] ?? $etudiant->getVille());
    $etudiant->setSexe($_POST['sexe'] ?? $etudiant->getSexe());
    $etudiant->setDateNaissance($_POST['dateNaissance'] ?? $etudiant->getDateNaissance());

    // Vérifier spécialement photoUrl
    $photoUrl = $_POST['photoUrl'] ?? $etudiant->getPhotoUrl();
    if (!empty($photoUrl)) {
        $etudiant->setPhotoUrl($photoUrl);
    }

    if ($es->update($etudiant)) {
        // Après $es->update($etudiant)
        file_put_contents('update_debug.txt',
                "ID: " . $etudiant->getId() . "\n" .
                "Rows updated: " . $rowsAffected . "\n" .
                "Last error: " . json_encode(error_get_last()) . "\n",
                FILE_APPEND);
        echo json_encode(['status' => 'success', 'message' => 'Mise à jour réussie']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Échec de la mise à jour']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Étudiant non trouvé']);
}
?>
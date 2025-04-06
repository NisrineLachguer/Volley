<?php

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

require_once '../connexion/connexion.php';
require_once '../dao/dao.php';
require_once '../service/EtudiantService.php';

$data = json_decode(file_get_contents("php://input"));

$response = array();
$etudiantService = new EtudiantService();

try {
    if (isset($data->query)) {
        $query = $data->query;
        $etudiants = $etudiantService->searchEtudiant($query);

        $response["status"] = "success";
        $response["data"] = $etudiants;
    } else {
        $response["status"] = "error";
        $response["message"] = "Paramètre de recherche manquant";
    }
} catch (Exception $e) {
    $response["status"] = "error";
    $response["message"] = $e->getMessage();
}

echo json_encode($response);
?>
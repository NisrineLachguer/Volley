<?php

/* if ($_SERVER["REQUEST_METHOD"] == "POST") {
  include_once '../racine.php';
  include_once RACINE . '/service/EtudiantService.php';
  loadAll();
  }

  function loadAll() {
  $es = new EtudiantService();
  header('Content-type: application/json');
  echo json_encode($es->findAllApi());
  }


 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Scripting/EmptyPHP.php to edit this template
 */


header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

include_once '../racine.php';
include_once RACINE . '/service/EtudiantService.php';

try {
    $es = new EtudiantService();
    $etudiants = $es->findAllApi();

    echo json_encode([
        'status' => 'success',
        'data' => $etudiants,
        'count' => count($etudiants),
        'timestamp' => time()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage()
    ]);
}
?>


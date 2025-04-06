<?php

include_once '../racine.php';
include_once RACINE . '/service/EtudiantService.php';

$es = new EtudiantService();
$es->delete($es->findById($_POST['id']));

echo json_encode(array('status' => 'success'));
?>
<?php

header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

error_reporting(E_ALL);
ini_set('display_errors', 1);

// Vérifier la méthode HTTP
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(["status" => "error", "message" => "Méthode non autorisée"]);
    exit;
}

// Vérifier si des données ont été envoyées
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (json_last_error() !== JSON_ERROR_NONE || !isset($data['image'])) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Données invalides ou image manquante"]);
    exit;
}

// Traitement de l'image
$imageData = $data['image'];
$prefix = 'data:image/';

// Vérifier le type d'image
if (strpos($imageData, $prefix) !== 0) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Format d'image non supporté"]);
    exit;
}

// Extraire le type MIME et les données
$mimeType = substr($imageData, strpos($imageData, ':') + 1, strpos($imageData, ';') - strpos($imageData, ':') - 1);
$base64Data = substr($imageData, strpos($imageData, ',') + 1);

// Vérifier les types MIME autorisés
$allowedMimeTypes = ['image/jpeg', 'image/png', 'image/gif'];
if (!in_array($mimeType, $allowedMimeTypes)) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Type d'image non supporté"]);
    exit;
}

// Décoder les données base64
$decoded = base64_decode($base64Data, true);
if ($decoded === false) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "Données image corrompues"]);
    exit;
}

// Vérifier la taille de l'image (max 5MB)
if (strlen($decoded) > 5 * 1024 * 1024) {
    http_response_code(400);
    echo json_encode(["status" => "error", "message" => "L'image est trop volumineuse (max 5MB)"]);
    exit;
}

// Créer le dossier upload s'il n'existe pas
$uploadDir = __DIR__ . '/uploads/';
if (!is_dir($uploadDir) && !mkdir($uploadDir, 0755, true)) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Impossible de créer le dossier de destination"]);
    exit;
}

// Déterminer l'extension du fichier
$extension = 'jpg';
if ($mimeType === 'image/png')
    $extension = 'png';
if ($mimeType === 'image/gif')
    $extension = 'gif';

// Générer un nom de fichier unique
$filename = uniqid('img_') . '.' . $extension;
$filepath = $uploadDir . $filename;

// Sauvegarder le fichier
if (file_put_contents($filepath, $decoded) === false) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Échec de l'enregistrement de l'image"]);
    exit;
}
// Dans uploadImage.php, avant le echo final
file_put_contents('upload_log.txt', print_r([
    "status" => "success",
    "url" => "http://10.0.2.2/projet/ws/uploads/" . $filename,
    "filename" => $filename
], true), FILE_APPEND);

// Réponse de succès
echo json_encode([
    "status" => "success",
    "url" => "http://10.0.2.2/projet/ws/uploads/" . $filename,
    "filename" => $filename,
    "size" => strlen($decoded),
    "mimeType" => $mimeType
        ], JSON_PRETTY_PRINT);
?>
<?php

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Scripting/PHPClass.php to edit this template
 */

/**
 * Description of EtudiantService
 *
 * @author Nisrine
 */
include_once RACINE . '/classes/Etudiant.php';
include_once RACINE . '/connexion/Connexion.php';
include_once RACINE . '/dao/IDao.php';

class EtudiantService Implements IDao {

    //put your code here
    private $connexion;

    function __construct() {
        $this->connexion = new Connexion();
    }

    public function create($o) {
        //$query = "INSERT INTO Etudiant (`id`, `nom`, `prenom`, `ville`, `sexe`) " . "VALUES (NULL, '" . $o->getNom() . "', '" . $o->getPrenom() . "','" . $o->getVille() . "', '" . $o->getSexe() . "');";
        //$query = "INSERT INTO Etudiant (`id`, `nom`, `prenom`, `ville`, `sexe`, `photoUrl`) " . "VALUES (NULL, '" . $o->getNom() . "', '" . $o->getPrenom() . "','" . $o->getVille() . "', '" . $o->getSexe() . "', '" . $o->getPhotoUrl() . "');";
        $query = "INSERT INTO Etudiant (`id`, `nom`, `prenom`, `ville`, `sexe`, `photoUrl`, `dateNaissance`) " . "VALUES (NULL, '" . $o->getNom() . "', '" . $o->getPrenom() . "','" . $o->getVille() . "', '" . $o->getSexe() . "', '" . $o->getPhotoUrl() . "', '" . $o->getDateNaissance() . "');";
        $req = $this->connexion->getConnexion()->prepare($query);
        
        $req->execute() or die('Erreur SQL');
  
    }

    public function delete($o) {
        $query = "delete from Etudiant where id = " . $o->getId();
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute() or die('Erreur SQL');
    }

    public function findAll() {
        $etds = array();
        $query = "select * from Etudiant";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute();
        while ($e = $req->fetch(PDO::FETCH_OBJ)) {
            //$etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe);
            //$etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photoUrl);
            $etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photoUrl, $e->dateNaissance);
        }
        return $etds;
    }

    /*public function findById($id) {
        $query = "select * from Etudiant where id = " . $id;
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute();
        if ($e = $req->fetch(PDO::FETCH_OBJ)) {
            //$etd = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe);
            //$etd = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photoUrl);
            $etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photoUrl, $e->dateNaissance);
        }
        return $etds;
    }*/
    public function findById($id) {
           $query = "select * from Etudiant where id = " . $id;
           $req = $this->connexion->getConnexion()->prepare($query);
           $req->execute();
           if ($e = $req->fetch(PDO::FETCH_OBJ)) {
               $etd = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photoUrl, $e->dateNaissance);
               return $etd; // Retourner l'objet Etudiant directement
           }
           return null;
       }

    public function update($o) {
        $query = "UPDATE `etudiant` SET 
        `nom` = ?, 
        `prenom` = ?, 
        `ville` = ?,
        `sexe` = ?, 
        `dateNaissance` = ?, 
        `photoUrl` = ? 
        WHERE `id` = ?";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute([
                    $o->getNom(),
                    $o->getPrenom(),
                    $o->getVille(),
                    $o->getSexe(),
                    $o->getDateNaissance(),
                    $o->getPhotoUrl(),
                    $o->getId()
                ]) or die('Erreur SQL');
        return true;
    }

    public function findAllApi() {
        $query = "select * from Etudiant";
        $req = $this->connexion->getConnexion()->prepare($query);
        $req->execute();
        return $req->fetchAll(PDO::FETCH_ASSOC);
    }
    
    public function searchEtudiant($query) {
        $sql = "SELECT * FROM etudiant WHERE nom LIKE :query OR prenom LIKE :query";
        $stmt = $this->dao->getConnection()->prepare($sql);
        $searchQuery = "%" . $query . "%";
        $stmt->bindParam(':query', $searchQuery);
        $stmt->execute();

        $etudiants = array();
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $etudiant = new Etudiant();
            $etudiant->setId($row['id']);
            $etudiant->setNom($row['nom']);
            $etudiant->setPrenom($row['prenom']);
            $etudiant->setVille($row['ville']);
            $etudiant->setSexe($row['sexe']);
            $etudiant->setDateNaissance($row['dateNaissance']);
            $etudiant->setPhotoUrl($row['photoUrl']);

            array_push($etudiants, $etudiant);
        }

        return $etudiants;
    }
}

<?php

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Scripting/PHPClass.php to edit this template
 */

/**
 * Description of Etudiant
 *
 * @author Nisrine
 */
class Etudiant {

     private $id;
     private $nom;
     private $prenom;
     private $ville;
     private $sexe;
     private $photoUrl; 
     private $dateNaissance;

    public function __construct($id, $nom, $prenom, $ville, $sexe, $photoUrl = '', $dateNaissance = null) {
        $this->id = $id;
        $this->nom = $nom;
        $this->prenom = $prenom;
        $this->ville = $ville;
        $this->sexe = $sexe;
        $this->photoUrl = $photoUrl;
        $this->dateNaissance = $dateNaissance;
    }

    public function getDateNaissance() {
        return $this->dateNaissance;
    }

    public function setDateNaissance($dateNaissance) {
        $this->dateNaissance = $dateNaissance;
    }
     function getId() {
     return $this->id;
     }
     function getNom() {
     return $this->nom;
     }
     function getPrenom() {return $this->prenom;
     }
     function getVille() {
     return $this->ville;
     }
     function getSexe() {
     return $this->sexe;
     }
     function setId($id) {
     $this->id = $id;
     }
     function setNom($nom) {
     $this->nom = $nom;
     }
     function setPrenom($prenom) {
     $this->prenom = $prenom;
     }
     function setVille($ville) {
     $this->ville = $ville;
     }
     function setSexe($sexe) {
     $this->sexe = $sexe;
     }
     public function __toString() {
     return $this->nom . " " . $this->prenom;
     }
     public function getPhotoUrl() {
        return $this->photoUrl;
    }

    public function setPhotoUrl($photoUrl) {
        $this->photoUrl = $photoUrl;
    }

    
}

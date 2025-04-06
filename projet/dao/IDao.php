<?php

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Scripting/PHPInterface.php to edit this template
 */

/**
 *
 * @author Nisrine
 */
interface IDao {
    //put your code here
    function create($o);
    function delete($o);
    function update($o);
    function findAll();
    function findById($id);
}

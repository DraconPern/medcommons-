<?php

//registerpost.php - process MOD register request
require_once "modpay.inc.php";
require_once "template.inc.php";

list($accid,$fn,$ln,$email,$idp,$mc,$auth)=logged_in();

$couponnum = $_REQUEST['c'];

$markup = '<div id="ContentBoxInterior" mainTitle="Preview MedCommons Voucher" >'.file_get_contents("displaycoupon.html");
$markup = standardcoupon ($couponnum,$markup);

echo template("base.tpl.php")
        ->set("title","Voucher Preview - MedCommons on Demand")
        ->set("content",$markup)
        ->fetch();
?>

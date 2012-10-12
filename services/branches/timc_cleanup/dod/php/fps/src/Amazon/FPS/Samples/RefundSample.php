<?php
/** 
 *  PHP Version 5
 *
 *  @category    Amazon
 *  @package     Amazon_FPS
 *  @copyright   Copyright 2007 Amazon Technologies, Inc.
 *  @link        http://aws.amazon.com
 *  @license     http://aws.amazon.com/apache2.0  Apache License, Version 2.0
 *  @version     2008-05-01
 */
/******************************************************************************* 
 *    __  _    _  ___ 
 *   (  )( \/\/ )/ __)
 *   /__\ \    / \__ \
 *  (_)(_) \/\/  (___/
 * 
 *  Amazon FPS PHP5 Library
 *  Generated: Thu Apr 24 02:05:45 PDT 2008
 * 
 */

/**
 * Refund  Sample
 */

include_once ('.config.inc.php'); 

require_once ('Amazon/FPS/Model/Refund.php');

/************************************************************************
 * Instantiate Implementation of Amazon FPS
 * 
 * AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY constants 
 * are defined in the .config.inc.php located in the same 
 * directory as this sample
 ***********************************************************************/
 $service = new Amazon_FPS_Client(AWS_ACCESS_KEY_ID, 
                                       AWS_SECRET_ACCESS_KEY);
 
/************************************************************************
 * Uncomment to try out Mock Service that simulates Amazon_FPS
 * responses without calling Amazon_FPS service.
 *
 * Responses are loaded from local XML files. You can tweak XML files to
 * experiment with various outputs during development
 *
 * XML files available under Amazon/FPS/Mock tree
 *
 ***********************************************************************/
 // $service = new Amazon_FPS_Mock();

/************************************************************************
 * Setup request parameters and uncomment invoke to try out 
 * sample for Refund Action
 ***********************************************************************/
 // @TODO: set request. Action can be passed as Amazon_FPS_Model_Refund 
 // object or array of parameters
 $time = time();
$request = new Amazon_FPS_Model_Refund();
$request->setTransactionId('13HOOTN577666F6G71NE5R2LC4RCVGFOZTD'); //set the txn id
$request->setRefundTransactionReference("Refund $time");//Unique transaction reference  
$request->setTransactionDescription('chargeback');//description for the refund 
$amount = new Amazon_FPS_Model_Amount();
$amount->setCurrencyCode('USD');
$amount->setValue(1.00); //amount to be refunded
$request->setRefundAmount($amount);
//$request->setMarketplaceRefundPolicy(6);//This field is optional

invokeRefund($service, $request);

                        
/**
  * Refund Action Sample
  * 
  * This operation enables the merchant issue a complete/partial refund of the
  * original transaction. The refunded money goes into the customer's credit card
  * if the original payment instrument was credit card. Else it goes into the
  * customer's ABT balance.
  * If the original transaction is a marketplace transaction, by default the
  * Marketplace fee is not refunded. This can be overridden by the caller.
  * 
  *   
  * @param Amazon_FPS_Interface $service instance of Amazon_FPS_Interface
  * @param mixed $request Amazon_FPS_Model_Refund or array of parameters
  */
  function invokeRefund(Amazon_FPS_Interface $service, $request) 
  {
      try {
              $response = $service->refund($request);
              
                echo ("Service Response\n");
                echo ("=============================================================================\n");

                echo("        RefundResponse\n");
                if ($response->isSetResponseMetadata()) { 
                    echo("            ResponseMetadata\n");
                    $responseMetadata = $response->getResponseMetadata();
                    if ($responseMetadata->isSetRequestId()) 
                    {
                        echo("                RequestId\n");
                        echo("                    " . $responseMetadata->getRequestId() . "\n");
                    }
                } 
                if ($response->isSetRefundResult()) { 
                    echo("            RefundResult\n");
                    $refundResult = $response->getRefundResult();
                    if ($refundResult->isSetTransactionStatus()) 
                    {
                        echo("                TransactionStatus\n");
                        echo("                    " . $refundResult->getTransactionStatus() . "\n");
                    }
                    if ($refundResult->isSetTransactionId()) 
                    {
                        echo("                TransactionId\n");
                        echo("                    " . $refundResult->getTransactionId() . "\n");
                    }
                    if ($refundResult->isSetPendingReason()) 
                    {
                        echo("                PendingReason\n");
                        echo("                    " . $refundResult->getPendingReason() . "\n");
                    }
                } 

     } catch (Amazon_FPS_Exception $ex) {
         echo("Caught Exception: " . $ex->getMessage() . "\n");
         echo("Response Status Code: " . $ex->getStatusCode() . "\n");
         echo("Error Code: " . $ex->getErrorCode() . "\n");
         echo("Error Type: " . $ex->getErrorType() . "\n");
         echo("Request ID: " . $ex->getRequestId() . "\n");
         echo("XML: " . $ex->getXML() . "\n");
     }
 }
        

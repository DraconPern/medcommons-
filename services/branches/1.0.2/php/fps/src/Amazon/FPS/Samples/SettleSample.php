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
 * Settle  Sample
 */

include_once ('.config.inc.php'); 

require_once ('Amazon/FPS/Model/Settle.php');

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
 * sample for Settle Action
 ***********************************************************************/
$request =  new Amazon_FPS_Model_Settle();
$request->setTransactionId('');//set the transaction Id here
$amount = new Amazon_FPS_Model_Amount();
$amount->setCurrencyCode('USD');
$amount->setValue(6);//set the amount here
$request->setSettleAmount($amount);

 // @TODO: set request. Action can be passed as Amazon_FPS_Model_Settle 
 // object or array of parameters
invokeSettle($service, $request);

                            
/**
  * Settle Action Sample
  * 
  * This operation enables merchants to receive funds they had reserved on a
  * customer's Amazon Payments Account Balance/Credit Card. They can settle partial
  * or full amount authoized on the payment instrument using this operation.
  *   
  * @param Amazon_FPS_Interface $service instance of Amazon_FPS_Interface
  * @param mixed $request Amazon_FPS_Model_Settle or array of parameters
  */
  function invokeSettle(Amazon_FPS_Interface $service, $request) 
  {
      try {
              $response = $service->settle($request);
              
                echo ("Service Response\n");
                echo ("=============================================================================\n");

                echo("        SettleResponse\n");
                if ($response->isSetResponseMetadata()) { 
                    echo("            ResponseMetadata\n");
                    $responseMetadata = $response->getResponseMetadata();
                    if ($responseMetadata->isSetRequestId()) 
                    {
                        echo("                RequestId\n");
                        echo("                    " . $responseMetadata->getRequestId() . "\n");
                    }
                } 
                if ($response->isSetSettleResult()) { 
                    echo("            SettleResult\n");
                    $settleResult = $response->getSettleResult();
                    if ($settleResult->isSetTransactionStatus()) 
                    {
                        echo("                TransactionStatus\n");
                        echo("                    " . $settleResult->getTransactionStatus() . "\n");
                    }
                    if ($settleResult->isSetTransactionId()) 
                    {
                        echo("                TransactionId\n");
                        echo("                    " . $settleResult->getTransactionId() . "\n");
                    }
                    if ($settleResult->isSetPendingReason()) 
                    {
                        echo("                PendingReason\n");
                        echo("                    " . $settleResult->getPendingReason() . "\n");
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
    

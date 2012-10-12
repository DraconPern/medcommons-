<?php
/** 
 *  PHP Version 5
 *
 *  @category    Amazon
 *  @package     Amazon_EC2
 *  @copyright   Copyright 2008-2009 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *  @link        http://aws.amazon.com
 *  @license     http://aws.amazon.com/apache2.0  Apache License, Version 2.0
 *  @version     2009-11-30
 */
/******************************************************************************* 
 *    __  _    _  ___ 
 *   (  )( \/\/ )/ __)
 *   /__\ \    / \__ \
 *  (_)(_) \/\/  (___/
 * 
 *  Amazon EC2 PHP5 Library
 *  Generated: Fri Dec 11 13:55:19 PST 2009
 * 
 */

/**
 *  @see Amazon_EC2_Model
 */
require_once ('Amazon/EC2/Model.php');  

    

/**
 * Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse
 * 
 * Properties:
 * <ul>
 * 
 * <li>ResponseMetadata: Amazon_EC2_Model_ResponseMetadata</li>
 * <li>PurchaseReservedInstancesOfferingResult: Amazon_EC2_Model_PurchaseReservedInstancesOfferingResult</li>
 *
 * </ul>
 */ 
class Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse extends Amazon_EC2_Model
{


    /**
     * Construct new Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse
     * 
     * @param mixed $data DOMElement or Associative Array to construct from. 
     * 
     * Valid properties:
     * <ul>
     * 
     * <li>ResponseMetadata: Amazon_EC2_Model_ResponseMetadata</li>
     * <li>PurchaseReservedInstancesOfferingResult: Amazon_EC2_Model_PurchaseReservedInstancesOfferingResult</li>
     *
     * </ul>
     */
    public function __construct($data = null)
    {
        $this->_fields = array (
        'ResponseMetadata' => array('FieldValue' => null, 'FieldType' => 'Amazon_EC2_Model_ResponseMetadata'),
        'PurchaseReservedInstancesOfferingResult' => array('FieldValue' => null, 'FieldType' => 'Amazon_EC2_Model_PurchaseReservedInstancesOfferingResult'),
        );
        parent::__construct($data);
    }

       
    /**
     * Construct Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse from XML string
     * 
     * @param string $xml XML string to construct from
     * @return Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse 
     */
    public static function fromXML($xml)
    {
        $dom = new DOMDocument();
        $dom->loadXML($xml);
        $xpath = new DOMXPath($dom);
    	$xpath->registerNamespace('a', 'http://ec2.amazonaws.com/doc/2009-11-30/');
        $response = $xpath->query('//a:PurchaseReservedInstancesOfferingResponse');
        if ($response->length == 1) {
            return new Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse(($response->item(0))); 
        } else {
            throw new Exception ("Unable to construct Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse from provided XML. 
                                  Make sure that PurchaseReservedInstancesOfferingResponse is a root element");
        }
          
    }
    
    /**
     * Gets the value of the ResponseMetadata.
     * 
     * @return ResponseMetadata ResponseMetadata
     */
    public function getResponseMetadata() 
    {
        return $this->_fields['ResponseMetadata']['FieldValue'];
    }

    /**
     * Sets the value of the ResponseMetadata.
     * 
     * @param ResponseMetadata ResponseMetadata
     * @return void
     */
    public function setResponseMetadata($value) 
    {
        $this->_fields['ResponseMetadata']['FieldValue'] = $value;
        return;
    }

    /**
     * Sets the value of the ResponseMetadata  and returns this instance
     * 
     * @param ResponseMetadata $value ResponseMetadata
     * @return Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse instance
     */
    public function withResponseMetadata($value)
    {
        $this->setResponseMetadata($value);
        return $this;
    }


    /**
     * Checks if ResponseMetadata  is set
     * 
     * @return bool true if ResponseMetadata property is set
     */
    public function isSetResponseMetadata()
    {
        return !is_null($this->_fields['ResponseMetadata']['FieldValue']);

    }

    /**
     * Gets the value of the PurchaseReservedInstancesOfferingResult.
     * 
     * @return PurchaseReservedInstancesOfferingResult PurchaseReservedInstancesOfferingResult
     */
    public function getPurchaseReservedInstancesOfferingResult() 
    {
        return $this->_fields['PurchaseReservedInstancesOfferingResult']['FieldValue'];
    }

    /**
     * Sets the value of the PurchaseReservedInstancesOfferingResult.
     * 
     * @param PurchaseReservedInstancesOfferingResult PurchaseReservedInstancesOfferingResult
     * @return void
     */
    public function setPurchaseReservedInstancesOfferingResult($value) 
    {
        $this->_fields['PurchaseReservedInstancesOfferingResult']['FieldValue'] = $value;
        return;
    }

    /**
     * Sets the value of the PurchaseReservedInstancesOfferingResult  and returns this instance
     * 
     * @param PurchaseReservedInstancesOfferingResult $value PurchaseReservedInstancesOfferingResult
     * @return Amazon_EC2_Model_PurchaseReservedInstancesOfferingResponse instance
     */
    public function withPurchaseReservedInstancesOfferingResult($value)
    {
        $this->setPurchaseReservedInstancesOfferingResult($value);
        return $this;
    }


    /**
     * Checks if PurchaseReservedInstancesOfferingResult  is set
     * 
     * @return bool true if PurchaseReservedInstancesOfferingResult property is set
     */
    public function isSetPurchaseReservedInstancesOfferingResult()
    {
        return !is_null($this->_fields['PurchaseReservedInstancesOfferingResult']['FieldValue']);

    }



    /**
     * XML Representation for this object
     * 
     * @return string XML for this object
     */
    public function toXML() 
    {
        $xml = "";
        $xml .= "<PurchaseReservedInstancesOfferingResponse xmlns=\"http://ec2.amazonaws.com/doc/2009-11-30/\">";
        $xml .= $this->_toXMLFragment();
        $xml .= "</PurchaseReservedInstancesOfferingResponse>";
        return $xml;
    }

}
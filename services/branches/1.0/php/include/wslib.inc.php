<?php

// rest web service - outerframework

class XMLFragment {
    public $xml = null;
    
    function __construct($xml) {
        $this->xml = $xml;
    }
    
    function __toString() {
        return $this->xml;
    }
}

abstract class restws {

  /* Set to true in unit tests, prevents exit */
  public $test = false;

    private $outbuf;
    private $servicetag;

    function set_servicetag ($s) { $this->servicetag = $s;} // sets outer tag

    function cleanreq($fieldname)
    {
        // take an input field from the command line or POST
        // and clean it up before going any further
        if (!isset($_REQUEST[$fieldname])) return false; //wld 07sep06 - tough checking
        $value = $_REQUEST[$fieldname];
        $value = htmlspecialchars($value);
        return $value;
    }

    abstract function xmlbody ();
    
    function xmlreply ()
    {
        // generate headers
        $mimetype = 'text/xml';
        $charset = 'ISO-8859-1';
        if(!$this->test)
          header("Content-type: $mimetype; charset=$charset");
        echo ('<?xml version="1.0" ?>'."\n");
        echo $this->outbuf; // this is where we can trace
    }

  function xm($s) { 
      if($s instanceof XMLFragment)
          $this->outbuf.= $s->xml;
      else
          $this->outbuf.= $s;
  }

  /**
   * Escape xml entities
   */
  function xmlentities($string) {
     return str_replace ( array ( '&', '"', "'", '<', '>' ), array ( '&amp;' , '&quot;', '&apos;' , '&lt;' , '&gt;' ), $string );
  }
    
  function xmnest($tag,$val) {
    return new XMLFragment("<$tag>$val</$tag>");
  }
  
  /**
   * Convenience method to format XML tag.  This method
   * actually returns an XMLFragment object which can be treated
   * as a string because PHP implicitly will cast it to a string.
   * <p>
   * If the argument is a raw string then it will be escaped.
   * However if it's an XMLFragment it will be left unescaped.
   * To make an xml field with multiple children, separate
   * children with commas, like so:
   * <p>
   *   xmfield("foo", xmfield("bar","tree"), xmfield("cat","dog"))
   * <p>
   * If you want *no* escaping of children, use xmnest() instead.
   * 
   * @param String $tag name of tag
   * @param String  $val contents of tag, will be escaped
   * @return XML fragment for formatted tag
   */
  function xmfield($tag) {
      
    $out = "";
      
    $numargs = func_num_args();
    for($i=1; $i<$numargs; ++$i) {
        
        $val = func_get_arg($i);
        
        // just returns a string, must go thru xm() to be seen
        if($val === null) {
            // output nothing
        }
        else
        if($val instanceof XMLFragment) 
            $out .= $val;
        else 
            $out .= $this->xmlentities($val);
    }
    return new XMLFragment("<$tag>$out</$tag>");
  }

    //
    //outer frame of XML document response is implemented by
    //   calling xmltop {calls to xm}  calling xmlend()
    //
    function xmltop()
    {
        $this->outbuf="";
        $this->xm("<".$this->servicetag.">\n");//outer level
        $srva = $_SERVER['SERVER_ADDR'];
        $srvp = $_SERVER['SERVER_PORT'];
        $gmt = gmstrftime("%b %d %Y %H:%M:%S");
        $uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
        $this->xm("<details>$srva:$srvp $gmt GMT</details>");
    //    $this->xm("<referer>".htmlspecialchars($_SERVER ['HTTP_REFERER'])."</referer>\n");
        $this->xm("<requesturi>\n".$uri."</requesturi>\n");
    }

    function xmlend( $xml_status)
    {
        $this->xm("<summary_status>".$xml_status."</summary_status>\n");
        $this->xm("</".$this->servicetag.">\n");//outer level
        $this->xmlreply(); // show its all good

    if($this->test)
      throw new Exception("exit with status $xml_status");
    else
      exit;
    }

    function handlews($servicetag)
    {

        $this->set_servicetag($servicetag);
        $this->xmltop();
        $this->xmlbody();
        $this->xmlend("success");

    }
}
?>

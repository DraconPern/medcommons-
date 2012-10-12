
package org.cxp2.soap;


import javax.jws.WebService;
//import javax.jws.soap.SOAPBinding;

import org.cxp2.DeleteResponse;
import org.cxp2.GetResponse;
import org.cxp2.PutResponse;
//@WebService(serviceName = "CXP2", targetNamespace = "http://:cxp2.org")
//@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface CXPService {


    public GetResponse get(
        org.cxp2.GetRequest GetRequest);


    public PutResponse put(
        org.cxp2.PutRequest PutRequest);


    public DeleteResponse delete(
        org.cxp2.DeleteRequest DeleteRequest);

}

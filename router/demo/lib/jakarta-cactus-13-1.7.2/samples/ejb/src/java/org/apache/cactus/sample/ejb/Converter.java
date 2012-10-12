/* 
 * ========================================================================
 * 
 * Copyright 2001-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ========================================================================
 */
package org.apache.cactus.sample.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**
 * Sample EJB interface
 *
 * @version $Id: Converter.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public interface Converter extends EJBObject
{
    /**
     * Convert Yens to US dollars.
     * 
     * @param theYenAmount the amount to convert
     * @return the conversion in US dollars
     * @throws RemoteException in case of error
     */
    double convertYenToDollar(double theYenAmount) 
        throws RemoteException;
}

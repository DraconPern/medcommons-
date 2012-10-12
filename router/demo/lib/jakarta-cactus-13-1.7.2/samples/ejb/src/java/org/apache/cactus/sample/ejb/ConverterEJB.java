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

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Sample EJB bean.
 *
 * @version $Id: ConverterEJB.java 238816 2004-02-29 16:36:46Z vmassol $
 */
public class ConverterEJB implements SessionBean
{
    /**
     * Session bean context
     */
    private SessionContext context;

    /**
     * @see Converter#convertYenToDollar(double)
     */
    public double convertYenToDollar(double theYenAmount)
    {
        return theYenAmount / 100.0;
    }

    /**
     * @see EJB specifications 
     */
    public void ejbCreate() throws CreateException
    {
    }

    /**
     * @see EJB specifications 
     */
    public void setSessionContext(SessionContext theContext)
    {
        this.context = theContext;
    }

    /**
     * @see SessionBean#ejbActivate()
     */
    public void ejbActivate()
    {
    }

    /**
     * @see SessionBean#ejbPassivate()
     */
    public void ejbPassivate()
    {
    }

    /**
     * @see SessionBean#ejbRemove()
     */
    public void ejbRemove()
    {
    }
}

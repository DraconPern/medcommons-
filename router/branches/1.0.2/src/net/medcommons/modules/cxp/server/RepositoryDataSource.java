package net.medcommons.modules.cxp.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;


/**
 * DataSource from Repository
 * <P>
 * 
 * @author mesozoic
 *
 */
public class RepositoryDataSource

    implements DataSource

{

    private String contentType;

    private String name;

   // private byte[] data;

    
    private InputStream inputStream = null;

    

    public RepositoryDataSource(String contentType, String name, InputStream inputStream)

    {
    	this.contentType = contentType;
    	this.name = name;
    	this.inputStream = inputStream;

    }




/*
    public byte[] getData()

    {

        return data;

    }



    public void setData(byte[] data)

    {

        this.data = data;

    }
    */



    public void setContentType(String contentType)

    {

        this.contentType = contentType;

    }



    public void setName(String name)

    {

        this.name = name;

    }



    public String getContentType()

    {

        return contentType;

    }



    public InputStream getInputStream()

        throws IOException

    {

       return(this.inputStream);

    }



    public String getName()

    {

        return name;

    }



    public OutputStream getOutputStream()

        throws IOException

    {

        return null;

    }



}
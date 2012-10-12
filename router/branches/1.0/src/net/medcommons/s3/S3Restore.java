package net.medcommons.s3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

class S3Download {
    String objectName;
    
    boolean done = false;
    boolean busy = false;
    
    S3Download(String objectName) {
        this.objectName = objectName;
    }
    
    boolean matches(String objectName) {
        return objectName.equals(this.objectName);
    }
    
    synchronized void notifyDone() {
        this.done = true;
        notifyAll();
    }
    
    synchronized void waitUntilDone() {
        while (!this.done) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }
    
    synchronized boolean queryWork() {
        boolean result = !this.busy;
        this.busy = true;
        return result;
    }
}

public class S3Restore {
    
    public static final int MAX_CONCURRENT_DOWNLOADS = 4;
    
    S3Download downloads[] = new S3Download[MAX_CONCURRENT_DOWNLOADS];
    
    /* this format must be consistent with backup/cxp_to_s3.py */
    public static final String FILENAME_FORMAT = "%s.%s.tar.bz2.enc.%03d";
    
    /***************
     * Properties...
     */
    
    /** S3 bucket name where backups are stored */
    public String bucket = "backups.medcommons.net";
    
    public String getBucket() {
        return this.bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public S3Client client;
    
    public S3Client getClient() {
        return this.client;
    }
    
    public void setClient(S3Client client) {
        this.client = client;
    }
    
    public File temporaryDirectory = new File(System.getProperty("java.io.tmpdir", "."));
    
    public File getTemporaryDirectory() {
        return this.temporaryDirectory;
    }
    
    public void setTemporaryDirectory(File dir) {
        this.temporaryDirectory = dir;
    }
    
    public File repositoryDirectory = new File(".");
    
    public File getRepositoryDirectory() {
        return this.repositoryDirectory;
    }
    
    public void setRepositoryDirectory(File dir) {
        this.repositoryDirectory = dir;
    }
    /* ...properties
     ***************/
    
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                S3Restore s3 = new S3Restore();
                
                s3.get(args[0], args[1]);
            }
            else {
                System.err.println("java S3Restore {mcid} {guid}");
                System.exit(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(2);
        }
    }
    
    public S3Restore() throws IOException {
        this.client = new S3Client();
    }
    
    public S3Restore(S3Client client) {
        this.client = client;
    }
    
    /**
     * The main entry point to S3Restore.  This is the blocking
     * call, waiting for a download slot to open up (only 4 concurrent
     * downloads), pulling the content from S3, then concatenating,
     * decrypting, uncompressing, the full sequence into the appropriate
     * document repository directory.
     */
    public void get(String storageId, String documentGuid) throws IOException {
        String lockName = storageId + '.' + documentGuid;
        /* 1st step: find an empty download slot, possibly blocking */
        S3Download download = getDownload(lockName);
        Runtime runtime = Runtime.getRuntime();
        
        System.err.println("Here!");
        System.err.println(storageId);
        System.err.println(documentGuid);
        System.err.println(this.bucket);
        
        /* 2nd step: if this slot is already doing work, possibly blocking */
        if (download.queryWork()) {
            try {
                int i = 0;
                File f = null;
                
                for (;;) {
                    String object = String.format(FILENAME_FORMAT,
                                    storageId, documentGuid, i);
                    
                    f = new File(this.temporaryDirectory, object);
                    
                    FileOutputStream out = new FileOutputStream(f);
                    
                    try {
                        if (this.client.get(this.bucket, object, out) != 200)
                            break;
                    } finally {
                        out.close();
                    }
                    
                    i++;
                }
                
                /* all parts of multi-part download are complete */
                f.delete();
                
                System.err.printf("i - %d\n", i);
                
                if (i > 0) {
                    Process process;
                    String args[] = new String[i + 2];    
                    File dir = new File(this.repositoryDirectory, storageId);
                    
                    if (!dir.isDirectory())
                        dir.mkdir();
                    
                    args[0] = "/opt/gateway/bin/mc_restore";
                    args[1] = dir.getAbsolutePath();
                    
                    for (int j = 0; j < i; j++)
                        args[2 + j] = String.format(FILENAME_FORMAT, storageId,
                                        documentGuid, j);
                    
                    /* De-buggery */
                    System.err.println("mc_restore!!");
                    System.err.println(this.temporaryDirectory);
                    System.err.println(this.repositoryDirectory);
                    System.err.printf("i - %d\n", i);
                    for (int j = 0; j < 2+i; j++)
                        System.err.printf("args[%d] - %s\n", j,args[j]);
                    
                    {String cmd = "whoami";
                    Runtime run = Runtime.getRuntime();
                    Process pr = run.exec(cmd);
                    pr.waitFor();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = "";
                    while ((line=buf.readLine())!=null) {
                        System.err.println(line);
                    }}
                    {String cmd = "whereis mc_restore";
                    Runtime run = Runtime.getRuntime();
                    Process pr = run.exec(cmd);
                    pr.waitFor();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = "";
                    while ((line=buf.readLine())!=null) {
                        System.err.println(line);
                    }}	
                    /* De-buggery */
                    
                    process = runtime.exec(args, null, this.temporaryDirectory);
                    
                    try {
                        process.waitFor();
                    } catch (InterruptedException ex) {
                        /* this is why I hate java */
                    }
                }
            } catch (Exception ex) {
                
                ex.printStackTrace(System.err);
            } finally {
                this.markDone(download);
            }
        }
        else
            download.waitUntilDone();
    }
    
    /*
     * First step, find an empty down load slot, looking for
     * a similar down load.
     *
     * If there's a similar down load, we can just wait for
     * that one to complete.
     */
    protected synchronized S3Download getDownload(String lockName) {
        int slot;
        
        for (;;) {
            slot = -1;
            
            for (int i = 0; i < MAX_CONCURRENT_DOWNLOADS; i++) {
                S3Download download = this.downloads[i];
                
                if (download == null)
                    slot = i;
                else if (download.matches(lockName))
                    return download;
            }
            
            if (slot != -1) {
                S3Download download = new S3Download(lockName);
                this.downloads[slot] = download;
                return download;
            }
            
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }
    
    protected synchronized void markDone(S3Download download) {
        for (int i = 0; i < MAX_CONCURRENT_DOWNLOADS; i++)
            if (this.downloads[i] == download) {
                download.notifyDone();
                
                this.downloads[i] = null;
                this.notifyAll();
            }
    }
}


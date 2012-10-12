/**
 * 
 */
package net.medcommons.application.dicomclient

import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import net.medcommons.application.dicomclient.utils.DBimport net.sourceforge.pbeans.Storeimport static net.medcommons.application.dicomclient.utils.ManagedTransaction.*import net.medcommons.application.dicomclient.utils.CxpTransactionimport org.apache.log4j.BasicConfiguratorimport net.medcommons.application.dicomclient.utils.StatusDisplayManagerimport net.medcommons.application.dicomclient.utils.ManagedTransactionimport net.medcommons.application.dicomclient.utils.DicomOutputTransactionimport java.util.logging.Levelimport java.util.logging.Logger

/**
 * @author ssadedin
 */
public class DownloadCCRTest {
    
    static {
        BasicConfigurator.configure()
	    StatusDisplayManager.testMode = true
	    Logger.getLogger("net.sourceforge.pbeans").level = Level.WARNING
     }
    
    def cxptx = [ patientName: "Jane Hernandez",
                  displayName: "JaneH",
                  nSeries: 1,
                  totalImages: 1,
                  status: STATUS_QUEUED,
                  transactionType: "GET"]
    
    def dicomOutTx = [ retryCount: 0,
                       status: ManagedTransaction.STATUS_QUEUED
                     ]
    Store s
    
    
    @BeforeClass
    static void setUpBeforeClass() throws Exception{
    }
    
    @Before
    void setUp() throws Exception {
	    DB.testMode()
	    s = DB.get()
    }
    
    def tx
    def doTx
    void fixture() {
        tx = new CxpTransaction(cxptx)
        s.insert(tx)
        
        doTx = new DicomOutputTransaction(dicomOutTx)
        doTx.cxpJob = tx.id
        s.insert(doTx)
    }
    
    @Test
    void testRun(){
        fixture()
        def executed = false
        def dlCCR = [ executeJob: { xtx -> executed=xtx }] as DownloadCCR
        def t = new Thread({  dlCCR.run()  })
        t.start()
        Thread.sleep(3000)
        dlCCR.running = false
        t.interrupt()
        t.join()
        
        assert executed.id == tx.id
    }
    
    /**
     * If no jobs in the queue should not call executeJob
     */
    @Test
    void testRunNoJobs() {
        def executed = false
        def dlCCR = [ executeJob: { xtx -> executed=xtx }] as DownloadCCR
        def t = new Thread({ dlCCR.run() })
        t.start()
        Thread.sleep(3000)
        dlCCR.running = false
        t.interrupt()
        t.join()
        
        assert executed == false
    }
    
    @Test
    void testExecuteJob() {
        fixture()
        
        def ran = false
        DownloadCCR dlCCR = 
            [ 
              createCxpJob: { cxpTx,outTx-> [ run: {ran = true} ] as DownloadCxpJob } 
            ] as DownloadCCR
            
        dlCCR.jobHandler = [ addCxpJob: {job->}, deleteCxpJob: { job-> }] as JobHandler
        dlCCR.executeJob(tx)
        
        assert ran == true
    }
    
}

package net.medcommons.router.services.ccrmerge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.medcommons.router.services.ccrmerge.preprocess.MergeConstants;
import net.medcommons.router.services.xds.consumer.web.action.CCRDocument;
import net.medcommons.router.util.BaseTestCase;

/**
 * Basic idea of test:
 * - Take all CCRs from AAFP web site.
 * - Make sure that the contents are valid (many are not).
 * - Merge them all into each other.
 * This isn't a subtle test of merging. It's just a gross test
 * for integrating CCRs from different vendors who make different 
 * assumptions about what's in a CCR.
 * @author mesozoic
 *
 */
public class MonsterMergeTest  extends BaseTestCase  implements MergeConstants{
	/**
	 * Logger to use with this class
	 */
	private static Logger log = Logger.getLogger(MonsterMergeTest.class);
	
	public MonsterMergeTest() throws Exception{
		super();
	}
	public void testMerge(){
		
	}
	private File getInputDirectory() throws IOException{
		File testsDir = new File("tests");
		if (!testsDir.exists())
			throw new FileNotFoundException("tests directory missing");
		File rootDir = new File(testsDir, "SampleCCRSfromAAFP");
		if (!rootDir.exists()){
			throw new FileNotFoundException("SampleCCRSfromAAFP directory missing");
		}
		return(rootDir);
	}
	
	private List<File> getCCRs(File directory){
		FilenameFilter filter = new FilenameFilter() {
			 
            public boolean accept(File dir, String name) {
                if(name.endsWith(".xml"))
                    return true;               
                else return false;
            }
        };
		ArrayList<File> ccrFiles = new ArrayList<File>();
		
		File[] directories = directory.listFiles();
		for (int i = 0;i<directories.length; i++){
			File[] xmlFiles = directories[i].listFiles(filter);
			if (xmlFiles != null){
				for (int j =0; j<xmlFiles.length; j++){
					ccrFiles.add(xmlFiles[j]);
				}
			}
		}
		return(ccrFiles);
		
	}
	public void testMergeAll() throws Exception{
		List<File> ccrs = getCCRs(getInputDirectory());
		
		File initial = ccrs.get(0);
		CCRDocument currentCCR = CCRUtils.loadCCR(initial.getAbsolutePath());
		
		for (int i=1;i<ccrs.size(); i++){
			CCRDocument incomingCCR = CCRUtils.loadCCR(ccrs.get(i).getAbsolutePath());
			log.info("About to merge in " + ccrs.get(i).getAbsolutePath());
			File merged = CCRUtils.mergeCCR(MergerFactory.class,"MergeIteration" + i , currentCCR, incomingCCR);
			
			log.info("Completed merging " + ccrs.get(i).getAbsolutePath() 
					+ "\n about to load merged data " + merged.getAbsolutePath());
			currentCCR = CCRUtils.loadCCR(merged.getAbsolutePath());	
			
			
			 
		}
		ElementCount[] finalCount = CCRUtils.generateElementCounts(currentCCR);
		CCRUtils.printElementCounts("Monster Merge", finalCount);
	}
	
}

package com.legstar.pdi.zosfile;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import junit.framework.TestCase;

/**
 * Tests to run after the package was deployed.
 *
 */
public class ZosFileITCase extends TestCase {
    
    public void setUp() throws Exception {
        System.setProperty("KETTLE_PLUGIN_BASE_FOLDERS",
                Const.DEFAULT_PLUGIN_BASE_FOLDERS + ",target/plugins");
        KettleEnvironment.init();
    }
    
    /**
     * A variable file with no RDW.
     * @throws Exception if test fails
     */
    public void testVariableNoRDW() throws Exception {
        runTrans("src/test/resources/variable.ktr");
    }
    
    /**
     * A variable file with RDW.
     * @throws Exception if test fails
     */
    public void testVariableRDW() throws Exception {
        runTrans("src/test/resources/variable-rdw.ktr");
    }
    
    /**
     * Generic execution of a transformation from a kettle XML description. 
     * @param fileName the kettle transformation description (ktr file)
     * @throws Exception if execution fails
     */
    protected void runTrans(final String fileName) throws Exception {
        TransMeta transMeta = new TransMeta(fileName);
        Trans trans = new Trans(transMeta);
        trans.setLogLevel(LogLevel.DEBUG);
        trans.initializeVariablesFrom(null);
        trans.getTransMeta().setInternalKettleVariables(trans);
        
        trans.setSafeModeEnabled(true);
        
        // see if the transformation checks ok
        List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
        trans.getTransMeta().checkSteps(remarks, false, null);
        for( CheckResultInterface remark : remarks ) {
            if( remark.getType() == CheckResultInterface.TYPE_RESULT_ERROR ) {
                fail("Check error: " + fileName + ", "+remark.getErrorCode());
            }
        }
        
        // execute transformation
        trans.execute(null); 

        trans.waitUntilFinished();
        Result result = trans.getResult();
        assertEquals(0, result.getNrErrors());
    }

}

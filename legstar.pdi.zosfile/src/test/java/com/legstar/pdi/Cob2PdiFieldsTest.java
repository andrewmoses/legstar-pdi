package com.legstar.pdi;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.io.FileUtils;

/**
 * Tests for CobolToPdi class.
 * 
 */
public class Cob2PdiFieldsTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    /** Current context class loader. */
    private ClassLoader _tccl;

    /**
     * Put a set of test case jars onto the context class loader.
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        setCreateReferences(CREATE_REFERENCES);
        FileUtils.forceMkdir(GEN_PDISCHEMA_DIR);
        FileUtils.cleanDirectory(GEN_PDISCHEMA_DIR);

        _tccl = Thread.currentThread().getContextClassLoader();
        String[] jarFileNames = CobolToPdi.getClasspath(
                "src/test/resources/jars").split(";");
        URL[] jarFileUrls = new URL[jarFileNames.length];
        for (int i = 0; i < jarFileNames.length; i++) {
            jarFileUrls[i] = new File(jarFileNames[i]).toURI().toURL();
        }
        URLClassLoader cl = new URLClassLoader(jarFileUrls, _tccl);
        Thread.currentThread().setContextClassLoader(cl);
    }

    public void tearDown() {
        Thread.currentThread().setContextClassLoader(_tccl);
    }

    /**
     * Test flat01.
     * 
     * @throws Exception
     */
    public void testFlat01() throws Exception {

        List<CobolFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.flat01cc.Flat01Record");
        check("flat01", "json", fields.toString());

    }

    /**
     * Test flat02.
     * 
     * @throws Exception
     */
    public void testFlat02() throws Exception {

        List<CobolFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.flat02cc.Flat02Record");
        check("flat02", "json", fields.toString());

    }

    /**
     * Test stru03.
     * 
     * @throws Exception
     */
    public void testStru03() throws Exception {

        List<CobolFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru03cc.Stru03Record");
        check("stru03", "json", fields.toString());

    }

    /**
     * Test stru04.
     * 
     * @throws Exception
     */
    public void testStru04() throws Exception {

        List<CobolFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru04cc.Stru04Record");
        check("stru04", "json", fields.toString());

    }

    /**
     * Test stru05 (with name conflicts).
     * 
     * @throws Exception
     */
    public void testStru05() throws Exception {

        List<CobolFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru05cc.Stru05Record");
        check("stru05", "json", fields.toString());

    }
}

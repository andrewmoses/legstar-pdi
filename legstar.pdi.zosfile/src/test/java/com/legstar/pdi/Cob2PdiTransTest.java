package com.legstar.pdi;

import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

import com.legstar.coxb.cob2trans.Cob2TransGenerator.Cob2TransResult;

/**
 * Test embedded COB Transformers generation.
 */
public class Cob2PdiTransTest extends AbstractTest {

    /**
     * Fake a plugin location with a conf folder.
     */
    public void setUp() {
        System.setProperty(CobolToPdi.PLUGIN_FOLDER_PROPERTY,
                "src/main/resources");
    }

    /**
     * Generate and bundle a Tranformer.
     * 
     * @throws Exception if test fails
     */
    public void testDefaults() throws Exception {
        Cob2TransResult results = Cob2PdiTrans
                .generateTransformer(null, "flat01", FileUtils
                        .readFileToString(new File(COPYBOOKS_DIR, "FLAT01CC")),
                        COPYBOOKS_ENC, "FLAT01CC", CobolToPdi
                                .getClasspath("target/dependency"));
        assertNotNull(results.jarFile.getPath());
        JarFile jarFile = new JarFile(results.jarFile);
        JarEntry jarEntry = jarFile
                .getJarEntry("flat01/flat01cc/bind/Flat01RecordHostToJavaTransformer.class");
        assertEquals(
                "flat01/flat01cc/bind/Flat01RecordHostToJavaTransformer.class",
                jarEntry.getName());
    }

}

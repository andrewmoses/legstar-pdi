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

    /**
     * Test that we can generate valid package names.
     * 
     * @throws Exception if something goes wrong
     */
    public void testPackageName() throws Exception {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e",
                Cob2PdiTrans.getPackageName(null, "", "ISO-8859-1", null));
        assertEquals("z_002fosfileinput.d41d8cd98f00b204e9800998ecf8427e",
                Cob2PdiTrans.getPackageName("z/OS File Input", "",
                        "ISO-8859-1", null));
        assertEquals("_1my_0025020cobol", Cob2PdiTrans.getPackageName(null,
                null, null, "1my%020cobol.cbl"));
        assertEquals("z_002fosfileinput1._1my_0025020cobol",
                Cob2PdiTrans.getPackageName("z/OS File Input 1", null, null,
                        "1my%020cobol.cbl"));

    }

}

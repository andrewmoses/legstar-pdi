package com.legstar.pdi.io;

import java.io.File;

import com.legstar.coxb.host.HostData;

import junit.framework.TestCase;

/**
 * Test the ZosVariableRDWFileInputStream class.
 *
 */
public class ZosVariableRDWFileInputStreamTest extends TestCase {
    
    /**
     * Check behavior when no RDW is present.
     * 
     * @throws Exception if test fails
     */
    public void testNoRDW() throws Exception {
        ZosVariableRDWFileInputStream fs = new ZosVariableRDWFileInputStream(
                new File(
                        "src/test/resources/ZOS.TCOBWVB.ROW1.bin"));
        byte[] buffer = new byte[4];

        try {
            fs.read(buffer);
            fail();
        } catch (Exception e) {
            assertEquals(
                    "Record length extracted from RDW larger than maximum record length",
                    e.getMessage());
        }

        fs.close();

    }
    
    /**
     * Test with an actual RDW.
     * @throws Exception if test fails
     */
    public void testRDW() throws Exception {
        ZosVariableRDWFileInputStream fs = new ZosVariableRDWFileInputStream(
                new File(
                        "src/test/resources/ZOS.TCOBWVB.RDW.ROW1.bin"));
        byte[] buffer = new byte[183];
        int available = fs.read(buffer);
        assertEquals(108, available);
        assertEquals(
                "f0f0f0f0f0f1"
                + "d1d6c8d540e2d4c9e3c840404040404040404040"
                + "c3c1d4c2d9c9c4c7c540e4d5c9e5c5d9e2c9e3e8"
                + "f4f4f0f1f2f5f6f5"
                + "00000002"
                + "f1f061f0f461f1f1"
                + "000000000023556c"
                + "5c5c5c5c5c5c5c5c5c"
                + "f1f061f0f461f1f1"
                + "000000000023556c"
                + "5c5c5c5c5c5c5c5c5c",
                HostData.toHexString(buffer, 0, available));

        fs.close();
   }

}

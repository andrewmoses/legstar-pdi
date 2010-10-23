package com.legstar.pdi.io;

import java.io.File;

import com.legstar.coxb.host.HostData;
import com.legstar.pdi.io.ZosVariableFileInputStream;

import junit.framework.TestCase;

/**
 * Test the ZosFileInputStream class.
 *
 */
public class ZosVariableFileInputStreamTest extends TestCase {
    
    public void test1() throws Exception {
        ZosVariableFileInputStream fs = new ZosVariableFileInputStream(new File(
                "src/test/resources/ZOS.TCOBWVB.ROW1.bin"));
        byte[] buffer = new byte[4];
        
        // First time, buffer must be filled
        int available = fs.read(buffer);
        assertEquals(4, available);
        assertEquals("f0f0f0f0", HostData.toHexString(buffer));
        
        // Read again telling the stream we processed everything
        available = fs.read(buffer, available);
        assertEquals(4, available);
        assertEquals("f0f1d1d6", HostData.toHexString(buffer));
        
        // Read again telling the stream we processed less and less
        available = fs.read(buffer, available - 1);
        assertEquals(4, available);
        assertEquals("d6c8d540", HostData.toHexString(buffer));

        available = fs.read(buffer, available - 2);
        assertEquals(4, available);
        assertEquals("d540e2d4", HostData.toHexString(buffer));

        available = fs.read(buffer, available - 3);
        assertEquals(4, available);
        assertEquals("40e2d4c9", HostData.toHexString(buffer));
        
        // This is special, nothing was processed so we get the
        // same data
        available = fs.read(buffer, available - 4);
        assertEquals(4, available);
        assertEquals("40e2d4c9", HostData.toHexString(buffer));
        
        // Try again after the residual was consumed
        available = fs.read(buffer, available);
        assertEquals(4, available);
        assertEquals("e3c84040", HostData.toHexString(buffer));
       
        available = fs.read(buffer, available - 4);
        assertEquals(4, available);
        assertEquals("e3c84040", HostData.toHexString(buffer));

        available = fs.read(buffer, available - 4);
        assertEquals(4, available);
        assertEquals("e3c84040", HostData.toHexString(buffer));

        // Now consume the rest till we near the end of the file
        available = fs.read(buffer, available);
        assertEquals(4, available);
        assertEquals("40404040", HostData.toHexString(buffer));
        
        while(available == 4) {
            available = fs.read(buffer, available);
        }
        assertEquals(2, available);
        assertEquals("5c5c5c5c", HostData.toHexString(buffer));
        
        // The file is exhausted, we should still be able to get
        // the residue
        available = fs.read(buffer, 1);
        assertEquals(1, available);
        assertEquals("5c5c5c5c", HostData.toHexString(buffer));

        available = fs.read(buffer, 1);
        assertEquals(-1, available);
        assertEquals("5c5c5c5c", HostData.toHexString(buffer));

        fs.close();
    }

}

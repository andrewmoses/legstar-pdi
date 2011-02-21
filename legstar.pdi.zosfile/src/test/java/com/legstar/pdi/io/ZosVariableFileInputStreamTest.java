package com.legstar.pdi.io;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.legstar.coxb.host.HostData;

/**
 * Test the ZosFileInputStream class.
 * 
 */
public class ZosVariableFileInputStreamTest extends TestCase {

    /** File stream under test. */
    private ZosVariableFileInputStream _zfis;

    /** Used as input for testing. */
    private File _tempFile;

    /** {@inheritDoc} */
    public void setUp() throws Exception {
        _tempFile = File.createTempFile("legstar-tos", ".tmp");
        _tempFile.deleteOnExit();
        _zfis = new ZosVariableFileInputStream(_tempFile);
    }

    /** {@inheritDoc} */
    public void tearDown() throws Exception {
        _zfis.close();
    }

    /**
     * Read an empty variable record file.
     * 
     * @throws Exception if test fails
     */
    public void testEmptyVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(-1, n);
    }

    /**
     * Read with a buffer larger than file.
     * 
     * @throws Exception if test fails
     */
    public void testLargerVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "abc");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(3, n);
        assertEquals("abc", new String(b, 0, n));
        n = _zfis.read(b, 3);
        assertEquals(-1, n);

    }

    /**
     * Read a sequence of same size records.
     * 
     * @throws Exception if test fails
     */
    public void testFixedVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "abcdefgh");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(4, n);
        assertEquals("abcd", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(4, n);
        assertEquals("efgh", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(-1, n);
    }

    /**
     * Process less bytes than read.
     * 
     * @throws Exception if test fails
     */
    public void testSmallerVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "abcdefgh");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(4, n);
        assertEquals("abcd", new String(b, 0, n));
        n = _zfis.read(b, 3); // We processed only 3 bytes
        assertEquals(4, n);
        assertEquals("defg", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(1, n); // There is one byte left
        assertEquals("h", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(-1, n);
    }

    /**
     * Process no bytes at all.
     * 
     * @throws Exception if test fails
     */
    public void testNoBytesVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "abcdefgh");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(4, n);
        assertEquals("abcd", new String(b, 0, n));
        n = _zfis.read(b, 0); // We processed nothing
        assertEquals(4, n);
        assertEquals("abcd", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(4, n);
        assertEquals("efgh", new String(b, 0, n));
        n = _zfis.read(b, n);
        assertEquals(-1, n);

    }

    /**
     * Process bytes slower than reads.
     * 
     * @throws Exception if test fails
     */
    public void testSlowerVariableRecord() throws Exception {
        FileUtils.writeStringToFile(_tempFile, "abcdefgh");
        byte[] b = new byte[4];
        int n = _zfis.read(b, 0);
        assertEquals(4, n);
        assertEquals("abcd", new String(b, 0, n));
        n = _zfis.read(b, 4);
        assertEquals(4, n);
        assertEquals("efgh", new String(b, 0, n));
        n = _zfis.read(b, 3);
        assertEquals(1, n);
        assertEquals("h", new String(b, 0, n));
        n = _zfis.read(b, 1);
        assertEquals(-1, n);

    }

    public void test1() throws Exception {
        ZosVariableFileInputStream fs = new ZosVariableFileInputStream(
                new File("src/test/resources/ZOS.TCOBWVB.ROW1.bin"));
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

        while (available == 4) {
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

package com.legstar.pdi.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Reading a z/OS file which records are fixed length.
 * <p/>
 * Fixed size records are treated very simply, the buffer size being passed
 * on read calls is assumed to have the exact size of the record.
 * 
 */
public class ZosFixedFileInputStream extends FileInputStream implements
        ZosFileInputStream {

    /**
     * Create a z/OS file stream.
     * 
     * @param arg0 the underlying file name
     * @throws FileNotFoundException if file cannot be located
     */
    public ZosFixedFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Reads bytes from the underlying file.
     * 
     * @param b the buffer which is expected to have the exact size of the record
     * @return the number of bytes available to process (-1 if file exhausted)
     * @throws IOException if reading fails
     */
    public int read(byte[] b) throws IOException {
        return super.read(b);
    }

    /**
     * Reads bytes from the underlying file.
     * <p/>
     * This method ignores the number of bytes processed since the 
     * buffer size allows us to know the record length.
     * 
     * @param b the buffer which is expected to have the exact size of the record
     * @param processed this parameter value is ignored.
     * @return the number of bytes available to process (-1 if file exhausted)
     * @throws IOException if reading fails
     */
    public int read(byte[] b, int processed) throws IOException {
        return read(b);
    }

}

package com.legstar.pdi;

import java.io.File;
import java.io.FileNotFoundException;

import com.legstar.pdi.io.ZosFileInputStream;
import com.legstar.pdi.io.ZosFixedFileInputStream;
import com.legstar.pdi.io.ZosVariableFileInputStream;
import com.legstar.pdi.io.ZosVariableRDWFileInputStream;
import com.legstar.pdi.zosfile.ZosFileInputMeta;

/**
 * A utility class to determine the type of file io needed based
 * upon the user options selected in PDI.
 * 
 */
public class ZosFileInputStreamFactory {
    
    
    /**
     * A utility class.
     */
    private ZosFileInputStreamFactory() {
        
    }
    
    /**
     * The z/OS file can be fixed length or variable length. Furthermore,
     * variable length files can have RDW prefixes or not.
     * 
     * @param meta the options selected
     * @param file the underlying z/OS file
     * @return a stream reader appropriate for the type of z/OS records in the
     *         file
     * @throws FileNotFoundException if underlying file not found
     */
    public static ZosFileInputStream create(final ZosFileInputMeta meta,
            final File file) throws FileNotFoundException {

        if (meta.isVariableLength()) {
            if (meta.hasRecordDescriptorWord()) {
                return new ZosVariableRDWFileInputStream(file);
            } else {
                return new ZosVariableFileInputStream(file);
            }
        } else {
            return new ZosFixedFileInputStream(file);
        }
    }

}

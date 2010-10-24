package com.legstar.pdi.sample.tcobwvb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.legstar.coxb.convert.CobolConversionException;
import com.legstar.coxb.convert.simple.CobolBinarySimpleConverter;
import com.legstar.coxb.transform.HostTransformException;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.test.coxb.tcobwvb.CustomerData;
import com.legstar.test.coxb.tcobwvb.Transaction;
import com.legstar.test.coxb.tcobwvb.bind.CustomerDataTransformers;

import junit.framework.TestCase;

/**
 * Test that the sample is correct.
 */
public class SampleTest extends TestCase {

    /** For the output file. */
	public static final String LS = System.getProperty("line.separator");
	
	/** Column separator for CSV files produced. */
	public static final String CS = ";";
	
	/** Location of our sample files. */
	private static final File SAMPLE_FILES_FOLDER = new File("src/main/files");
	
    /** Location of the reference CSV files we generate. */
    private static final File REFERENCE_CSV_FOLDER = new File("src/test/resources");

    /** Location where the actual CSV files are generated. */
    private static final File ACTUAL_CSV_FOLDER = new File("target/csv");

    /** The semi-column separated output file header. */
    private static final String CSV_HEADER = "ID" + CS + "NAME" + CS
            + "ADDRESS" + CS + "PHONE" + CS + "TNBR"
                    + CS + "DATE_0" + CS + "AMOUNT_0" + CS + "COMMENT_0"
                    + CS + "DATE_1" + CS + "AMOUNT_1" + CS + "COMMENT_1"
                    + CS + "DATE_2" + CS + "AMOUNT_2" + CS + "COMMENT_2"
                    + CS + "DATE_3" + CS + "AMOUNT_3" + CS + "COMMENT_3"
                    + CS + "DATE_4" + CS + "AMOUNT_4" + CS + "COMMENT_4";
	
	/** The legstar transformer used for a record.*/
	private CustomerDataTransformers _tf;
	
	/** Byte buffer used to read host data.*/
	private byte[] _hostRecord;
	
	/** The maximum size of the host data to fit in hostRecord. */
	private int _maxLength;
	
	/** {@inheritDoc}*/
	public void setUp() throws Exception {
		_tf = new CustomerDataTransformers();
		_maxLength = _tf.getHostToJava().getBinding().getByteLength();
		assertEquals(183, _maxLength);
		_hostRecord = new byte[_maxLength];
		FileUtils.forceMkdir(ACTUAL_CSV_FOLDER);
	}

	/**
	 * Test the file with no RDW.
	 */
	public void testSampleWithoutRDW() throws Exception {

		FileInputStream fs = null;
		try {
            String fileName = "ZOS.TCOBWVB";
            fs = new FileInputStream(new File(SAMPLE_FILES_FOLDER, fileName
                    + ".bin"));

            FileWriter fw = new FileWriter(new File(ACTUAL_CSV_FOLDER, fileName
                    + ".csv"));
            fw.write(CSV_HEADER);
            fw.write(LS);

			int count = fs.read(_hostRecord);
			int length;
			int residual = 0;
			HostTransformStatus status = new HostTransformStatus();

			while (count > 0 || residual > 0) {
				CustomerData javaRecord = _tf.toJava(_hostRecord, status);
				length = status.getHostBytesProcessed();
				
				outputCsvRecord(fw, javaRecord);
				
				/* We might have data read in excess that we still need to process */
				residual = residual + count - length;
				if (residual > 0) {
					System.arraycopy(_hostRecord, length, _hostRecord, 0, residual);
				} else if (residual == -1) {
					break;
				}
				
				count = fs.read(_hostRecord, residual, _maxLength - residual);
			}
			fw.close();
			assertFileContent(fileName);

		} catch (IOException e) {
			e.printStackTrace();
            fail(e.getMessage());
		} catch (HostTransformException e) {
			e.printStackTrace();
            fail(e.getMessage());
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
		            fail(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Test the file with  RDW.
	 */
	public void testSampleWithDRW() throws Exception {
		FileInputStream fs = null;
		try {

            String fileName = "ZOS.TCOBWVB.RDW";
            fs = new FileInputStream(new File(SAMPLE_FILES_FOLDER, fileName
                    + ".bin"));

            FileWriter fw = new FileWriter(new File(ACTUAL_CSV_FOLDER, fileName
                    + ".csv"));
			fw.write(CSV_HEADER);
			fw.write(LS);

			int count = readWithRDW(fs);

			while (count > 0 ) {
				CustomerData javaRecord = _tf.toJava(_hostRecord);
				
				outputCsvRecord(fw, javaRecord);
				
				count = readWithRDW(fs);
			}
			fw.close();
            assertFileContent(fileName);

		} catch (IOException e) {
			e.printStackTrace();
            fail(e.getMessage());
		} catch (HostTransformException e) {
			e.printStackTrace();
            fail(e.getMessage());
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
		            fail(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Read variable records which are known to start with a
	 * Record Descriptor Word
	 * @param fs the file to read from
	 * @return the count of characters actually read (excluding the RDW itself)
	 * @throws IOException if the RDW is invalid
	 */
	protected int readWithRDW(FileInputStream fs) throws IOException {
		byte[] RDW = new byte[4];
		int count = fs.read(RDW);
		if (count == -1) {
			return count;
		}
		if (count < RDW.length) {
			throw new IOException("Record does not start with an RDW");
		}
		try {
			int rdw = CobolBinarySimpleConverter.fromHostSingle(2, false, 4, 0,
					RDW, 0).intValue();
			if (rdw > 0) {
				/* Beware that raw rdw accounts for the rdw length (4 bytes)*/
				rdw -= RDW.length;
				if (rdw > _maxLength) {
					throw new IOException(
							"Record length extracted from RDW larger than maximum record length");
				}
				return fs.read(_hostRecord, 0, rdw);
			} else {
				return 0;
			}
		} catch (CobolConversionException e) {
			throw new IOException("Unable to translate RDW content");
		}
	}
	
	/**
	 * Produce a CSV record.
	 * @param fw a writer
	 * @param javaRecord the transformed data
	 * @throws Exception if writing fails
	 */
	protected void outputCsvRecord(FileWriter fw, CustomerData javaRecord)
			throws Exception {
		fw.write(Long.toString(javaRecord.getCustomerId()));
		fw.write(CS);
		fw.write(javaRecord.getPersonalData().getCustomerName());
		fw.write(CS);
		fw.write(javaRecord.getPersonalData().getCustomerAddress());
		fw.write(CS);
		fw.write(javaRecord.getPersonalData().getCustomerPhone());
		fw.write(CS);
		fw.write(Long
				.toString(javaRecord.getTransactions().getTransactionNbr()));
		for (Transaction transaction : javaRecord.getTransactions()
				.getTransaction()) {
			fw.write(CS);
			fw.write(transaction.getTransactionDate());
			fw.write(CS);
			fw.write(transaction.getTransactionAmount().toString());
			fw.write(CS);
			fw.write(transaction.getTransactionComment());
		}
		fw.write(LS);

	}
	
    /**
     * A primitive file content comparator.
     * @param expectedFileName the expected file name
     * @param actualFileName the actual file name
     */
    protected void assertFileContent(String fileName) {
        try {
            String expected = FileUtils.readFileToString(new File(
                    REFERENCE_CSV_FOLDER, fileName + ".csv"));
            String actual = FileUtils.readFileToString(new File(
                    ACTUAL_CSV_FOLDER, fileName + ".csv"));
            assertEquals(expected, actual);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}

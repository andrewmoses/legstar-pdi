package com.legstar.pdi.zosfile;

import java.math.BigDecimal;
import java.util.List;


import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.TransformationTestCase;

import com.legstar.pdi.CobolToPdi;
import com.legstar.pdi.zosfile.ZosFileInputMeta;

/**
 * Test the step execution headless (without the dialog).
 *
 */
public class ZosFileInputTest extends TransformationTestCase {

	public ZosFileInputTest() throws KettleException {
		super();
	}
	
	/**
	 * Initialize meta data as the dialog would have done and then run the step.
	 * The PDI test framework automatically creates a transformation where our
	 * step is followed by a dummy step.
	 * @throws Exception if execution fails
	 */
	public void testCustomerData() throws Exception {
        String stepName = "zosfileinput";
        ZosFileInputMeta zosFileInputMeta = new ZosFileInputMeta();

        zosFileInputMeta
                .setJaxbQualifiedClassName("com.legstar.test.coxb.tcobwvb.CustomerData");
        zosFileInputMeta
                .setFilename("src/test/resources/ZOS.TCOBWVB.ROW1.bin");

		zosFileInputMeta.setInputFields(CobolToPdi.toFieldArray(
		        zosFileInputMeta.getJaxbQualifiedClassName()));

		TransMeta transMeta = TransTestFactory.generateTestTransformation(
				new Variables(), zosFileInputMeta, stepName);

		List<RowMetaAndData> result = TransTestFactory
				.executeTestTransformation(transMeta,
						TransTestFactory.INJECTOR_STEPNAME, stepName,
						TransTestFactory.DUMMY_STEPNAME, createSourceData());

		checkRows(createResultData(), result);
	}
	
	/**
	 * The PDI test framework has an injector step before our own.
	 * @return row meta and data for the injector step
	 */
	public List<RowMetaAndData> createSourceData() {
		return createData(createSourceRowMetaInterface(), new Object[][] {
				new Object[] { "abc" } });
	}

    /**
     * This is the meta data for the injector step.
     * @return meta data for the injector
     */
    public RowMetaInterface createSourceRowMetaInterface() {
        return createRowMetaInterface(new ValueMeta("field1",
                ValueMeta.TYPE_STRING));
    }

	/**
	 * Create a row of expected results.
	 * @return the expected row and data results
	 */
	public List<RowMetaAndData> createResultData() {
		return createData(createResultRowMetaInterface(), new Object[][] {
				new Object[] { new Long(1),
						"JOHN SMITH",
						"CAMBRIDGE UNIVERSITY",
						"44012565",
						new Long(2),
						"10/04/11",
						new BigDecimal("235.56"),
						"*********",
                        "10/04/11",
                        new BigDecimal("235.56"),
                        "*********"} });
	}

	/**
	 * Create the expected meta data.
	 * @return the expected meta data
	 */
	public RowMetaInterface createResultRowMetaInterface() {
		RowMetaInterface rm = createRowMetaInterface(
				new ValueMeta("CustomerId",	ValueMeta.TYPE_INTEGER),
				new ValueMeta("PersonalData_CustomerName",	ValueMeta.TYPE_STRING),
				new ValueMeta("PersonalData_CustomerAddress",	ValueMeta.TYPE_STRING),
				new ValueMeta("PersonalData_CustomerPhone",	ValueMeta.TYPE_STRING),
				new ValueMeta("Transactions_TransactionNbr",	ValueMeta.TYPE_INTEGER),
				new ValueMeta("Transactions_Transaction_LastTransDateChoice_LastTransDate_0",	ValueMeta.TYPE_STRING),
				new ValueMeta("Transactions_Transaction_LastTransAmount_0",	ValueMeta.TYPE_BIGNUMBER),
				new ValueMeta("Transactions_Transaction_LastTransComment_0",	ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransDateChoice_LastTransDate_1",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransAmount_1",    ValueMeta.TYPE_BIGNUMBER),
                new ValueMeta("Transactions_Transaction_LastTransComment_1",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransDateChoice_LastTransDate_2",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransAmount_2",    ValueMeta.TYPE_BIGNUMBER),
                new ValueMeta("Transactions_Transaction_LastTransComment_2",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransDateChoice_LastTransDate_3",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransAmount_3",    ValueMeta.TYPE_BIGNUMBER),
                new ValueMeta("Transactions_Transaction_LastTransComment_3",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransDateChoice_LastTransDate_4",    ValueMeta.TYPE_STRING),
                new ValueMeta("Transactions_Transaction_LastTransAmount_4",    ValueMeta.TYPE_BIGNUMBER),
                new ValueMeta("Transactions_Transaction_LastTransComment_4",    ValueMeta.TYPE_STRING)
				);
		return rm;
	}
}

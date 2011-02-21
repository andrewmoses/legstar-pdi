package com.legstar.pdi.zosfile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.TransformationTestCase;

import com.legstar.coxb.host.HostData;
import com.legstar.pdi.Cob2Pdi;
import com.legstar.pdi.Cob2PdiFields;
import com.legstar.pdi.CobFileInputField;

/**
 * Test the step execution headless (without the dialog).
 * <p/>
 * Initialize meta data as the dialog would have done and then run the step. The
 * PDI test framework automatically creates a transformation where our step is
 * followed by a dummy step.
 */
public class ZosFileInputTest extends TransformationTestCase {

    /** {@inheritDoc} */
    public void setUp() throws KettleException {
        /* Tells our code where to look for the plugin folder tree. */
        System.setProperty(Cob2Pdi.PLUGIN_FOLDER_PROPERTY, "src/test/resources");
    }

    public ZosFileInputTest() throws KettleException {
        super();
    }

    public ZosFileInputTest(String name) throws KettleException {
        super(name);
    }

    /**
     * Test FLAT01.
     * 
     * @throws Exception if execution fails
     */
    public void testFlat01() throws Exception {
        transform("zosfileinput",
                "com.legstar.test.coxb.flat01cc.Flat01Record[FLAT01CC.jar]",
                "F0F0F0F0F4F3D5C1D4C5F0F0F0F0F4F3404040404040404040400215000F",
                new Object[][] { { new Long(43), "NAME000043",
                        new BigDecimal("2150.00") } });
    }

    /**
     * Test CUSDATCC.
     * 
     * @throws Exception if execution fails
     */
    public void testCustomerData() throws Exception {
        transform(
                "zosfileinput",
                "com.legstar.test.coxb.cusdatcc.CustomerData[CUSDATCC.jar]",
                "F0F0F0F0F0F1D1D6C8D540E2D4C9E3C840404040404040404040C3C1D4C2D9C9C4C7C540E4D5C9E5C5D9E2C9E3E8F4F4F0F1F2F5F6F500000002F1F061F0F461F1F1000000000023556C5C5C5C5C5C5C5C5C5CF1F061F0F461F1F1000000000023556C5C5C5C5C5C5C5C5C5C",
                new Object[][] { { new Long(1), "JOHN SMITH",
                        "CAMBRIDGE UNIVERSITY", "44012565", new Long(2),
                        "10/04/11", new BigDecimal("235.56"), "*********",
                        "10/04/11", new BigDecimal("235.56"), "*********" } });
    }

    /**
     * Test RDEF01.
     * 
     * @throws Exception if execution fails
     */
    public void testRdef01() throws Exception {
        transform("zosfileinput",
                "com.legstar.test.coxb.rdef01cc.Rdef01Record[RDEF01CC.jar]",
                "00010250000F40404040404000010260000F404040404040",
                new Object[][] { { new Long("1"), new BigDecimal("2500.00") },
                        { new Long("1"), new BigDecimal("2600.00") } });
    }

    /**
     * Initialize meta data as the dialog would have done and then run the step.
     * The PDI test framework automatically creates a transformation where our
     * step is followed by a dummy step.
     * 
     * @throws Exception if execution fails
     */
    protected void transform(final String stepName,
            final String compositeJaxbClassName, final String hexRecord,
            final Object[][] resultData) throws Exception {
        ZosFileInputMeta zosFileInputMeta = new ZosFileInputMeta();

        zosFileInputMeta.setCompositeJaxbClassName(compositeJaxbClassName);
        zosFileInputMeta.setFilename(writeToTempBinFile(hexRecord));

        zosFileInputMeta.setInputFields(Cob2PdiFields.getCobolFields(
                zosFileInputMeta.getCompositeJaxbClassName(), getClass()));

        TransMeta transMeta = TransTestFactory.generateTestTransformation(
                new Variables(), zosFileInputMeta, stepName);

        List<RowMetaAndData> result = TransTestFactory
                .executeTestTransformation(transMeta,
                        TransTestFactory.INJECTOR_STEPNAME, stepName,
                        TransTestFactory.DUMMY_STEPNAME, createSourceData());

        checkRows(
                createResultData(stepName, zosFileInputMeta.getInputFields(),
                        resultData), result);
    }

    /**
     * Turns a hex content into a single record file for testing.
     * 
     * @param hexContent the hex content
     * @return a file name holding a record
     * @throws IOException if writing fails
     */
    protected String writeToTempBinFile(final String hexContent)
            throws IOException {
        File tempFile = File.createTempFile(this.getName(), ".tmp");
        tempFile.deleteOnExit();
        FileUtils.writeByteArrayToFile(tempFile,
                HostData.toByteArray(hexContent));
        return tempFile.getAbsolutePath();

    }

    /**
     * Create a row of expected results.
     * 
     * @param stepName the step name
     * @param fileFields the file record fields
     * @param resultData the expected result data (one or more rows)
     * 
     * @return the expected row and data results
     */
    public List<RowMetaAndData> createResultData(final String stepName,
            final CobFileInputField[] fileFields, final Object[][] resultData) {
        return createData(createResultRowMetaInterface(stepName, fileFields),
                resultData);
    }

    /**
     * The PDI test framework has an injector step before our own.
     * 
     * @return row meta and data for the injector step
     */
    protected List<RowMetaAndData> createSourceData() {
        return createData(createSourceRowMetaInterface(),
                new Object[][] { new Object[] { "abc" } });
    }

    /**
     * This is the meta data for the injector step.
     * 
     * @return meta data for the injector
     */
    protected RowMetaInterface createSourceRowMetaInterface() {
        return createRowMetaInterface(new ValueMeta("field1",
                ValueMeta.TYPE_STRING));
    }

    /**
     * Create the expected meta data.
     * 
     * @param stepName the step name
     * @param fileFields the file fields
     * 
     * @return the expected meta data
     */
    protected RowMetaInterface createResultRowMetaInterface(
            final String stepName, final CobFileInputField[] fileFields) {
        RowMetaInterface outputRowMeta = new RowMeta();
        Cob2PdiFields.fieldsToRowMeta(fileFields, stepName, outputRowMeta);
        return outputRowMeta;

    }
}

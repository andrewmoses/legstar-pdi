package com.legstar.pdi;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Tests for CobolToPdi class.
 * 
 */
public class Cob2PdiFieldsTest extends AbstractTest {

    /** True when references should be created. */
    private static final boolean CREATE_REFERENCES = false;

    /**
     * Put a set of test case jars onto the context class loader.
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {
        super.setUp();
        setCreateReferences(CREATE_REFERENCES);

    }

    /**
     * Check if name conflicts are handled correctly.
     */
    public void testNameConflicts() {
        List<CobFileInputField> fields = new ArrayList<CobFileInputField>();
        assertEquals("aname",
                Cob2PdiFields.newName(fields, "aname", null, null));
        assertEquals("aname_0",
                Cob2PdiFields.newName(fields, "aname", null, "_0"));
        assertEquals("aname_0",
                Cob2PdiFields.newName(fields, "aname", "parent", "_0"));
        CobFileInputField field = new CobFileInputField();
        field.setName("aname_0");
        fields.add(field);
        assertEquals("aname_0",
                Cob2PdiFields.newName(fields, "aname", null, "_0"));
        assertEquals("parent_aname_0",
                Cob2PdiFields.newName(fields, "aname", "parent", "_0"));
        field = new CobFileInputField();
        field.setName("parent_aname_0");
        fields.add(field);
        assertEquals("parent_aname_0",
                Cob2PdiFields.newName(fields, "aname", "parent", "_0"));
        assertEquals("grandparent_parent_aname_0", Cob2PdiFields.newName(
                fields, "aname", "grandparent_parent", "_0"));

    }

    /**
     * Test flat01 (simple).
     * 
     * @throws Exception
     */
    public void testFlat01() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.flat01cc.Flat01Record");
        check("flat01", "json", fields.toString());

    }

    /**
     * Test flat02 (simple array).
     * 
     * @throws Exception
     */
    public void testFlat02() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.flat02cc.Flat02Record");
        check("flat02", "json", fields.toString());

    }

    /**
     * Test stru03 (complex array).
     * 
     * @throws Exception
     */
    public void testStru03() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru03cc.Stru03Record");
        check("stru03", "json", fields.toString());

    }

    /**
     * Test stru04 (several nested arrays).
     * 
     * @throws Exception
     */
    public void testStru04() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru04cc.Stru04Record");
        check("stru04", "json", fields.toString());

    }

    /**
     * Test stru05 (with name conflicts).
     * 
     * @throws Exception
     */
    public void testStru05() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.stru05cc.Stru05Record");
        check("stru05", "json", fields.toString());

    }

    /**
     * Test rdef01 (with redefines).
     * 
     * @throws Exception
     */
    public void testRdef01() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.rdef01cc.Rdef01Record");
        check("rdef01", "json", fields.toString());

    }

    /**
     * Test rdef01 (more complex redefines).
     * 
     * @throws Exception
     */
    public void testRdef02() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.rdef02cc.Rdef02Record");
        check("rdef02", "json", fields.toString());

    }

    /**
     * Test alltypes (All supported COBOL types).
     * 
     * @throws Exception
     */
    public void testAllTypes() throws Exception {

        List<CobFileInputField> fields = Cob2PdiFields
                .toFields("com.legstar.test.coxb.alltypcc.AlltypesRecord");
        check("alltypcc", "json", fields.toString());

    }

    /**
     * Test generation of output row meta.
     * 
     * @throws Exception id transformation fails
     */
    public void testToOutputRowMeta() throws Exception {

        RowMetaInterface outputRowMeta = new RowMeta();
        Cob2PdiFields.fieldsToRowMeta(Cob2PdiFields.getCobolFields(
                "com.legstar.test.coxb.flat01cc.Flat01Record", getClass()),
                null, outputRowMeta);
        ValueMetaInterface valueMeta = outputRowMeta.getValueMeta(0);
        assertEquals("ComNumber", valueMeta.getName());
        assertEquals("Integer", valueMeta.getTypeDesc());
        valueMeta = outputRowMeta.getValueMeta(1);
        assertEquals("ComName", valueMeta.getName());
        assertEquals("String", valueMeta.getTypeDesc());
        valueMeta = outputRowMeta.getValueMeta(2);
        assertEquals("ComAmount", valueMeta.getName());
        assertEquals("BigNumber", valueMeta.getTypeDesc());

    }

}

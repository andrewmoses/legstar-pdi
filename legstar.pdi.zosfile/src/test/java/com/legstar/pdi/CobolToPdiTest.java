package com.legstar.pdi;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.legstar.coxb.host.HostData;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.pdi.CobolFileInputField;
import com.legstar.pdi.CobolToPdi;
import com.legstar.test.coxb.LsfileaeCases;

import junit.framework.TestCase;

/**
 * Tests for CobolToPdi class.
 *
 */
public class CobolToPdiTest extends TestCase {
	
	 /**
	 * Test CustomerData.
	 * @throws Exception
	 */
	public void testToFieldsCustomerData() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.tcobwvb.ObjectFactory(),
				new com.legstar.test.coxb.tcobwvb.CustomerData());
		assertEquals("CustomerId", fields.get(0).getName());
		assertEquals("CustomerName", fields.get(1).getName());
		assertEquals("CustomerAddress", fields.get(2).getName());
		assertEquals("CustomerPhone", fields.get(3).getName());
		assertEquals("TransactionNbr", fields.get(4).getName());
		assertEquals("TransactionDate_0", fields.get(5).getName());
		assertEquals("TransactionAmount_0", fields.get(6).getName());
		assertEquals("TransactionComment_0", fields.get(7).getName());
        assertEquals("TransactionDate_1", fields.get(8).getName());
        assertEquals("TransactionAmount_1", fields.get(9).getName());
        assertEquals("TransactionComment_1", fields.get(10).getName());
        assertEquals("TransactionDate_2", fields.get(11).getName());
        assertEquals("TransactionAmount_2", fields.get(12).getName());
        assertEquals("TransactionComment_2", fields.get(13).getName());
        assertEquals("TransactionDate_3", fields.get(14).getName());
        assertEquals("TransactionAmount_3", fields.get(15).getName());
        assertEquals("TransactionComment_3", fields.get(16).getName());
        assertEquals("TransactionDate_4", fields.get(17).getName());
        assertEquals("TransactionAmount_4", fields.get(18).getName());
        assertEquals("TransactionComment_4", fields.get(19).getName());
	}

	/**
	 * Test Lsfileae.
	 * @throws Exception
	 */
	public void testToFieldsLsfileae() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.lsfileae.ObjectFactory(),
				new com.legstar.test.coxb.lsfileae.Dfhcommarea());
		assertEquals("ComNumber", fields.get(0).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(0).getType());
		assertEquals("ComName", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(1).getType());
		assertEquals("ComAddress", fields.get(2).getName());
		assertEquals("ComPhone", fields.get(3).getName());
		assertEquals("ComDate", fields.get(4).getName());
		assertEquals("ComAmount", fields.get(5).getName());
		assertEquals("ComComment", fields.get(6).getName());
	}

	/**
	 * Test Osarray.
	 * @throws Exception
	 */
	public void testToFieldsOsarray() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.osarrays.ObjectFactory(),
				new com.legstar.test.coxb.osarrays.Dfhcommarea());
		assertEquals("SString", fields.get(0).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(0).getType());
		assertEquals("SBinary", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(1).getType());
		assertEquals("AString_0", fields.get(2).getName());
		assertEquals("AString_1", fields.get(3).getName());
		assertEquals("ABinary_0", fields.get(4).getName());
		assertEquals("ABinary_1", fields.get(5).getName());
	}

	/**
	 * Test Lsfileac.
	 * @throws Exception
	 */
	public void testToFieldsLsfileac() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.lsfileac.ObjectFactory(),
				new com.legstar.test.coxb.lsfileac.ReplyData());
		assertEquals("ReplyItemscount", fields.get(0).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(0).getType());
		assertEquals("ReplyNumber_0", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(1).getType());
		assertEquals("ReplyName_0", fields.get(2).getName());
		assertEquals("ReplyAddress_0", fields.get(3).getName());
		assertEquals("ReplyPhone_0", fields.get(4).getName());
		assertEquals("ReplyDate_0", fields.get(5).getName());
		assertEquals("ReplyAmount_0", fields.get(6).getName());
		assertEquals("ReplyComment_0", fields.get(7).getName());

		assertEquals("ReplyNumber_1", fields.get(8).getName());
		assertEquals("ReplyName_1", fields.get(9).getName());
		assertEquals("ReplyAddress_1", fields.get(10).getName());
		assertEquals("ReplyPhone_1", fields.get(11).getName());
		assertEquals("ReplyDate_1", fields.get(12).getName());
		assertEquals("ReplyAmount_1", fields.get(13).getName());
		assertEquals("ReplyComment_1", fields.get(14).getName());
	}

	/**
	 * Test Alltypes.
	 * @throws Exception
	 */
	public void testToFieldsAlltypes() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.alltypes.ObjectFactory(),
				new com.legstar.test.coxb.alltypes.Dfhcommarea());
		assertEquals("SString", fields.get(0).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(0).getType());
		assertEquals("SBinary", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_BINARY, fields.get(1).getType());
		assertEquals("SShort", fields.get(2).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(2).getType());
		assertEquals("SUshort", fields.get(3).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(3).getType());
		assertEquals("SInt", fields.get(4).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(4).getType());
		assertEquals("SUint", fields.get(5).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(5).getType());
		assertEquals("SLong", fields.get(6).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(6).getType());
		assertEquals("SUlong", fields.get(7).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(7).getType());
		assertEquals("SXlong", fields.get(8).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(8).getType());
		assertEquals("SUxlong", fields.get(9).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(9).getType());
		assertEquals("SDec", fields.get(10).getName());
		assertEquals(ValueMetaInterface.TYPE_BIGNUMBER, fields.get(10).getType());
		assertEquals("SFloat", fields.get(11).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(11).getType());
		assertEquals("SDouble", fields.get(12).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(12).getType());
		assertEquals("AString_0", fields.get(13).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(13).getType());
		assertEquals("AString_1", fields.get(14).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(14).getType());
		assertEquals("ABinary_0", fields.get(15).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(15).getType());
		assertEquals("ABinary_1", fields.get(16).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(16).getType());
		assertEquals("AShort_0", fields.get(17).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(17).getType());
		assertEquals("AShort_1", fields.get(18).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(18).getType());
		assertEquals("AUshort_0", fields.get(19).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(19).getType());
		assertEquals("AUshort_1", fields.get(20).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(20).getType());
		assertEquals("AInt_0", fields.get(21).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(21).getType());
		assertEquals("AInt_1", fields.get(22).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(22).getType());
		assertEquals("AUint_0", fields.get(23).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(23).getType());
		assertEquals("AUint_1", fields.get(24).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(24).getType());
		assertEquals("ALong_0", fields.get(25).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(25).getType());
		assertEquals("ALong_1", fields.get(26).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(26).getType());
		assertEquals("AUlong_0", fields.get(27).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(27).getType());
		assertEquals("AUlong_1", fields.get(28).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(28).getType());
		assertEquals("AXlong_0", fields.get(29).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(29).getType());
		assertEquals("AXlong_1", fields.get(30).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(30).getType());
		assertEquals("AUxlong_0", fields.get(31).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(31).getType());
		assertEquals("AUxlong_1", fields.get(32).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(32).getType());
		assertEquals("ADec_0", fields.get(33).getName());
		assertEquals(ValueMetaInterface.TYPE_BIGNUMBER, fields.get(33).getType());
		assertEquals("ADec_1", fields.get(34).getName());
		assertEquals(ValueMetaInterface.TYPE_BIGNUMBER, fields.get(34).getType());
		assertEquals("AFloat_0", fields.get(35).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(35).getType());
		assertEquals("AFloat_1", fields.get(36).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(36).getType());
		assertEquals("ADouble_0", fields.get(37).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(37).getType());
		assertEquals("ADouble_1", fields.get(38).getName());
		assertEquals(ValueMetaInterface.TYPE_NUMBER, fields.get(38).getType());
	}
	
	/**
	 * Test Redsimpt.
	 * @throws Exception
	 */
	public void testToFieldsRedsimpt() throws Exception {
		
		List<CobolFileInputField> fields = CobolToPdi.toFields(
				new com.legstar.test.coxb.redsimpt.ObjectFactory(),
				new com.legstar.test.coxb.redsimpt.Dfhcommarea());
		assertEquals("CDefinition1", fields.get(0).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(0).getType());
		assertEquals(18, fields.get(0).getLength());
		assertTrue(fields.get(0).isRedefined());
	}

	/**
	 * Test calculation of host byte array length.
	 */
    public void testHostByteLength() {
        try {
            assertEquals(
                    79,
                    CobolToPdi
                            .newHostRecord((new com.legstar.test.coxb.lsfileae.bind.DfhcommareaTransformers())).length);
            assertEquals(
                    32025,
                    CobolToPdi
                            .newHostRecord(new com.legstar.test.coxb.dplarcht.bind.DfhcommareaTransformers()).length);
        } catch (KettleException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
	
	/**
	 * Test transformation and generation of PDI output row data.
	 * @throws Exception id transformation fails
	 */
	public void testToOutputRowDataLsfileae() throws Exception {
		
		RowMetaInterface outputRowMeta = new RowMeta();
		CobolToPdi.fieldsToRowMeta(
				CobolToPdi.getCobolFields(
						"com.legstar.test.coxb.lsfileae.Dfhcommarea",
						getClass()), 
				null,
				outputRowMeta);
		
		HostTransformStatus status = new HostTransformStatus();
		Object[] outputRowData = CobolToPdi.toOutputRowData(
				outputRowMeta,
				new com.legstar.test.coxb.lsfileae.bind.DfhcommareaTransformers(),
				HostData.toByteArray(LsfileaeCases.getHostBytesHex()),
				CobolToPdi.getDefaultHostCharset(),
				status);

        assertEquals(79, status.getHostBytesProcessed());
		assertEquals(7, outputRowData.length);
		assertEquals(100L, outputRowData[0]);
		assertEquals("TOTO", (String) outputRowData[1]);
		assertEquals("LABAS STREET", (String) outputRowData[2]);
		assertEquals("88993314", (String) outputRowData[3]);
		assertEquals("100458", (String) outputRowData[4]);
		assertEquals("00100.35", (String) outputRowData[5]);
		assertEquals("A VOIR", (String) outputRowData[6]);
		
	}
	
	
	/**
	 * Check if name conflicts are handled correctly.
	 */
	public void testNameConflicts() {
	    List<CobolFileInputField> fields = new ArrayList <CobolFileInputField>();
	    assertEquals("aname", CobolToPdi.newName(fields, "aname", null, null));
        assertEquals("aname_0", CobolToPdi.newName(fields, "aname", null, "_0"));
        assertEquals("aname_0", CobolToPdi.newName(fields, "aname", "parent", "_0"));
        CobolFileInputField field = new CobolFileInputField();
        field.setName("aname_0");
        fields.add(field);
        assertEquals("aname_0", CobolToPdi.newName(fields, "aname", null, "_0"));
        assertEquals("parent_aname_0", CobolToPdi.newName(fields, "aname", "parent", "_0"));
        field = new CobolFileInputField();
        field.setName("parent_aname_0");
        fields.add(field);
        assertEquals("parent_aname_0", CobolToPdi.newName(fields, "aname", "parent", "_0"));
        assertEquals("grandparent_parent_aname_0", CobolToPdi.newName(fields, "aname", "grandparent_parent", "_0"));
	    
	}
	
}

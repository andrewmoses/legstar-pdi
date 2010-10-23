package com.legstar.pdi;

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
		assertEquals("PersonalData_CustomerName", fields.get(1).getName());
		assertEquals("PersonalData_CustomerAddress", fields.get(2).getName());
		assertEquals("PersonalData_CustomerPhone", fields.get(3).getName());
		assertEquals("Transactions_TransactionNbr", fields.get(4).getName());
		assertEquals("Transactions_Transaction_LastTransDateChoice_LastTransDate_0", fields.get(5).getName());
		assertEquals("Transactions_Transaction_LastTransAmount_0", fields.get(6).getName());
		assertEquals("Transactions_Transaction_LastTransComment_0", fields.get(7).getName());
        assertEquals("Transactions_Transaction_LastTransDateChoice_LastTransDate_1", fields.get(8).getName());
        assertEquals("Transactions_Transaction_LastTransAmount_1", fields.get(9).getName());
        assertEquals("Transactions_Transaction_LastTransComment_1", fields.get(10).getName());
        assertEquals("Transactions_Transaction_LastTransDateChoice_LastTransDate_2", fields.get(11).getName());
        assertEquals("Transactions_Transaction_LastTransAmount_2", fields.get(12).getName());
        assertEquals("Transactions_Transaction_LastTransComment_2", fields.get(13).getName());
        assertEquals("Transactions_Transaction_LastTransDateChoice_LastTransDate_3", fields.get(14).getName());
        assertEquals("Transactions_Transaction_LastTransAmount_3", fields.get(15).getName());
        assertEquals("Transactions_Transaction_LastTransComment_3", fields.get(16).getName());
        assertEquals("Transactions_Transaction_LastTransDateChoice_LastTransDate_4", fields.get(17).getName());
        assertEquals("Transactions_Transaction_LastTransAmount_4", fields.get(18).getName());
        assertEquals("Transactions_Transaction_LastTransComment_4", fields.get(19).getName());
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
		assertEquals("ComPersonal_ComName", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_STRING, fields.get(1).getType());
		assertEquals("ComPersonal_ComAddress", fields.get(2).getName());
		assertEquals("ComPersonal_ComPhone", fields.get(3).getName());
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
		assertEquals("ReplyItem_ReplyNumber_0", fields.get(1).getName());
		assertEquals(ValueMetaInterface.TYPE_INTEGER, fields.get(1).getType());
		assertEquals("ReplyItem_ReplyPersonal_ReplyName_0", fields.get(2).getName());
		assertEquals("ReplyItem_ReplyPersonal_ReplyAddress_0", fields.get(3).getName());
		assertEquals("ReplyItem_ReplyPersonal_ReplyPhone_0", fields.get(4).getName());
		assertEquals("ReplyItem_ReplyDate_0", fields.get(5).getName());
		assertEquals("ReplyItem_ReplyAmount_0", fields.get(6).getName());
		assertEquals("ReplyItem_ReplyComment_0", fields.get(7).getName());

		assertEquals("ReplyItem_ReplyNumber_1", fields.get(8).getName());
		assertEquals("ReplyItem_ReplyPersonal_ReplyName_1", fields.get(9).getName());
		assertEquals("ReplyItem_ReplyPersonal_ReplyAddress_1", fields.get(10).getName());
		assertEquals("ReplyItem_ReplyPersonal_ReplyPhone_1", fields.get(11).getName());
		assertEquals("ReplyItem_ReplyDate_1", fields.get(12).getName());
		assertEquals("ReplyItem_ReplyAmount_1", fields.get(13).getName());
		assertEquals("ReplyItem_ReplyComment_1", fields.get(14).getName());
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
		assertEquals("CDefinition1Choice_CDefinition1", fields.get(0).getName());
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
							.hostByteLength(new com.legstar.test.coxb.lsfileae.bind.DfhcommareaTransformers()));
			assertEquals(
					32025,
					CobolToPdi
							.hostByteLength(new com.legstar.test.coxb.dplarcht.bind.DfhcommareaTransformers()));
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
				CobolToPdi.toFieldArray(
						"com.legstar.test.coxb.lsfileae.Dfhcommarea"), 
				null,
				outputRowMeta);
		
		HostTransformStatus status = new HostTransformStatus();
		Object[] outputRowData = CobolToPdi.toOutputRowData(
				outputRowMeta,
				new com.legstar.test.coxb.lsfileae.bind.DfhcommareaTransformers(),
				HostData.toByteArray(LsfileaeCases.getHostBytesHex()),
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
	
}

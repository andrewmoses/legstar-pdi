package com.legstar.pdi;

import java.math.BigDecimal;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

import com.legstar.coxb.host.HostData;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.coxb.util.BindingUtil;

/**
 * Tests for CobolToPdi class.
 * 
 */
public class CobolToPdiTest extends AbstractTest {

    /**
     * Test calculation of host byte array length.
     */
    public void testHostByteLength() throws Exception {
        try {
            assertEquals(
                    30,
                    CobolToPdi.newHostRecord(BindingUtil
                            .newTransformers("com.legstar.test.coxb.flat01cc.Flat01Record")).length);
            assertEquals(
                    98,
                    CobolToPdi.newHostRecord(BindingUtil
                            .newTransformers("com.legstar.test.coxb.stru04cc.Stru04Record")).length);
        } catch (KettleException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Test transformation and generation of PDI output row data.
     * 
     * @throws Exception id transformation fails
     */
    public void testFlat01() throws Exception {

        Object[] outputRowData = transform(
                "com.legstar.test.coxb.flat01cc.Flat01Record",
                "F0F0F0F0F4F3D5C1D4C5F0F0F0F0F4F3404040404040404040400215000FF0",
                30);
        assertEquals(43L, outputRowData[0]);
        assertEquals("NAME000043", outputRowData[1]);
        assertEquals(new BigDecimal("2150.00"), outputRowData[2]);

    }

    /**
     * Test with a simple array.
     * 
     * @throws Exception id transformation fails
     */
    public void testFlat02() throws Exception {

        Object[] outputRowData = transform(
                "com.legstar.test.coxb.flat02cc.Flat02Record",
                "F0F0F0F0F6F2D5C1D4C5F0F0F0F0F6F2404040404040404040400310000F003E001F0014000F000C",
                40);
        assertEquals(62L, outputRowData[0]);
        assertEquals("NAME000062", outputRowData[1]);
        assertEquals(new BigDecimal("3100.00"), outputRowData[2]);
        assertEquals(new Short("62"), outputRowData[3]);
        assertEquals(new Short("31"), outputRowData[4]);
        assertEquals(new Short("20"), outputRowData[5]);
        assertEquals(new Short("15"), outputRowData[6]);
        assertEquals(new Short("12"), outputRowData[7]);

    }

    /**
     * Test complex array unmarshaling.
     * 
     * @throws Exception
     */
    public void testStru03() throws Exception {
        Object[] outputRowData = transform(
                "com.legstar.test.coxb.stru03cc.Stru03Record",
                "F0F0F0F0F6F2D5C1D4C5F0F0F0F0F6F2404040404040404040400310000F003EC1C2001FC1C20014C1C2000FC1C2000CC1C2",
                50);
        assertEquals(13, outputRowData.length);
        assertEquals(62L, outputRowData[0]);
        assertEquals("NAME000062", outputRowData[1]);
        assertEquals(new BigDecimal("3100.00"), outputRowData[2]);
        assertEquals(new Short("62"), outputRowData[3]);
        assertEquals("AB", outputRowData[4]);
        assertEquals(new Short("31"), outputRowData[5]);
        assertEquals("AB", outputRowData[6]);
        assertEquals(new Short("20"), outputRowData[7]);
        assertEquals("AB", outputRowData[8]);
        assertEquals(new Short("15"), outputRowData[9]);
        assertEquals("AB", outputRowData[10]);
        assertEquals(new Short("12"), outputRowData[11]);
        assertEquals("AB", outputRowData[12]);
    }

    /**
     * Test with a complex structure with multiple arrays.
     * 
     * @throws Exception id transformation fails
     */
    public void testStru04() throws Exception {

        Object[] outputRowData = transform(
                "com.legstar.test.coxb.stru04cc.Stru04Record",
                "0190000F00090006C2C5C5C2C4C40001900FC2C2C5C4C5C30000950F0003000000020013000CC2C4C2C1C5C40003800FC1C5C2C2C4C10001900F000600000005001C0013C1C5C2C5C1C30005700FC4C2C3C3C3C20002850F0009000000080023750F",
                98);
        assertEquals(new BigDecimal("1900.00"), outputRowData[0]);
        assertEquals(new Short("9"), outputRowData[1]);
        assertEquals(new Short("6"), outputRowData[2]);
        assertEquals("B", outputRowData[3]);
        assertEquals("E", outputRowData[4]);
        assertEquals("E", outputRowData[5]);
        assertEquals("B", outputRowData[6]);
        assertEquals("D", outputRowData[7]);
        assertEquals("D", outputRowData[8]);
        assertEquals(new BigDecimal("19.00"), outputRowData[9]);
        assertEquals("B", outputRowData[10]);
        assertEquals("B", outputRowData[11]);
        assertEquals("E", outputRowData[12]);
        assertEquals("D", outputRowData[13]);
        assertEquals("E", outputRowData[14]);
        assertEquals("C", outputRowData[15]);
        assertEquals(new BigDecimal("9.50"), outputRowData[16]);
        assertEquals(new Short("3"), outputRowData[17]);
        assertEquals(new Integer("2"), outputRowData[18]);
        assertEquals(new Short("19"), outputRowData[19]);
        assertEquals(new Short("12"), outputRowData[20]);
        assertEquals("B", outputRowData[21]);
        assertEquals("D", outputRowData[22]);
        assertEquals("B", outputRowData[23]);
        assertEquals("A", outputRowData[24]);
        assertEquals("E", outputRowData[25]);
        assertEquals("D", outputRowData[26]);
        assertEquals(new BigDecimal("38.00"), outputRowData[27]);
        assertEquals("A", outputRowData[28]);
        assertEquals("E", outputRowData[29]);
        assertEquals("B", outputRowData[30]);
        assertEquals("B", outputRowData[31]);
        assertEquals("D", outputRowData[32]);
        assertEquals("A", outputRowData[33]);
        assertEquals(new BigDecimal("19.00"), outputRowData[34]);
        assertEquals(new Short("6"), outputRowData[35]);
        assertEquals(new Integer("5"), outputRowData[36]);
        assertEquals(new Short("28"), outputRowData[37]);
        assertEquals(new Short("19"), outputRowData[38]);
        assertEquals("A", outputRowData[39]);
        assertEquals("E", outputRowData[40]);
        assertEquals("B", outputRowData[41]);
        assertEquals("E", outputRowData[42]);
        assertEquals("A", outputRowData[43]);
        assertEquals("C", outputRowData[44]);
        assertEquals(new BigDecimal("57.00"), outputRowData[45]);
        assertEquals("D", outputRowData[46]);
        assertEquals("B", outputRowData[47]);
        assertEquals("C", outputRowData[48]);
        assertEquals("C", outputRowData[49]);
        assertEquals("C", outputRowData[50]);
        assertEquals("B", outputRowData[51]);
        assertEquals(new BigDecimal("28.50"), outputRowData[52]);
        assertEquals(new Short("9"), outputRowData[53]);
        assertEquals(new Integer("8"), outputRowData[54]);
        assertEquals(new BigDecimal("237.50"), outputRowData[55]);

    }

    /**
     * Actual transformation of a zos payload to PDI objects array.
     * 
     * @param qualifiedClassName the record descriptor class name
     * @param hostPayload the host payload as hex byte string
     * @param expectedBytesProcessed the expected number of byte processed
     * @return an array of java objects
     * @throws Exception if transformation fails
     */
    protected Object[] transform(final String qualifiedClassName,
            final String hostPayload, final int expectedBytesProcessed)
            throws Exception {
        RowMetaInterface outputRowMeta = new RowMeta();
        Cob2PdiFields.fieldsToRowMeta(
                Cob2PdiFields.getCobolFields(qualifiedClassName, getClass()),
                null, outputRowMeta);

        HostTransformStatus status = new HostTransformStatus();
        Object[] outputRowData = CobolToPdi.toOutputRowData(outputRowMeta,
                BindingUtil.newTransformers(qualifiedClassName),
                HostData.toByteArray(hostPayload),
                Cob2PdiFields.getDefaultHostCharset(), status);
        assertEquals(expectedBytesProcessed, status.getHostBytesProcessed());
        assertNotNull(outputRowData);
        return outputRowData;
    }

}

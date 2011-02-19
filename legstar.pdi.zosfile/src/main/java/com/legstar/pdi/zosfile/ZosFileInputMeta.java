package com.legstar.pdi.zosfile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import com.legstar.pdi.CobolFileInputField;
import com.legstar.pdi.CobolToPdi;

/**
 * This is a Kettle step implementation class. Acts as a model and knows how to
 * serialize itself both in XML or a Kettle repository.
 */
@Step(id = "com.legstar.pdi.zosfile", name = "ZosFileInputMeta.Step.Name", description = "ZosFileInputMeta.Step.Description", categoryDescription = "ZosFileInputMeta.Step.CategoryDescription", image = "ZOSFIN.png", i18nPackageName = "com.legstar.pdi.zosfile/messages"

)
public class ZosFileInputMeta extends BaseStepMeta implements StepMetaInterface {

    /** I18N identifier. */
    private static Class<?> PKG = ZosFileInputMeta.class;

    /*
     * ------------------------------------------------------------------------
     * Following are key identifiers for this model persistence.
     * ------------------------------------------------------------------------
     */

    /** Used to serialize/deserialize the file name. */
    public static final String FILE_NAME_TAG = "filename";

    /** Used to serialize/deserialize isVariableLength. */
    public static final String IS_VARIABLE_LENGTH_TAG = "isvariablelength";

    /** Used to serialize/deserialize hasRecordDescriptorWord. */
    public static final String HAS_RECORD_DESCRIPTOR_WORD_TAG = "hasrdw";

    /** Used to serialize/deserialize the mainframe host character set. */
    public static final String HOST_CHARSET_TAG = "hostcharset";

    /** Used to serialize/deserialize isFromCobolSource. */
    public static final String IS_FROM_COBOL_SOURCE = "isfromcobolsource";

    /** Used to serialize/deserialize the JAXB composite class name. */
    public static final String COMPOSITE_JAXB_CLASS_NAME_TAG = "compositejaxbclassname";

    /** Used to serialize/deserialize the COBOL source code. */
    public static final String COBOL_SOURCE_TAG = "cobolsource";

    /** Used to serialize/deserialize the COBOL host character set. */
    public static final String COBOL_CHARSET_TAG = "cobolcharset";

    /** Used to serialize/deserialize the COBOL file path. */
    public static final String COBOL_FILE_PATH_TAG = "cobolfilepath";

    /** Used to serialize/deserialize the field name attribute. */
    public static final String FIELD_NAME_TAG = "name";

    /** Used to serialize/deserialize the field type attribute. */
    public static final String FIELD_TYPE_TAG = "type";

    /** Used to serialize/deserialize the field length attribute. */
    public static final String FIELD_LENGTH_TAG = "length";

    /** Used to serialize/deserialize the field precision attribute. */
    public static final String FIELD_PRECISION_TAG = "precision";

    /** Used to serialize/deserialize the field trim type attribute. */
    public static final String FIELD_TRIM_TYPE_TAG = "trim_type";

    /** Used to serialize/deserialize the field redefined attribute. */
    public static final String FIELD_REDEFINED_TAG = "redefined";

    /*
     * ------------------------------------------------------------------------
     * Following are this class fields that are persistent.
     * ------------------------------------------------------------------------
     */

    /** The local copy of the z/OS file. */
    private String _filename;

    /** Are the z/OS file records variable length. */
    private boolean _isVariableLength;

    /** Does the z/OS file records start with an RDW?. */
    private boolean _hasRecordDescriptorWord;

    /** The mainframe character set. */
    private String _hostCharset;

    /** True if this meta originates from COBOL source. */
    private boolean _isFromCobolSource;

    /**
     * The COBOL-annotated JAXB class name and containing jar file name used by
     * Transformer.
     */
    private String _compositeJaxbClassName;

    /** The COBOL source code used to generate the Transformer. */
    private String _cobolSource;

    /** The COBOL character set. */
    private String _cobolCharset;

    /** The COBOL file path. */
    private String _cobolFilePath;

    /** Fields from a z/OS file record. */
    private CobolFileInputField[] _inputFields;

    public ZosFileInputMeta() {
        super();
    }

    /*
     * ------------------------------------------------------------------------
     * Bean properties section
     * ------------------------------------------------------------------------
     */

    /**
     * @return the local copy of the z/OS file
     */
    public String getFilename() {
        return _filename;
    }

    /**
     * @param filename the local copy of the z/OS file to set
     */
    public void setFilename(String filename) {
        _filename = filename;
    }

    /**
     * Are the z/OS file records variable length
     * 
     * @return true if the z/OS file records are fixed length
     */
    public boolean isVariableLength() {
        return _isVariableLength;
    }

    /**
     * Are the z/OS file records variable length
     * 
     * @param isFixedRecord true if the z/OS file records are fixed length
     */
    public void setIsVariableLength(boolean isFixedRecord) {
        _isVariableLength = isFixedRecord;
    }

    /**
     * Does the z/OS file records start with an RDW?.
     * 
     * @return true if z/OS file records start with a record descriptor word
     */
    public boolean hasRecordDescriptorWord() {
        return _hasRecordDescriptorWord;
    }

    /**
     * Does the z/OS file records start with an RDW?.
     * 
     * @param _hasRecordDescriptorWord true if z/OS file records start with a
     *        record descriptor word
     */
    public void setHasRecordDescriptorWord(boolean hasRecordDescriptorWord) {
        _hasRecordDescriptorWord = hasRecordDescriptorWord;
    }

    /**
     * The mainframe character set
     * 
     * @return the mainframe character set
     */
    public String getHostCharset() {
        return _hostCharset;
    }

    /**
     * The mainframe character set.
     * 
     * @param hostCharset The mainframe character set
     */
    public void setHostCharset(String hostCharset) {
        _hostCharset = hostCharset;
    }

    /**
     * @return true if this meta originates from COBOL source
     */
    public boolean isFromCobolSource() {
        return _isFromCobolSource;
    }

    /**
     * @param isFromCobolSource true if this meta originates from COBOL source
     */
    public void setFromCobolSource(boolean isFromCobolSource) {
        _isFromCobolSource = isFromCobolSource;
    }

    /**
     * @return the COBOL-annotated JAXB class name and containing jar file name
     *         used by Transformer
     */
    public String getCompositeJaxbClassName() {
        return _compositeJaxbClassName;
    }

    /**
     * @param compositeJaxbClassName the COBOL-annotated JAXB class name and
     *        containing jar file name used by Transformer to set
     */
    public void setCompositeJaxbClassName(String compositeJaxbClassName) {
        _compositeJaxbClassName = compositeJaxbClassName;
    }

    /**
     * @return the COBOL-annotated JAXB qualified class name used by Transformer
     */
    public String getCobolSource() {
        return _cobolSource;
    }

    /**
     * @param cobolSource the COBOL-annotated JAXB qualified class name used by
     *        Transformer to set
     */
    public void setCobolSource(String cobolSource) {
        _cobolSource = cobolSource;
    }

    /**
     * @return the COBOL character set
     */
    public String getCobolCharset() {
        return _cobolCharset;
    }

    /**
     * @param cobolCharset the COBOL character set to set
     */
    public void setCobolCharset(String cobolCharset) {
        _cobolCharset = cobolCharset;
    }

    /**
     * @return the COBOL file path
     */
    public String getCobolFilePath() {
        return _cobolFilePath;
    }

    /**
     * @param cobolFilePath the COBOL file path to set
     */
    public void setCobolFilePath(String cobolFilePath) {
        this._cobolFilePath = cobolFilePath;
    }

    /**
     * @return the inputFields
     */
    public CobolFileInputField[] getInputFields() {
        return _inputFields;
    }

    /**
     * @param inputFields the inputFields to set
     */
    public void setInputFields(CobolFileInputField[] inputFields) {
        _inputFields = inputFields;
    }

    /*
     * ------------------------------------------------------------------------
     * Initial bean properties values
     * ------------------------------------------------------------------------
     */
    public void setDefault() {
        _inputFields = new CobolFileInputField[0];
        _hostCharset = CobolToPdi.getDefaultHostCharset();
        _isFromCobolSource = true;
        _cobolCharset = Charset.defaultCharset().name();
    }

    /*
     * ------------------------------------------------------------------------
     * XML Serialization section
     * ------------------------------------------------------------------------
     */

    /** {@inheritDoc} */
    public String getXML() throws KettleValueException {
        StringBuffer retval = new StringBuffer();
        addTagValue(retval, FILE_NAME_TAG, _filename);
        addTagValue(retval, IS_VARIABLE_LENGTH_TAG, _isVariableLength);
        addTagValue(retval, HAS_RECORD_DESCRIPTOR_WORD_TAG,
                _hasRecordDescriptorWord);
        addTagValue(retval, HOST_CHARSET_TAG, _hostCharset);

        addTagValue(retval, IS_FROM_COBOL_SOURCE, _isFromCobolSource);
        addTagValue(retval, COMPOSITE_JAXB_CLASS_NAME_TAG,
                _compositeJaxbClassName);
        addTagValue(retval, COBOL_SOURCE_TAG, _cobolSource, _cobolCharset);
        addTagValue(retval, COBOL_CHARSET_TAG, _cobolCharset);
        addTagValue(retval, COBOL_FILE_PATH_TAG, _cobolFilePath);

        retval.append("    <fields>").append(Const.CR);
        for (int i = 0; i < _inputFields.length; i++) {
            CobolFileInputField field = _inputFields[i];

            retval.append("      <field>").append(Const.CR);
            addTagValue(retval, FIELD_NAME_TAG, field.getName());
            addTagValue(retval, FIELD_TYPE_TAG,
                    ValueMeta.getTypeDesc(field.getType()));
            addTagValue(retval, FIELD_LENGTH_TAG, field.getLength());
            addTagValue(retval, FIELD_PRECISION_TAG, field.getPrecision());
            addTagValue(retval, FIELD_TRIM_TYPE_TAG,
                    ValueMeta.getTrimTypeCode(field.getTrimType()));
            addTagValue(retval, FIELD_REDEFINED_TAG, field.isRedefined());
            retval.append("      </field>").append(Const.CR);
        }
        retval.append("    </fields>").append(Const.CR);
        return retval.toString();
    }

    /**
     * Add a formatted XML element.
     * 
     * @param retval the target XML string
     * @param tagName the XML tag name
     * @param value the element value
     */
    protected void addTagValue(StringBuffer retval, String tagName, int value) {
        retval.append("        ")
                .append(XMLHandler.addTagValue(tagName, value));
    }

    /**
     * Add a formatted XML element.
     * 
     * @param retval the target XML string
     * @param tagName the XML tag name
     * @param value the element value
     */
    protected void addTagValue(StringBuffer retval, String tagName, String value) {
        retval.append("        ")
                .append(XMLHandler.addTagValue(tagName, value));
    }

    /**
     * Add a formatted XML element.
     * 
     * @param retval the target XML string
     * @param tagName the XML tag name
     * @param value the element value
     */
    protected void addTagValue(StringBuffer retval, String tagName,
            boolean value) {
        retval.append("        ")
                .append(XMLHandler.addTagValue(tagName, value));
    }

    /**
     * Add a formatted XML element.
     * 
     * @param retval the target XML string
     * @param tagName the XML tag name
     * @param value the element value
     * @param charset the character set the element is encoded into
     * @throws KettleValueException if byte array conversion fails
     */
    protected void addTagValue(StringBuffer retval, String tagName,
            String value, String charset) throws KettleValueException {
        try {
            if (value == null || value.length() == 0) {
                addTagValue(retval, tagName, value);
            } else {
                retval.append("        ")
                        .append(XMLHandler.addTagValue(tagName,
                                value.getBytes(charset)));
            }
        } catch (IOException e) {
            throw new KettleValueException(e);
        }
    }

    /**
     * Load the values for this step from an XML Node
     * 
     * @param stepnode the Node to get the info from
     * @param databases The available list of databases to reference to
     * @param counters Counters to reference.
     * @throws KettleXMLException When an unexpected XML error occurred.
     *         (malformed etc.)
     */
    public void loadXML(Node stepnode, List<DatabaseMeta> databases,
            Map<String, Counter> counters) throws KettleXMLException {

        try {
            _filename = getTagString(stepnode, FILE_NAME_TAG, null);
            _isVariableLength = getTagBoolean(stepnode, IS_VARIABLE_LENGTH_TAG,
                    false);
            _hasRecordDescriptorWord = getTagBoolean(stepnode,
                    HAS_RECORD_DESCRIPTOR_WORD_TAG, false);
            _hostCharset = getTagString(stepnode, HOST_CHARSET_TAG,
                    CobolToPdi.getDefaultHostCharset());

            _isFromCobolSource = getTagBoolean(stepnode, IS_FROM_COBOL_SOURCE,
                    true);
            _compositeJaxbClassName = getTagString(stepnode,
                    COMPOSITE_JAXB_CLASS_NAME_TAG, null);
            _cobolCharset = getTagString(stepnode, COBOL_CHARSET_TAG, Charset
                    .defaultCharset().name());
            _cobolSource = getTagString(stepnode, COBOL_SOURCE_TAG,
                    _cobolCharset, null);
            _cobolFilePath = getTagString(stepnode, COBOL_FILE_PATH_TAG, null);

            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nFields = XMLHandler.countNodes(fields, "field");

            _inputFields = new CobolFileInputField[nFields];
            for (int i = 0; i < nFields; i++) {
                _inputFields[i] = new CobolFileInputField();

                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

                _inputFields[i].setName(getTagString(fnode, FIELD_NAME_TAG,
                        null));
                _inputFields[i]
                        .setType(ValueMeta.getType(getTagString(
                                fnode,
                                FIELD_TYPE_TAG,
                                ValueMeta
                                        .getTypeDesc(ValueMetaInterface.TYPE_STRING))));
                _inputFields[i]
                        .setLength(getTagInt(fnode, FIELD_LENGTH_TAG, -1));
                _inputFields[i].setPrecision(getTagInt(fnode,
                        FIELD_PRECISION_TAG, 0));
                _inputFields[i]
                        .setTrimType(ValueMeta.getTrimTypeByCode(getTagString(
                                fnode,
                                FIELD_TRIM_TYPE_TAG,
                                ValueMeta
                                        .getTrimTypeCode(ValueMetaInterface.TRIM_TYPE_NONE))));
                _inputFields[i].setRedefined(getTagBoolean(fnode,
                        FIELD_REDEFINED_TAG, false));
            }
        } catch (Exception e) {
            throw new KettleXMLException(
                    getI18N("ZosFileInputMeta.XmlPersistence.FailedToLoadStepInfo"),
                    e);
        }

    }

    /**
     * Return an XML element value.
     * 
     * @param node the DOM node
     * @param tagName the element name
     * @param defaultValue the default value if element not found
     * @return the element value (defaultValue if not found)
     */
    protected String getTagString(Node node, String tagName, String defaultValue) {
        String strValue = XMLHandler.getTagValue(node, tagName);
        if (strValue == null) {
            return defaultValue;
        } else {
            return strValue;
        }
    }

    /**
     * Return an XML element value.
     * 
     * @param node the DOM node
     * @param tagName the element name
     * @param charset the character set used to encode this string
     * @param defaultValue the default value if element not found
     * @return the element value (defaultValue if not found)
     * @throws KettleValueException if conversions from XML encoding to string
     *         fails
     */
    protected String getTagString(Node node, String tagName, String charset,
            String defaultValue) throws KettleValueException {
        try {
            String strValue = XMLHandler.getTagValue(node, tagName);
            if (strValue == null) {
                return defaultValue;
            } else {
                byte[] bytes = XMLHandler.stringToBinary(strValue);
                return new String(bytes, charset);
            }
        } catch (KettleException e) {
            throw new KettleValueException(e);
        } catch (UnsupportedEncodingException e) {
            throw new KettleValueException(e);
        }
    }

    /**
     * Return an XML element value.
     * 
     * @param node the DOM node
     * @param tagName the element name
     * @param defaultValue the default value if element not found
     * @return the element value (defaultValue if not found)
     */
    protected boolean getTagBoolean(Node node, String tagName,
            boolean defaultValue) {
        String strValue = XMLHandler.getTagValue(node, tagName);
        if (strValue == null) {
            return defaultValue;
        } else {
            return "Y".equalsIgnoreCase(strValue);
        }
    }

    /**
     * Return an XML element value.
     * 
     * @param node the DOM node
     * @param tagName the element name
     * @return the element value (defaultValue if not found)
     */
    protected int getTagInt(Node node, String tagName, int defaultValue) {
        String strValue = XMLHandler.getTagValue(node, tagName);
        if (strValue == null) {
            return defaultValue;
        } else {
            return Const.toInt(strValue, -1);
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Repository Serialization section
     * ------------------------------------------------------------------------
     */

    public void readRep(Repository rep, ObjectId id_step,
            List<DatabaseMeta> databases, Map<String, Counter> counters)
            throws KettleException {
        try {
            _filename = rep.getStepAttributeString(id_step, FILE_NAME_TAG);
            _isVariableLength = rep.getStepAttributeBoolean(id_step,
                    IS_VARIABLE_LENGTH_TAG);
            _hasRecordDescriptorWord = rep.getStepAttributeBoolean(id_step,
                    HAS_RECORD_DESCRIPTOR_WORD_TAG);
            _hostCharset = rep
                    .getStepAttributeString(id_step, HOST_CHARSET_TAG);

            _isFromCobolSource = rep.getStepAttributeBoolean(id_step,
                    IS_FROM_COBOL_SOURCE);
            _compositeJaxbClassName = rep.getStepAttributeString(id_step,
                    COMPOSITE_JAXB_CLASS_NAME_TAG);
            _cobolSource = rep
                    .getStepAttributeString(id_step, COBOL_SOURCE_TAG);
            _cobolCharset = rep.getStepAttributeString(id_step,
                    COBOL_CHARSET_TAG);
            _cobolFilePath = rep.getStepAttributeString(id_step,
                    COBOL_FILE_PATH_TAG);

            int nFields = rep.countNrStepAttributes(id_step, "field_"
                    + FIELD_NAME_TAG);
            _inputFields = new CobolFileInputField[nFields];

            for (int i = 0; i < nFields; i++) {
                _inputFields[i] = new CobolFileInputField();

                _inputFields[i].setName(rep.getStepAttributeString(id_step, i,
                        "field_" + FIELD_NAME_TAG));
                _inputFields[i].setType(ValueMeta.getType(rep
                        .getStepAttributeString(id_step, i, "field_"
                                + FIELD_TYPE_TAG)));
                _inputFields[i].setLength((int) rep.getStepAttributeInteger(
                        id_step, i, "field_" + FIELD_LENGTH_TAG));
                _inputFields[i].setPrecision((int) rep.getStepAttributeInteger(
                        id_step, i, "field_" + FIELD_PRECISION_TAG));
                _inputFields[i].setTrimType(ValueMeta.getTrimTypeByCode(rep
                        .getStepAttributeString(id_step, i, "field_"
                                + FIELD_TRIM_TYPE_TAG)));
                _inputFields[i].setRedefined(rep.getStepAttributeBoolean(
                        id_step, i, "field_" + FIELD_REDEFINED_TAG));
            }
        } catch (Exception e) {
            throw new KettleException(
                    getI18N("ZosFileInputMeta.RepPersistence.FailedToLoadStepInfo"),
                    e);
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation,
            ObjectId id_step) throws KettleException {
        try {
            rep.saveStepAttribute(id_transformation, id_step, FILE_NAME_TAG,
                    _filename);
            rep.saveStepAttribute(id_transformation, id_step,
                    IS_VARIABLE_LENGTH_TAG, _isVariableLength);
            rep.saveStepAttribute(id_transformation, id_step,
                    HAS_RECORD_DESCRIPTOR_WORD_TAG, _hasRecordDescriptorWord);
            rep.saveStepAttribute(id_transformation, id_step, HOST_CHARSET_TAG,
                    _hostCharset);

            rep.saveStepAttribute(id_transformation, id_step,
                    IS_FROM_COBOL_SOURCE, _isFromCobolSource);
            rep.saveStepAttribute(id_transformation, id_step,
                    COMPOSITE_JAXB_CLASS_NAME_TAG, _compositeJaxbClassName);
            rep.saveStepAttribute(id_transformation, id_step, COBOL_SOURCE_TAG,
                    _cobolSource);
            rep.saveStepAttribute(id_transformation, id_step,
                    COBOL_CHARSET_TAG, _cobolCharset);
            rep.saveStepAttribute(id_transformation, id_step,
                    COBOL_FILE_PATH_TAG, _cobolFilePath);

            for (int i = 0; i < _inputFields.length; i++) {
                CobolFileInputField field = _inputFields[i];

                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_NAME_TAG, field.getName());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_TYPE_TAG,
                        ValueMeta.getTypeDesc(field.getType()));
                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_LENGTH_TAG, field.getLength());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_PRECISION_TAG, field.getPrecision());
                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_TRIM_TYPE_TAG,
                        ValueMeta.getTrimTypeCode(field.getTrimType()));
                rep.saveStepAttribute(id_transformation, id_step, i, "field_"
                        + FIELD_REDEFINED_TAG, field.isRedefined());
            }
        } catch (Exception e) {
            throw new KettleException(
                    getI18N("ZosFileInputMeta.RepPersistence.FailedToSaveStepInfo")
                            + id_step, e);
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Defines what gets sent. A row is a flat sequence of fields.
     * ------------------------------------------------------------------------
     */

    public void getFields(RowMetaInterface rowMeta, String origin,
            RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) {

        CobolToPdi.fieldsToRowMeta(_inputFields, origin, rowMeta);
    }

    /*
     * ------------------------------------------------------------------------
     * Check sanity of step position in a transformation context.
     * ------------------------------------------------------------------------
     */

    public void check(List<CheckResultInterface> remarks, TransMeta transmeta,
            StepMeta stepMeta, RowMetaInterface prev, String input[],
            String output[], RowMetaInterface info) {
        CheckResult cr;

        // See if we have input streams leading to this step!
        if (input.length > 0) {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                    getI18N("ZosFileInputMeta.CheckResult.StepReceivingData"),
                    stepMeta);
            remarks.add(cr);
        } else {
            cr = new CheckResult(
                    CheckResult.TYPE_RESULT_OK,
                    getI18N("ZosFileInputMeta.CheckResult.NoInputReceivedFromOtherSteps"),
                    stepMeta);
            remarks.add(cr);
        }

    }

    /*
     * ------------------------------------------------------------------------
     * Return associated Step implementation and StepData.
     * ------------------------------------------------------------------------
     */

    public StepInterface getStep(StepMeta stepMeta,
            StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
            Trans disp) {
        return new ZosFileInput(stepMeta, stepDataInterface, cnr, transMeta,
                disp);
    }

    public StepDataInterface getStepData() {
        return new ZosFileInputData();
    }

    /*
     * ------------------------------------------------------------------------
     * I18N methods.
     * ------------------------------------------------------------------------
     */

    /**
     * Shortcut to I18N messages.
     * 
     * @param key the message identifier
     * @param parameters parameters if any
     * @return the localized message
     */
    public static String getI18N(final String key, final String... parameters) {
        return BaseMessages.getString(PKG, key, parameters);
    }

    /*
     * ------------------------------------------------------------------------
     * Java object overrides.
     * ------------------------------------------------------------------------
     */
    public Object clone() {
        Object retval = super.clone();
        return retval;
    }

}

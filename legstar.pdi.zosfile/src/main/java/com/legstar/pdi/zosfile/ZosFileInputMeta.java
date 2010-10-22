package com.legstar.pdi.zosfile;

import java.util.List;
import java.util.Map;


import org.pentaho.di.core.*;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

import com.legstar.pdi.CobolFileInputField;
import com.legstar.pdi.CobolToPdi;

/**
 * This is a Kettle step implementation class. Acts as a model and knows how to
 * serialize itself both in XML or a Kettle repository. TODO add host character
 * set
 */
public class ZosFileInputMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = ZosFileInputMeta.class; // for i18n purposes

	/** The JAXB qualified class name. */
	private String _jaxbQualifiedClassName;

	/** Used to serialize/deserialize the JAXB qualified class name from XML. */
	public static final String JAXBQUALIFIEDCLASSNAME_TAG = "jaxbqualifiedclassname";

	/** The local copy of the z/OS file. */
	private String _filename;

	/** Used to serialize/deserialize the file name from XML. */
	public static final String FILENAME_TAG = "filename";

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
	 * @param filename
	 *            the local copy of the z/OS file to set
	 */
	public void setFilename(String filename) {
		_filename = filename;
	}

	/**
	 * @return the inputFields
	 */
	public CobolFileInputField[] getInputFields() {
		return _inputFields;
	}

	/**
	 * @param inputFields
	 *            the inputFields to set
	 */
	public void setInputFields(CobolFileInputField[] inputFields) {
		_inputFields = inputFields;
	}

	/**
	 * @return the JAXB class name
	 */
	public String getJaxbQualifiedClassName() {
		return _jaxbQualifiedClassName;
	}

	/**
	 * @param jaxbQualifiedClassName
	 *            the JAXB class name to set
	 */
	public void setJaxbQualifiedClassName(String jaxbQualifiedClassName) {
		_jaxbQualifiedClassName = jaxbQualifiedClassName;
	}

	/*
	 * ------------------------------------------------------------------------
	 * Initial bean properties values
	 * ------------------------------------------------------------------------
	 */
	public void setDefault() {
		_inputFields = new CobolFileInputField[0];
	}

	/*
	 * ------------------------------------------------------------------------
	 * XML Serialization section
	 * ------------------------------------------------------------------------
	 */

	/** {@inheritDoc} */
	public String getXML() throws KettleValueException {
		StringBuffer retval = new StringBuffer();
		retval.append("    ").append(
				XMLHandler.addTagValue(JAXBQUALIFIEDCLASSNAME_TAG, _jaxbQualifiedClassName));
		retval.append("    ").append(
				XMLHandler.addTagValue(FILENAME_TAG, _filename));
		retval.append("    <fields>").append(Const.CR);
		for (int i = 0; i < _inputFields.length; i++) {
			CobolFileInputField field = _inputFields[i];

			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(
					XMLHandler.addTagValue("name", field.getName()));
			retval.append("        ").append(
					XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(field
							.getType())));
			retval.append("        ").append(
					XMLHandler.addTagValue("format", field.getFormat()));
			retval.append("        ").append(
					XMLHandler.addTagValue("currency", field
							.getCurrencySymbol()));
			retval.append("        ")
					.append(
							XMLHandler.addTagValue("decimal", field
									.getDecimalSymbol()));
			retval.append("        ").append(
					XMLHandler.addTagValue("group", field.getGroupSymbol()));
			retval.append("        ").append(
					XMLHandler.addTagValue("length", field.getLength()));
			retval.append("        ").append(
					XMLHandler.addTagValue("precision", field.getPrecision()));
			retval.append("        ").append(
					XMLHandler.addTagValue("trim_type", ValueMeta
							.getTrimTypeCode(field.getTrimType())));
			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);
		return retval.toString();
	}

	/**
	 * Load the values for this step from an XML Node
	 * 
	 * @param stepnode
	 *            the Node to get the info from
	 * @param databases
	 *            The available list of databases to reference to
	 * @param counters
	 *            Counters to reference.
	 * @throws KettleXMLException
	 *             When an unexpected XML error occurred. (malformed etc.)
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,
			Map<String, Counter> counters) throws KettleXMLException {

		try {
			_filename = XMLHandler.getTagValue(stepnode, FILENAME_TAG);
			_jaxbQualifiedClassName = XMLHandler
					.getTagValue(stepnode, JAXBQUALIFIEDCLASSNAME_TAG);
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nFields = XMLHandler.countNodes(fields, "field");

			_inputFields = new CobolFileInputField[nFields];
			for (int i = 0; i < nFields; i++) {
				_inputFields[i] = new CobolFileInputField();

				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

				_inputFields[i].setName(XMLHandler.getTagValue(fnode, "name"));
				_inputFields[i].setType(ValueMeta.getType(XMLHandler
						.getTagValue(fnode, "type")));
				_inputFields[i].setFormat(XMLHandler.getTagValue(fnode,
						"format"));
				_inputFields[i].setCurrencySymbol(XMLHandler.getTagValue(fnode,
						"currency"));
				_inputFields[i].setDecimalSymbol(XMLHandler.getTagValue(fnode,
						"decimal"));
				_inputFields[i].setGroupSymbol(XMLHandler.getTagValue(fnode,
						"group"));
				_inputFields[i].setLength(Const.toInt(XMLHandler.getTagValue(
						fnode, "length"), -1));
				_inputFields[i].setPrecision(Const.toInt(XMLHandler
						.getTagValue(fnode, "precision"), -1));
				_inputFields[i].setTrimType(ValueMeta
						.getTrimTypeByCode(XMLHandler.getTagValue(fnode,
								"trim_type")));
			}
		} catch (Exception e) {
			throw new KettleXMLException("Unable to load step info from XML", e);
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
			_jaxbQualifiedClassName = rep.getStepAttributeString(id_step,
					JAXBQUALIFIEDCLASSNAME_TAG);
			_filename = rep.getStepAttributeString(id_step, FILENAME_TAG);
			int nFields = rep.countNrStepAttributes(id_step, "field_name");

			_inputFields = new CobolFileInputField[nFields];

			for (int i = 0; i < nFields; i++) {
				_inputFields[i] = new CobolFileInputField();

				_inputFields[i].setName(rep.getStepAttributeString(id_step, i,
						"field_name"));
				_inputFields[i].setType(ValueMeta.getType(rep
						.getStepAttributeString(id_step, i, "field_type")));
				_inputFields[i].setFormat(rep.getStepAttributeString(id_step,
						i, "field_format"));
				_inputFields[i].setCurrencySymbol(rep.getStepAttributeString(
						id_step, i, "field_currency"));
				_inputFields[i].setDecimalSymbol(rep.getStepAttributeString(
						id_step, i, "field_decimal"));
				_inputFields[i].setGroupSymbol(rep.getStepAttributeString(
						id_step, i, "field_group"));
				_inputFields[i].setLength((int) rep.getStepAttributeInteger(
						id_step, i, "field_length"));
				_inputFields[i].setPrecision((int) rep.getStepAttributeInteger(
						id_step, i, "field_precision"));
				_inputFields[i]
						.setTrimType(ValueMeta.getTrimTypeByCode(rep
								.getStepAttributeString(id_step, i,
										"field_trim_type")));
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"TemplateStep.Exception.UnexpectedErrorInReadingStepInfo"),
					e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation,
			ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step,
					JAXBQUALIFIEDCLASSNAME_TAG, _jaxbQualifiedClassName);
			rep.saveStepAttribute(id_transformation, id_step, FILENAME_TAG,
					_filename);
			for (int i = 0; i < _inputFields.length; i++) {
				CobolFileInputField field = _inputFields[i];

				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_name", field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_type", ValueMeta.getTypeDesc(field.getType()));
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_format", field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_currency", field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_decimal", field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_group", field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_length", field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_precision", field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i,
						"field_trim_type", ValueMeta.getTrimTypeCode(field
								.getTrimType()));
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG,
					"TemplateStep.Exception.UnableToSaveStepInfoToRepository")
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages
					.getString(PKG,
							"ZosFileInputMeta.CheckResult.StepReceivingData"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(
					CheckResult.TYPE_RESULT_OK,
					BaseMessages
							.getString(PKG,
									"ZosFileInputMeta.CheckResult.NoInputReceivedFromOtherSteps"),
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
	 * Java object overrides.
	 * ------------------------------------------------------------------------
	 */
	public Object clone() {
		Object retval = super.clone();
		return retval;
	}

}

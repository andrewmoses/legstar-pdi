package com.legstar.pdi;

import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * A COBOL field is just a text field with some additional properties
 * such as redefines.
 *
 */
public class CobolFileInputField extends TextFileInputField {

	/** If the field is redefined.*/
	private boolean _redefined;

	/**
	 * @return true if field is redefined
	 */
	public boolean isRedefined() {
		return _redefined;
	}

	/**
	 * @param redefined true if field is redefined
	 */
	public void setRedefined(boolean redefined) {
		_redefined = redefined;
	}
	
	

}

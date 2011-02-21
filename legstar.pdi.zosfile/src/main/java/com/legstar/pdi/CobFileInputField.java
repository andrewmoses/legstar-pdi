package com.legstar.pdi;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

/**
 * A COBOL field is just a text field with some additional properties such as
 * redefines.
 * 
 */
public class CobFileInputField extends TextFileInputField {

    /** If the field is redefined. */
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

    /** {@inheritDoc} */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("name:\"" + getName() + "\"");
        sb.append(",type:\"" + resolveType(getType()) + "\"");
        if (getLength() > -1) {
            sb.append(",length:" + getLength());
        }
        if (getPrecision() > -1) {
            sb.append(",precision:" + getPrecision());
        }
        if (isRedefined()) {
            sb.append(",redefined:true");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * @param type the field type
     * @return a readable type
     */
    public String resolveType(final int type) {
        return ValueMetaInterface.typeCodes[type];
    }

}

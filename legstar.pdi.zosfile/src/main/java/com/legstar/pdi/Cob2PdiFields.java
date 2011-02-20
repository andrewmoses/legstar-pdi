package com.legstar.pdi;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;

import com.legstar.coxb.CobolBindingException;
import com.legstar.coxb.ICobolArrayBinding;
import com.legstar.coxb.ICobolArrayComplexBinding;
import com.legstar.coxb.ICobolArrayDbcsBinding;
import com.legstar.coxb.ICobolArrayDoubleBinding;
import com.legstar.coxb.ICobolArrayFloatBinding;
import com.legstar.coxb.ICobolArrayNationalBinding;
import com.legstar.coxb.ICobolArrayNumericBinding;
import com.legstar.coxb.ICobolArrayOctetStreamBinding;
import com.legstar.coxb.ICobolArrayStringBinding;
import com.legstar.coxb.ICobolBinding;
import com.legstar.coxb.ICobolChoiceBinding;
import com.legstar.coxb.ICobolComplexBinding;
import com.legstar.coxb.ICobolDbcsBinding;
import com.legstar.coxb.ICobolDoubleBinding;
import com.legstar.coxb.ICobolFloatBinding;
import com.legstar.coxb.ICobolNationalBinding;
import com.legstar.coxb.ICobolNumericBinding;
import com.legstar.coxb.ICobolOctetStreamBinding;
import com.legstar.coxb.ICobolStringBinding;
import com.legstar.coxb.host.HostException;
import com.legstar.coxb.impl.reflect.CComplexReflectBinding;
import com.legstar.coxb.util.BindingUtil;
import com.legstar.coxb.util.ClassUtil;
import com.legstar.coxb.util.ClassUtil.ClassName;

/**
 * Translates a COBOL Structure to PDI field sets.
 * <p/>
 * A PDI field set is flat and does not support arrays. It is similar to a
 * database row.
 */
public class Cob2PdiFields {

    /**
     * Utility class. No instantiation.
     */
    private Cob2PdiFields() {

    }

    /**
     * Flattens a COBOL structure so that fields become columns in a row.
     * <p/>
     * For PDI, a row (in the sense of a table row) is the structure that gets
     * transmitted over a hop from step to step.
     * <p/>
     * Here we temporarily setup a ClassLoader using the jar archive that
     * contains the Transformer code.
     * 
     * @param compositeJaxbClassName the JAXB class name and containing jar
     * @param clazz the caller class to use as a parent ClassLoader to the one
     *        we temporarily create
     * @return an array of fields as if originating from a text file
     * @throws KettleException if failed to get the COBOL structure info from
     *         JAXB
     */
    public static CobolFileInputField[] getCobolFields(
            final String compositeJaxbClassName, final Class<?> clazz)
            throws KettleException {

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            CobolToPdi.setTransformerClassLoader(clazz,
                    CobolToPdi.getJarFileName(compositeJaxbClassName));
            List<CobolFileInputField> fields = toFields(CobolToPdi
                    .getJaxbClassName(compositeJaxbClassName));
            return fields.toArray(new CobolFileInputField[fields.size()]);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * Flattens a COBOL structure so that fields become columns in a row.
     * <p/>
     * For PDI, a row (in the sense of a table row) is the structure that gets
     * transmitted over a hop from step to step.
     * <p/>
     * 
     * @param jaxbQualifiedClassName the JAXB class name
     * @return a list of fields as if originating from a text file
     * @throws KettleException if failed to get the COBOL structure info from
     *         JAXB
     */
    public static List<CobolFileInputField> toFields(
            final String jaxbQualifiedClassName) throws KettleException {
        try {
            ClassName className = ClassUtil.toClassName(jaxbQualifiedClassName);
            Object jaxbObjectFactory = BindingUtil
                    .newJaxbObjectFactory(className.packageName);
            Object jaxbObject = BindingUtil.newJaxbObject(jaxbObjectFactory,
                    className.className);
            return toFields(jaxbObjectFactory, jaxbObject);
        } catch (CobolBindingException e) {
            throw new KettleException(e);
        }
    }

    /**
     * Flattens a COBOL structure so that fields become columns in a row.
     * <p/>
     * For PDI, a row (in the sense of a table row) is the structure that gets
     * transmitted over a hop from step to step.
     * <p/>
     * 
     * @param jaxbObjectFactory the JAXB object factory
     * @param jaxbObject the concrete JAXB object instance bound to this complex
     *        element
     * @return a list of fields as if originating from a text file
     * @throws KettleException if failed to get the COBOL structure info from
     *         JAXB
     */
    public static List<CobolFileInputField> toFields(
            final Object jaxbObjectFactory, final Object jaxbObject)
            throws KettleException {

        try {
            CComplexReflectBinding root = new CComplexReflectBinding(
                    jaxbObjectFactory, jaxbObject);
            List<CobolFileInputField> fields = new ArrayList<CobolFileInputField>();
            toFields(fields, root, null, null, false);
            return fields;
        } catch (HostException e) {
            throw new KettleException(e);
        }

    }

    /**
     * Recursively adds fields to the fields list.
     * <p/>
     * Field names may be prefixed with the parent name to avoid name conflicts.
     * <p/>
     * Arrays are split in the maximum number of items, each one being named
     * after the array name and suffixed with its index within the array.
     * <p/>
     * Choices (redefines) result in alternatives being ignored. This is because
     * PDI does not allow the list of fields to be redefined dynamically based
     * on content. So we cannot use LegStar redefines handling strategies.
     * 
     * @param fields a list of fields being populated
     * @param binding the current COBOL data item
     * @param prefix the parents names (space if root)
     * @param suffix the index for array items
     * @param redefined true if this field is in a redefined sub-structure
     * @throws HostException if COBOL introspection fails
     */
    public static void toFields(final List<CobolFileInputField> fields,
            final ICobolBinding binding, final String prefix,
            final String suffix, final boolean redefined) throws HostException {

        if (binding instanceof ICobolComplexBinding) {
            for (ICobolBinding child : ((ICobolComplexBinding) binding)
                    .getChildrenList()) {
                String newPrefix = (prefix == null) ? ""
                        : (prefix.length() == 0 ? binding.getBindingName()
                                : prefix + "_" + binding.getBindingName());
                toFields(fields, child, newPrefix, suffix, redefined);
            }
        } else if (binding instanceof ICobolChoiceBinding) {
            /*
             * Opt to process the first alternative only and mark all
             * descendants as redefined.
             */
            String newPrefix = (prefix == null) ? ""
                    : (prefix.length() == 0 ? binding.getBindingName() : prefix
                            + "_" + binding.getBindingName());
            toFields(fields, ((ICobolChoiceBinding) binding)
                    .getAlternativesList().get(0), newPrefix, suffix, true);
        } else if (binding instanceof ICobolArrayBinding) {
            for (int i = 0; i < ((ICobolArrayBinding) binding).getMaxOccurs(); i++) {
                String newSuffix = ((suffix == null) ? "_" : suffix + "_") + i;
                if (binding instanceof ICobolArrayComplexBinding) {
                    toFields(fields,
                            ((ICobolArrayComplexBinding) binding)
                                    .getComplexItemBinding(), prefix,
                            newSuffix, redefined);
                } else {
                    fields.add(toField(fields, binding, prefix, newSuffix,
                            redefined));
                }
            }
        } else {
            fields.add(toField(fields, binding, prefix, suffix, redefined));
        }

    }

    /**
     * For elementary COBOL data items, this translates COBOL properties to PDI
     * field properties.
     * 
     * @param binding the elementary COBOL data item
     * @param prefix the parents names (space if root)
     * @param suffix the index for array items
     * @param redefined true if this field is in a redefined sub-structure
     * @return a PDI field
     */
    public static CobolFileInputField toField(
            final List<CobolFileInputField> fields,
            final ICobolBinding binding, final String prefix,
            final String suffix, final boolean redefined) {

        CobolFileInputField field = new CobolFileInputField();
        field.setName(newName(fields, binding.getBindingName(), prefix, suffix));

        if (binding instanceof ICobolNumericBinding
                || binding instanceof ICobolArrayNumericBinding) {
            field.setPrecision(binding.getFractionDigits());
        } else {
            field.setPrecision(-1);
        }
        field.setLength(-1);
        field.setTrimType(ValueMetaInterface.TRIM_TYPE_NONE);

        if (binding instanceof ICobolStringBinding) {
            field.setType(ValueMetaInterface.TYPE_STRING);
            field.setLength((binding.getByteLength()));
            field.setTrimType(ValueMetaInterface.TRIM_TYPE_RIGHT);

        } else if (binding instanceof ICobolArrayStringBinding) {
            field.setType(ValueMetaInterface.TYPE_STRING);
            field.setLength(((ICobolArrayBinding) binding).getItemByteLength());
            field.setTrimType(ValueMetaInterface.TRIM_TYPE_RIGHT);

        } else if (binding instanceof ICobolNumericBinding
                || binding instanceof ICobolArrayNumericBinding) {
            if (binding.getFractionDigits() > 0) {
                field.setType(ValueMetaInterface.TYPE_BIGNUMBER);
            } else {
                field.setType(ValueMetaInterface.TYPE_INTEGER);
            }

        } else if (binding instanceof ICobolDoubleBinding
                || binding instanceof ICobolArrayDoubleBinding) {
            field.setType(ValueMetaInterface.TYPE_NUMBER);

        } else if (binding instanceof ICobolFloatBinding
                || binding instanceof ICobolArrayFloatBinding) {
            field.setType(ValueMetaInterface.TYPE_NUMBER);

        } else if (binding instanceof ICobolDbcsBinding
                || binding instanceof ICobolNationalBinding) {
            field.setType(ValueMetaInterface.TYPE_STRING);
            field.setLength((binding.getByteLength() / 2));
            field.setTrimType(ValueMetaInterface.TRIM_TYPE_RIGHT);

        } else if (binding instanceof ICobolArrayDbcsBinding
                || binding instanceof ICobolArrayNationalBinding) {
            field.setType(ValueMetaInterface.TYPE_STRING);
            field.setLength(((ICobolArrayBinding) binding).getItemByteLength() / 2);
            field.setTrimType(ValueMetaInterface.TRIM_TYPE_RIGHT);

        } else if (binding instanceof ICobolOctetStreamBinding) {
            field.setType(ValueMetaInterface.TYPE_BINARY);
            field.setLength((binding.getByteLength()));

        } else if (binding instanceof ICobolArrayOctetStreamBinding) {
            field.setType(ValueMetaInterface.TYPE_BINARY);
            field.setLength(((ICobolArrayBinding) binding).getItemByteLength());

        } else {
            field.setType(ValueMetaInterface.TYPE_NONE);
        }
        field.setFormat("");
        field.setCurrencySymbol("");
        field.setDecimalSymbol("");
        field.setGroupSymbol("");
        field.setRedefined(redefined);

        return field;
    }

    /**
     * Creates a new column name based on the COXB field name.
     * <p/>
     * The proposed name is as simple as possible but we need to avoid any name
     * conflicts (a column must have a unique name) so we check for any conflict
     * and use a prefix system to disambiguate names.
     * 
     * @param fields the list of fields already named
     * @param binding the COXB field
     * @param prefix the maximum prefix
     * @param suffix an optional suffix (array items)
     * @return a unique name for a column
     */
    protected static String newName(final List<CobolFileInputField> fields,
            final String bindingName, final String prefix, final String suffix) {

        // Always add suffix if any (array item number)
        String name = bindingName + ((suffix == null) ? "" : suffix);

        // nothing to disambiguate with
        if (Const.isEmpty(prefix)) {
            return name;
        }
        String[] prefixes = prefix.split("_");
        int pos = prefixes.length - 1;

        while (nameConflict(name, fields) && pos > -1) {
            name = prefixes[pos] + '_' + name;
            pos--;
        }

        return name;

    }

    /**
     * Determines if a proposed name is already used.
     * 
     * @param name the proposed name
     * @param fields the list of fields already named
     * @return true if name already used
     */
    protected static boolean nameConflict(final String name,
            final List<CobolFileInputField> fields) {
        for (CobolFileInputField field : fields) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}

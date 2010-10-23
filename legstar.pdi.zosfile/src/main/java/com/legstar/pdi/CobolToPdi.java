package com.legstar.pdi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.KettleURLClassLoader;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.scannotation.AnnotationDB;

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
import com.legstar.coxb.impl.reflect.ReflectBindingException;
import com.legstar.coxb.transform.AbstractTransformer;
import com.legstar.coxb.transform.AbstractTransformers;
import com.legstar.coxb.transform.HostTransformException;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.coxb.transform.IHostToJavaTransformer;

/**
 * This is a Mediator between PDI objects and LegStar Objects.
 * </p>
 * The idea is to keep the PDI step code as simple as possible and
 * not leak too much LegStar specific code into the step code.
 * 
 */
public class CobolToPdi {

    /** An identifier for our lib class loader.*/
    public static final String LIB_CLASSLOADER_NAME = "legstar.pdi.lib";
    
    /** The default plugin location. */
    public static final String DEFAULT_PLUGIN_FOLDER = "plugins/steps/legstar.pdi.zosfile";
    
    /** The relative location of our private lib folder. */
    public static final String LIB_FOLDER = "lib";
    
    /** The JAXB/COBOL Legstar classes should be annotated with this. */
    public static final String LEGSTAR_ANNOTATIONS = "com.legstar.coxb.CobolElement";

	/**
	 * Utility class. No instantiation.
	 */
	private CobolToPdi() {

	}

	/**
	 * Flattens a COBOL structure so that fields become columns in a row.
	 * <p/>
	 * For PDI, a row (in the sense of a table row) is the structure that gets
	 * transmitted over a hop from step to step.
	 * <p/>
	 * 
	 * @param jaxbQualifiedClassName  the JAXB class name
	 * @return an array of fields as if originating from a text file
	 * @throws KettleException
	 *             if failed to get the COBOL structure info from JAXB
	 */
	public static CobolFileInputField[] toFieldArray(
			final String jaxbQualifiedClassName) throws KettleException {

		List<CobolFileInputField> fields = toFields(jaxbQualifiedClassName);
		return fields.toArray(new CobolFileInputField[fields.size()]);
	}

	/**
	 * Flattens a COBOL structure so that fields become columns in a row.
	 * <p/>
	 * For PDI, a row (in the sense of a table row) is the structure that gets
	 * transmitted over a hop from step to step.
	 * <p/>
	 * 
	 * @param jaxbQualifiedClassName  the JAXB class name
	 * @return a list of fields as if originating from a text file
	 * @throws KettleException
	 *             if failed to get the COBOL structure info from JAXB
	 */
	public static List<CobolFileInputField> toFields(
			final String jaxbQualifiedClassName)
			throws KettleException {
		try {
			ClassName className = toClassName(jaxbQualifiedClassName);
			Object jaxbObjectFactory = BindingReflectHelper
					.newJaxbObjectFactory(className.packageName);
			Object jaxbObject = BindingReflectHelper.newJaxbObject(
					jaxbObjectFactory, className.className);
			return toFields(jaxbObjectFactory, jaxbObject);
		} catch (ReflectBindingException e) {
			throw new KettleException(e);
		}
	}

	/**
	 * Create an instance of Transformers for a given JAXB root class name.
	 * Assumes binding classes were generated for this JAXB class.
	 * TODO reuse COXBGEN code for COXB package name and Transformers name.
	 * @param jaxbPackageName the JAXB package name
	 * @param jaxbClassName the JAXB root class name
	 * @return a new instance of Transformers
	 * @throws KettleException if transformers cannot be created
	 */
	public static AbstractTransformers newTransformers(
			final String jaxbQualifiedClassName) throws KettleException {
		try {
			ClassName className = toClassName(jaxbQualifiedClassName);
			return (AbstractTransformers) BindingReflectHelper.newTransformers(
					className.packageName, className.className);
		} catch (ReflectBindingException e) {
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
	 * @param jaxbObjectFactory
	 *            the JAXB object factory
	 * @param jaxbObject
	 *            the concrete JAXB object instance bound to this complex
	 *            element
	 * @return a list of fields as if originating from a text file
	 * @throws KettleException
	 *             if failed to get the COBOL structure info from JAXB
	 */
	public static List<CobolFileInputField> toFields(
			final Object jaxbObjectFactory,
			final Object jaxbObject) throws KettleException {

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
	 * Field names are prefixed with the parent name to avoid name conflicts.
	 * <p/>
	 * Arrays are split in the maximum number of items, each one being named
	 * after the array name and suffixed with its index within the array.
	 * <p/>
	 * Choices (redefines) are not handled and result in alternatives being ignored.
	 * This is because PDI does not allow the list of fields to be redefined dynamically
	 * based on content. So we cannot use LegStar redefines handling strategies.
	 * 
	 * @param fields
	 *            a list of fields being populated
	 * @param binding
	 *            the current COBOL data item
	 * @param prefix
	 *            the parents names (space if root)
	 * @param suffix
	 *            the index for array items
	 * @param redefined true if this field is in a redefined sub-structure
	 * @throws HostException
	 *             if COBOL introspection fails
	 */
	public static void toFields(final List<CobolFileInputField> fields,
			final ICobolBinding binding, final String prefix,
			final String suffix,
			final boolean redefined) throws HostException {

		if (binding instanceof ICobolComplexBinding) {
			for (ICobolBinding child : ((ICobolComplexBinding) binding)
					.getChildrenList()) {
				String newPrefix = (prefix == null) ? ""
						: (prefix.isEmpty() ? binding.getBindingName() : prefix
								+ "_" + binding.getBindingName());
				toFields(fields, child, newPrefix, suffix, redefined);
			}
		} else if (binding instanceof ICobolChoiceBinding) {
			/* Opt to process the first alternative only and mark
			 * all descendants as redefined. */
			String newPrefix = (prefix == null) ? ""
					: (prefix.isEmpty() ? binding.getBindingName() : prefix
							+ "_" + binding.getBindingName());
			toFields(fields, ((ICobolChoiceBinding) binding)
					.getAlternativesList().get(0), newPrefix, suffix, true);
		} else if (binding instanceof ICobolArrayBinding) {
			for (int i = 0; i < ((ICobolArrayBinding) binding).getMaxOccurs(); i++) {
				String newSuffix = ((suffix == null) ? "_" : suffix + "_") + i;
				if (binding instanceof ICobolArrayComplexBinding) {
					toFields(fields, ((ICobolArrayComplexBinding) binding)
							.getComplexItemBinding(), prefix, newSuffix, redefined);
				} else {
					fields.add(toField(binding, prefix, newSuffix, redefined));
				}
			}
		} else {
			fields.add(toField(binding, prefix, suffix, redefined));
		}

	}

	/**
	 * For elementary COBOL data items, this translates COBOL properties to PDI
	 * field properties.
	 * 
	 * @param binding
	 *            the elementary COBOL data item
	 * @param prefix
	 *            the parents names (space if root)
	 * @param suffix
	 *            the index for array items
	 * @param redefined true if this field is in a redefined sub-structure
	 * @return a PDI field
	 */
	public static CobolFileInputField toField(
			final ICobolBinding binding,
			final String prefix,
			final String suffix,
			final boolean redefined) {
		CobolFileInputField field = new CobolFileInputField();
		field
				.setName(((prefix == null || prefix.isEmpty()) ? ""
						: prefix + '_')
						+ binding.getBindingName()
						+ ((suffix == null) ? "" : suffix));

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
			field.setLength(((ICobolArrayBinding) binding)
							.getItemByteLength() / 2);
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
	 * Calculates the maximum byte array length of host data for a LegStar
	 * binding.
	 * 
	 * @param tf
	 *            the LegStar transformers associated with the binding
	 * @return the maximum byte array length of host data
	 * @throws KettleException
	 *             if binding cannot be accessed
	 */
	public static int hostByteLength(final AbstractTransformers tf)
			throws KettleException {
		try {
			return tf.getHostToJava().getBinding().getByteLength();
		} catch (CobolBindingException e) {
			throw new KettleException(e);
		}
	}

	/**
	 * Generates row meta structure from a fields array.
	 * 
	 * @param fields
	 *            the fields array
	 * @param origin
	 *            the data origin
	 * @param rowMeta
	 *            the row meta to generate
	 */
	public static void fieldsToRowMeta(final CobolFileInputField[] fields,
			final String origin, final RowMetaInterface rowMeta) {

		rowMeta.clear(); // Start with a clean slate, eats the input

		for (int i = 0; i < fields.length; i++) {
			CobolFileInputField field = fields[i];

			ValueMetaInterface valueMeta = new ValueMeta(field.getName(), field
					.getType());
			valueMeta.setConversionMask(field.getFormat());
			valueMeta.setLength(field.getLength());
			valueMeta.setPrecision(field.getPrecision());
			valueMeta.setConversionMask(field.getFormat());
			valueMeta.setDecimalSymbol(field.getDecimalSymbol());
			valueMeta.setGroupingSymbol(field.getGroupSymbol());
			valueMeta.setCurrencySymbol(field.getCurrencySymbol());
			valueMeta.setTrimType(field.getTrimType());
			valueMeta.setOrigin(origin);

			rowMeta.addValueMeta(valueMeta);
		}
	}

    /**
     * Creates a PDI output row from host data.
     * 
     * @param outputRowMeta
     *            the output row meta data.
     * @param tf
     *            a host transformer
     * @param hostRecord
     *            the host data
     * @param status
     *            additional info on the COBOL to Java transformation process
     * @return a PDI output row of data
     * @throws KettleException
     *             if transformation fails
     */
	public static Object[] toOutputRowData(
			final RowMetaInterface outputRowMeta,
			final AbstractTransformers tf,
			final byte[] hostRecord,
			final HostTransformStatus status)
			throws KettleException {
		try {
		    int expectedOutputRows = outputRowMeta.getFieldNames().length;
			IHostToJavaTransformer h2j = tf.getHostToJava();
			h2j.transform(hostRecord, status);
			ICobolComplexBinding binding = ((AbstractTransformer) h2j)
					.getCachedBinding();

			List<Object> objects = new ArrayList<Object>();
			toObjects(objects, binding, -1);
            /* PDI does not support variable size arrays. Need to fill all columns.*/
            for (int i = objects.size(); i < expectedOutputRows; i++) {
                objects.add(null);
            }

			return objects.toArray(new Object[objects.size()]);

		} catch (CobolBindingException e) {
			throw new KettleException(e);
		} catch (HostTransformException e) {
			throw new KettleException(e);
		} catch (HostException e) {
			throw new KettleException(e);
		}
	}

	/**
	 * Recursively populate object values.
	 * 
	 * @param objects
	 *            a list of object values being populated
	 * @param binding
	 *            the current COBOL data item
	 * @param index
	 *            the index for array items
	 * @throws HostException
	 *             if COBOL introspection fails
	 */
	public static void toObjects(
			final List<Object> objects,
			final ICobolBinding binding,
			final int index) throws HostException {

		if (binding instanceof ICobolComplexBinding) {
			for (ICobolBinding child : ((ICobolComplexBinding) binding)
					.getChildrenList()) {
				toObjects(objects, child, index);
			}
		} else if (binding instanceof ICobolChoiceBinding) {
			// Opt to process the first alternative only
			toObjects(objects, ((ICobolChoiceBinding) binding)
					.getAlternativesList().get(0), index);
		} else if (binding instanceof ICobolArrayBinding) {
			ICobolArrayBinding arrayBinding = (ICobolArrayBinding) binding;
			for (int i = 0; i < arrayBinding.getCurrentOccurs(); i++) {
				if (binding instanceof ICobolArrayComplexBinding) {
					toObjects(objects, ((ICobolArrayComplexBinding) binding)
							.getComplexItemBinding(), i);
				} else {
					objects.add(toObject(binding, i));
				}
			}
		} else {
			objects.add(toObject(binding, -1));
		}
	}

	/**
	 * For elementary COBOL data items, this translates COBOL values to PDI
	 * field values.
	 * 
	 * @param binding
	 *            the elementary COBOL data item
	 * @param index
	 *            the index to retrieve if element is an array
	 * @return a PDI value
	 * @throws HostException if values cannot be retrieved
	 */
	public static Object toObject(final ICobolBinding binding, final int index)
			throws HostException {

		if (binding instanceof ICobolStringBinding) {
			return ((ICobolStringBinding) binding).getStringValue();

		} else if (binding instanceof ICobolArrayStringBinding) {
			return ((ICobolArrayStringBinding) binding).getStringList().get(
					index);

		} else if (binding instanceof ICobolNumericBinding) {
			if (binding.getFractionDigits() > 0) {
				return ((ICobolNumericBinding) binding).getBigDecimalValue();
			} else {
				return ((ICobolNumericBinding) binding).getLongValue();
			}

		} else if (binding instanceof ICobolArrayNumericBinding) {
			if (binding.getFractionDigits() > 0) {
				return ((ICobolArrayNumericBinding) binding)
						.getBigDecimalList().get(index);
			} else {
				return ((ICobolArrayNumericBinding) binding).getLongList().get(
						index);
			}

		} else if (binding instanceof ICobolDoubleBinding) {
			return ((ICobolDoubleBinding) binding).getDoubleValue();

		} else if (binding instanceof ICobolArrayDoubleBinding) {
			return ((ICobolArrayDoubleBinding) binding).getDoubleList().get(
					index);

		} else if (binding instanceof ICobolFloatBinding) {
			return ((ICobolFloatBinding) binding).getFloatValue();

		} else if (binding instanceof ICobolArrayFloatBinding) {
			return ((ICobolArrayFloatBinding) binding).getFloatList()
					.get(index);

		} else if (binding instanceof ICobolDbcsBinding) {
			return ((ICobolDbcsBinding) binding).getStringValue();

		} else if (binding instanceof ICobolArrayDbcsBinding) {
			return ((ICobolArrayDbcsBinding) binding).getStringList()
					.get(index);

		} else if (binding instanceof ICobolNationalBinding) {
			return ((ICobolNationalBinding) binding).getStringValue();

		} else if (binding instanceof ICobolArrayNationalBinding) {
			return ((ICobolArrayNationalBinding) binding).getStringList().get(
					index);

		} else if (binding instanceof ICobolOctetStreamBinding) {
			return ((ICobolOctetStreamBinding) binding).getByteArrayValue();

		} else if (binding instanceof ICobolArrayOctetStreamBinding) {
			return ((ICobolArrayOctetStreamBinding) binding).getByteArrayList()
					.get(index);

		} else {
			return null;
		}
	}
	
	/**
	 * Separate package name and class name from a qualified class name.
	 * @param qualifiedClassName qualified class name
	 * @return a structure with both parts
	 */
	public static ClassName toClassName(final String qualifiedClassName) {
		ClassName className = new ClassName();
		int pos = qualifiedClassName.lastIndexOf('.');
		className.packageName = (pos == -1) ? null
				: qualifiedClassName.substring(0, pos);
		className.className = (pos == -1) ? qualifiedClassName
				: qualifiedClassName.substring(pos + 1);
		return className;
	}
	
	/**
	 * A simple vehicle for class name and package name.
	 *
	 */
	public static class ClassName {
		public String className;
		public String packageName;
	}

    /*
     * ------------------------------------------------------------------------
     * Class path handling.
     * ------------------------------------------------------------------------
     */
    /**
     * Create a new thread context class loader with the jars coming from
     * our private lib sub folder. These are needed to locate the
     * JAXB classes with COBOL annotations generated by LegStar.
     * <p/>
     * This is expensive so avoid doing that several times for the same thread.
     * 
     * @param clazz the class that attempts to set the class loader
     * @throws KettleException if classloader fails
     */
    public static void setLibClassLoader(Class < ? > clazz) throws KettleException {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl instanceof KettleURLClassLoader) {
            if (((KettleURLClassLoader) tccl).getName().equals(
                    LIB_CLASSLOADER_NAME)) {
                return;
            }
        }
        PluginFolder pluginLibFolder = getPluginLibFolder();
        List<URL> urls = new ArrayList<URL>();
        try {
            if (new File(pluginLibFolder.getFolder()).exists()) {
                FileObject[] libFiles = pluginLibFolder.findJarFiles();
                for (FileObject libFile : libFiles) {
                    urls.add(libFile.getURL());
                }
                ClassLoader parent = clazz.getClassLoader();
                KettleURLClassLoader cl = new KettleURLClassLoader(urls
                        .toArray(new URL[urls.size()]),
                        parent, LIB_CLASSLOADER_NAME);
                Thread.currentThread().setContextClassLoader(cl);
            }
        } catch (Exception e) {
            throw new KettleException(e);
        }
    }
    
    /**
     * Convenience method to get our private lib folder.
     * <p/>
     * If we are registered as a plugin (during integration tests or production)
     * then we get the location of our plugin from the registry otherwise se
     * assume we are running off "user.dir".
     * 
     * @return a kettle plugin folder
     */
    public static PluginFolder getPluginLibFolder() {
        
        String pluginLocation = null;
        PluginInterface plugin = PluginRegistry.getInstance().findPluginWithId(
                StepPluginType.class,
                "com.legstar.pdi.zosfile");
        if (plugin != null) {
            pluginLocation = plugin.getPluginDirectory().getPath();
        } else {
            pluginLocation = System.getProperty("user.dir") + '/'
                    + DEFAULT_PLUGIN_FOLDER;
        }
        pluginLocation += '/' + LIB_FOLDER;
        return new PluginFolder(pluginLocation, false, false);
    }
    
    /**
     * Fetches all available COBOL-annotated JAXB class names from the lib
     * sub-folder.
     * 
     * @return null if no jars found in lib subfolder, otherwise all
     *         COBOL-annotated JAXB classes
     * @throws KettleFileException in case of read failure on the jar files
     */
    public static List < String > getAvailableJaxbClassNames()
            throws KettleFileException {
        try {
            List < String > jaxbclassNames = null;
            FileObject[] fileObjects = getPluginLibFolder().findJarFiles();
            if (fileObjects != null && fileObjects.length > 0) {
                jaxbclassNames = new ArrayList < String >();
                for (FileObject fileObject : fileObjects) {
                    AnnotationDB annotationDB = new AnnotationDB();
                    annotationDB.scanArchives(fileObject.getURL());
                    Set < String > classNames = annotationDB
                            .getAnnotationIndex()
                            .get(LEGSTAR_ANNOTATIONS);
                    if (classNames != null && classNames.size() > 0) {
                        jaxbclassNames.addAll(classNames);
                    }
                }
            }
            return jaxbclassNames;
        } catch (FileSystemException e) {
            throw new KettleFileException(e);
        } catch (IOException e) {
            throw new KettleFileException(e);
        }
    }

}

package com.legstar.pdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import com.legstar.coxb.cob2trans.Cob2TransException;
import com.legstar.coxb.cob2trans.Cob2TransGenerator;
import com.legstar.coxb.cob2trans.Cob2TransGenerator.Cob2TransResult;
import com.legstar.coxb.cob2trans.Cob2TransModel;
import com.legstar.coxb.host.HostContext;
import com.legstar.coxb.host.HostData;
import com.legstar.coxb.host.HostException;
import com.legstar.coxb.impl.reflect.CComplexReflectBinding;
import com.legstar.coxb.transform.HostTransformException;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.coxb.transform.IHostTransformers;
import com.legstar.coxb.util.BindingUtil;
import com.legstar.coxb.util.ClassUtil;
import com.legstar.coxb.util.ClassUtil.ClassName;

/**
 * This is a Mediator between the PDI API and the LegStar PDI.
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
    
    /** The relative location of our private configuration folder. */
    public static final String CONF_FOLDER = "conf";
    
    /** The configuration file name. */
    public static final String CONF_FILE_NAME = "cob2trans.properties";
    
    /** The JAXB/COBOL Legstar classes should be annotated with this. */
    public static final String LEGSTAR_ANNOTATIONS = "com.legstar.coxb.CobolElement";

	/**
	 * Utility class. No instantiation.
	 */
	private CobolToPdi() {

	}

    /*
     * ------------------------------------------------------------------------
     * COBOL to Transformers generation.
     * ------------------------------------------------------------------------
     */
    /**
     * From COBOL code, this creates a set of transformers, bundles
     * them in a jar that it stores in the plugin lib sub folder and
     * produces JAXB root class names, that can be used to map
     * z/OS file records, as well as a jar file taht bundles all these
     * artifacts.
     * <p/>
     * The jar file name is build from a hash of the COBOL code so that
     * we get a unique name for each COBOL source.
     * 
     * @param monitor an Eclipse monitor to report generation progress
     * @param cobolCode the COBOL code to generate Transformers from
     * @return the generation results
     * @throws Cob2TransException
     *             if failed to get the COBOL structure info from JAXB
     */
    public static Cob2TransResult generateTransformer(
            final IProgressMonitor monitor,
            final String cobolCode) throws Cob2TransException {
        try {

            Cob2TransGenerator cob2trans = new Cob2TransGenerator(
                    CobolToPdi.getCob2TransModel());
            Cob2TransListenerAdapter listener = new Cob2TransListenerAdapter(
                    cob2trans, monitor);
            cob2trans.addCob2TransListener(listener);

            // TODO add cobolEncoding?
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] cobolCodeDigest = md5.digest(cobolCode.getBytes("UTF-8"));

            Cob2TransResult result = cob2trans.generate(
                    toTempFile(cobolCode, "UTF-8"),
                    "UTF-8",
                    HostData.toHexString(cobolCodeDigest),
                    createTempDirectory(),
                    getClasspath());

            // Deploy the jar to the lib folder
            FileUtils.copyFileToDirectory(result.jarFile,
                    new File(getPluginLibLocation()));

            return result;
        } catch (IOException e) {
            throw new Cob2TransException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new Cob2TransException(e);
        }

    }

    /**
     * Dumps content to a temporary file.
     * 
     * @param content some COBOL data item descriptions
     * @param encoding the encoding to use when writing to file
     * @return a temporary file with the content
     * @throws IOException if temp file cannot be created
     */
    public static File toTempFile(final String content, final String encoding)
            throws IOException {
        File cobolFile = File.createTempFile("legstar", ".cbl");
        cobolFile.deleteOnExit();
        FileUtils.writeStringToFile(cobolFile, content, encoding);
        return cobolFile;
    }

    /**
     * Artifacts will be generated in a temporary folder. 
     * @return the temporary folder
     * @throws IOException if temp folder cannot be created
     */
    public static File createTempDirectory()  throws IOException {
        File dir = File.createTempFile("legstar", "");
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();
        return dir;
    }
    
    /**
     * Load the configuration file into a Model.
     * 
     * @throws Cob2TransException
     *             if configuration file missing or file corrupt
     */
    public static Cob2TransModel getCob2TransModel() throws Cob2TransException {
        return getCob2TransModel(new File(getPluginConfLocation() + '/'
                + CONF_FILE_NAME));
    }

    /**
     * Load the configuration file into a Model.
     * 
     * @param configFile the configuration file to load
     * @throws Cob2TransException
     *             if configuration file missing or file corrupt
     */
    public static Cob2TransModel getCob2TransModel(final File configFile)
            throws Cob2TransException {
        try {
            if (configFile == null) {
                return new Cob2TransModel();
            } else {
                Properties config = new Properties();
                config.load(new FileInputStream(configFile));
                return new Cob2TransModel(config);
            }
        } catch (FileNotFoundException e) {
            throw new Cob2TransException(e);
        } catch (IOException e) {
            throw new Cob2TransException(e);
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Adapt COBOL hierarchy to PDI flat field list.
     * ------------------------------------------------------------------------
     */
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
     *            we temporarily create
     * @return an array of fields as if originating from a text file
     * @throws KettleException
     *             if failed to get the COBOL structure info from JAXB
     */
    public static CobolFileInputField[] getCobolFields(
            final String compositeJaxbClassName,
            final Class < ? > clazz) throws KettleException {

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            CobolToPdi.setTransformerClassLoader(clazz,
                    getJarFileName(compositeJaxbClassName));
            List < CobolFileInputField > fields =
                    toFields(getJaxbClassName(compositeJaxbClassName));
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
	 * @param jaxbQualifiedClassName  the JAXB class name
	 * @return a list of fields as if originating from a text file
	 * @throws KettleException
	 *             if failed to get the COBOL structure info from JAXB
	 */
	public static List<CobolFileInputField> toFields(
			final String jaxbQualifiedClassName)
			throws KettleException {
		try {
			ClassName className = ClassUtil.toClassName(jaxbQualifiedClassName);
			Object jaxbObjectFactory = BindingUtil
					.newJaxbObjectFactory(className.packageName);
			Object jaxbObject = BindingUtil.newJaxbObject(
					jaxbObjectFactory, className.className);
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
	 * Field names may be prefixed with the parent name to avoid name conflicts.
	 * <p/>
	 * Arrays are split in the maximum number of items, each one being named
	 * after the array name and suffixed with its index within the array.
	 * <p/>
	 * Choices (redefines) result in alternatives being ignored.
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
					fields.add(toField(fields, binding, prefix, newSuffix, redefined));
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
	        final List<CobolFileInputField> fields,
			final ICobolBinding binding,
			final String prefix,
			final String suffix,
			final boolean redefined) {

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
	 * Creates a new column name based on the COXB field name.
	 * <p/>
	 * The proposed name is as simple as possible but we need to avoid any
	 * name conflicts (a column must have a unique name) so we check for
	 * any conflict and use a prefix system to disambiguate names.
	 * @param fields the list of fields already named
	 * @param binding the COXB field
	 * @param prefix the maximum prefix
	 * @param suffix an optional suffix (array items)
	 * @return a unique name for a column
	 */
	protected static String newName(final List<CobolFileInputField> fields,
            final String bindingName,
            final String prefix,
            final String suffix) {

	    // Always add suffix if any (array item number)
	    String name = bindingName + ((suffix == null) ? "" : suffix);
	   
	    // nothing to disambiguate with
	    if (prefix == null || prefix.isEmpty()) {
	        return name;
	    }
	    String[] prefixes = prefix.split("_");
	    int pos = prefixes.length - 1;
	    
        while (nameConlict(name, fields) && pos > -1) {
            name  = prefixes[pos] + '_' + name;
            pos--;
        }
        
        return name;
	    
	}
	
    /**
     * Determines if a proposed name is already used.
     * @param name the proposed name
     * @param fields the list of fields already named
     * @return true if name already used
     */
    protected static boolean nameConlict(final String name,
            final List < CobolFileInputField > fields) {
        for (CobolFileInputField field : fields) {
            if (field.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*
     * ------------------------------------------------------------------------
     * Runtime Transformer execution.
     * ------------------------------------------------------------------------
     */
    /**
     * Create an instance of Transformers for a given JAXB root class name.
     * Assumes binding classes were generated for this JAXB class.
     * 
     * @param jaxbQualifiedClassName the JAXB class name
     * @return a new instance of Transformers
     * @throws KettleException if transformers cannot be created
     */
    public static IHostTransformers newTransformers(
            final String jaxbQualifiedClassName) throws KettleException {
        try {
            return BindingUtil.newTransformers(
                    jaxbQualifiedClassName);
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
     * @param hostCharset
     *            the host character set
     * @param status
     *            additional info on the COBOL to Java transformation process
     * @return a PDI output row of data
     * @throws KettleException
     *             if transformation fails
     */
	public static Object[] toOutputRowData(
			final RowMetaInterface outputRowMeta,
			final IHostTransformers tf,
			final byte[] hostRecord,
			final String hostCharset,
			final HostTransformStatus status)
			throws KettleException {
		try {
		    int expectedOutputRows = outputRowMeta.getFieldNames().length;
		    tf.toJava(hostRecord, hostCharset, status);

			List<Object> objects = new ArrayList<Object>();
			toObjects(objects, status.getBinding(), -1);

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
     * The default mainframe character set.
     * @return the default mainframe character set
     */
    public static String getDefaultHostCharset() {
        return HostContext.getDefaultHostCharsetName();
    }
    
    /**
     * Allocates a byte array large enough to accommodate the largest host record.
     * @param transformers the host transformer set
     * @return a byte array large enough for the largest record
     * @throws KettleFileException if byte array cannot be allocated
     */
    public static byte[] newHostRecord(final IHostTransformers transformers)
            throws KettleFileException {
        try {
            return new byte[transformers.getJavaToHost().getByteLength()];
        } catch (CobolBindingException e) {
            throw new KettleFileException(e);
        }
    }

    /*
     * ------------------------------------------------------------------------
     * Class path handling.
     * ------------------------------------------------------------------------
     */
    /**
     * Create a thread context class loader with an additional jar containing
     * Transformer and JAXB classes.
     * <p/>
     * This is expensive so avoid doing that several times for the same thread.
     * 
     * @param clazz the class that attempts to set the class loader
     * @param jarFileName the jar file containing the JAXB classes
     * @throws KettleException if classloader fails
     */
    public static void setTransformerClassLoader(
            Class < ? > clazz,
            final String jarFileName) throws KettleException {

        String classLoaderName = LIB_CLASSLOADER_NAME + '.' + jarFileName;
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl instanceof KettleURLClassLoader) {
            if (((KettleURLClassLoader) tccl).getName().equals(
                    classLoaderName)) {
                // Already setup
                return;
            }
        }

        try {
            PluginFolder pluginLibFolder = getPluginLibFolder();
            File jarFile = new File(pluginLibFolder.getFolder() + '/'
                    + jarFileName);
            ClassLoader parent = clazz.getClassLoader();
            KettleURLClassLoader cl = new KettleURLClassLoader(
                    new URL[] { jarFile.toURI().toURL() },
                    parent, classLoaderName);
            Thread.currentThread().setContextClassLoader(cl);
        } catch (Exception e) {
            throw new KettleException(e);
        }
    }
    
    /**
     * Convenience method to get our private lib folder.
     * 
     * @return a kettle plugin folder
     */
    public static PluginFolder getPluginLibFolder() {
        return new PluginFolder(getPluginLibLocation(), false, false);
    }
    
    /**
     * Find out where we are installed within Kettle.
     * <p/>
     * If we are registered as a plugin (during integration tests or production)
     * then we get the location of our plugin from the registry otherwise we
     * assume we are running off "user.dir".
     * 
     * @return the location where the plugin installled
     */
    public static String getPluginLocation() {
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
        return pluginLocation;
    }
    
    /**
     * @return the location of our private lib folder under the plugin
     */
    public static String getPluginLibLocation() {
        return getPluginLocation() + '/' + LIB_FOLDER;
    }
    
    /**
     * @return the location of our private configuration folder under the plugin
     */
    public static String getPluginConfLocation() {
        return getPluginLocation() + '/' + CONF_FOLDER;
    }
    
    /**
     * Compiler needs access to LegStar libraries which are installed in the
     * plugin folder.
     * @return a classspath usable to start java
     */
    @SuppressWarnings("unchecked")
    public static String getClasspath() {
        Collection < File > jarFiles = FileUtils.listFiles(new File(
                getPluginLocation()), new String[] { "jar" }, false);
        StringBuilder sb = new StringBuilder();
        boolean next = false;
        for (File jarFile : jarFiles) {
            if (next) {
                sb.append(File.pathSeparator);
            } else {
                next = true;
            }
            sb.append(jarFile.getPath());
        }
        return sb.toString();
    }
    
    /*
     * ------------------------------------------------------------------------
     * COBOL-annotated JAXB classes discovery and jar association.
     * ------------------------------------------------------------------------
     */
    /**
     * Fetches all available COBOL-annotated JAXB class names from the lib
     * sub-folder.
     * 
     * @return null if no jars found in lib subfolder, otherwise all
     *         COBOL-annotated JAXB classes along with the jar file name which
     *         contains that class. Each item in the list is formatted
     *         like so: className[jarFileName]
     * @throws KettleFileException in case of read failure on the jar files
     */
    public static List < String > getAvailableCompositeJaxbClassNames()
            throws KettleFileException {
        try {
            List < String > compositeJaxbclassNames = null;
            FileObject[] fileObjects = getPluginLibFolder().findJarFiles();
            if (fileObjects != null && fileObjects.length > 0) {
                compositeJaxbclassNames = new ArrayList < String >();
                for (FileObject fileObject : fileObjects) {
                    AnnotationDB annotationDB = new AnnotationDB();
                    annotationDB.scanArchives(fileObject.getURL());
                    Set < String > classNames = annotationDB
                            .getAnnotationIndex()
                            .get(LEGSTAR_ANNOTATIONS);
                    if (classNames != null) {
                        for (String className : classNames) {
                            compositeJaxbclassNames.add(getCompositeJaxbClassName(
                                    className, fileObject.getName()
                                            .getBaseName()));
                        }
                    }
                }
            }
            return compositeJaxbclassNames;
        } catch (FileSystemException e) {
            throw new KettleFileException(e);
        } catch (IOException e) {
            throw new KettleFileException(e);
        }
    }
    
    /**
     * Compose a name concatenating class name and containing jar file name.
     * @param qualifiedClassName the class name
     * @param jarFileName the containing jar file name
     * @return a string such as qualifiedClassName[jarFileName]
     */
    public static String getCompositeJaxbClassName(
            final String qualifiedClassName,
            final String jarFileName) {
        return qualifiedClassName + "[" + jarFileName + "]";
    }
    
    /**
     * Strip the containing jar file name and return the bare class name.
     * @param compositeJAXBClassName a string concatenating jar file name and class name
     * @return the bare class name part of the composite class name
     */
    public static String getJaxbClassName(final String compositeJAXBClassName) {
        int i = compositeJAXBClassName.indexOf('[');
        if (i > 0) {
            return compositeJAXBClassName.substring(0, i);
        }
        return compositeJAXBClassName;
    }
    
    /**
     * Strip the class name and return the jar file name.
     * 
     * @param compositeJAXBClassName a string concatenating jar file name and
     *            class name
     * @return the bare jar file name of the composite class name
     */
    public static String getJarFileName(final String compositeJAXBClassName) {
        int i = compositeJAXBClassName.indexOf('[');
        if (i > 0) {
            return compositeJAXBClassName.substring(i + 1,
                    compositeJAXBClassName.length() - 1);
        }
        return null;
    }

}

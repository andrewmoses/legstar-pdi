package com.legstar.pdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.pentaho.di.core.Const;

import com.legstar.coxb.cob2trans.Cob2TransException;
import com.legstar.coxb.cob2trans.Cob2TransGenerator;
import com.legstar.coxb.cob2trans.Cob2TransGenerator.Cob2TransResult;
import com.legstar.coxb.cob2trans.Cob2TransModel;
import com.legstar.coxb.gen.CoxbGenException;
import com.legstar.coxb.host.HostData;
import com.legstar.coxb.util.NameUtil;

/**
 * Translates a COBOL Structure to LegStar COBOL Transformers.
 * <p/>
 * COBOL Transformers are compiled and bundled in a jar archive stored where PDI
 * can find them.
 */
public class Cob2PdiTrans {

    /**
     * Utility class. No instantiation.
     */
    private Cob2PdiTrans() {

    }

    /**
     * From COBOL code, this creates a set of transformers, bundles them in a
     * jar that it stores in the plugin lib sub folder and produces JAXB root
     * class names, that can be used to map z/OS file records, as well as a jar
     * file that bundles all these artifacts.
     * <p/>
     * The jar file name is build from a hash of the COBOL code so that we get a
     * unique name for each COBOL source.
     * 
     * @param monitor an Eclipse monitor to report generation progress (null
     *        means no monitoring)
     * @param stepName used to create a more meaningful package name for the
     *        artifacts generated
     * @param cobolSource the COBOL code to generate Transformers from
     * @param cobolCharset the COBOL code encoding
     * @param cobolFilePath the COBOL file path (null if none)
     * @return the generation results
     * @throws Cob2TransException if failed to get the COBOL structure info from
     *         JAXB
     */
    public static Cob2TransResult generateTransformer(
            final IProgressMonitor monitor, final String stepName,
            final String cobolSource, final String cobolCharset,
            final String cobolFilePath) throws Cob2TransException {
        return generateTransformer(monitor, stepName, cobolSource,
                cobolCharset, cobolFilePath, CobolToPdi.getLibClassPath());

    }

    /**
     * From COBOL code, this creates a set of transformers, bundles them in a
     * jar that it stores in the plugin lib sub folder and produces JAXB root
     * class names, that can be used to map z/OS file records, as well as a jar
     * file taht bundles all these artifacts.
     * <p/>
     * The jar file name is build from a hash of the COBOL code so that we get a
     * unique name for each COBOL source.
     * 
     * @param monitor an Eclipse monitor to report generation progress (null
     *        means no monitoring)
     * @param stepName used to create a more meaningful package name for the
     *        artifacts generated
     * @param cobolSource the COBOL code to generate Transformers from
     * @param cobolCharset the COBOL code encoding
     * @param cobolFilePath the COBOL file path (null if none)
     * @param classPath a classpath parameter to pass on to compiler
     * @return the generation results
     * @throws Cob2TransException if failed to get the COBOL structure info from
     *         JAXB
     */
    public static Cob2TransResult generateTransformer(
            final IProgressMonitor monitor, final String stepName,
            final String cobolSource, final String cobolCharset,
            final String cobolFilePath, final String classPath)
            throws Cob2TransException {
        try {

            String packageName = getPackageName(stepName, cobolSource,
                    cobolCharset, cobolFilePath);

            Cob2TransModel model = Cob2PdiTrans.getCob2TransModel();
            String jaxbPackageName = getJaxbPackageName(model.getCoxbGenModel()
                    .getJaxbPackageName(), packageName);
            model.getCoxbGenModel().setJaxbPackageName(jaxbPackageName);

            Cob2TransGenerator cob2trans = new Cob2TransGenerator(model);

            if (monitor != null) {
                Cob2TransListenerAdapter listener = new Cob2TransListenerAdapter(
                        cob2trans, monitor);
                cob2trans.addCob2TransListener(listener);
            }

            Cob2TransResult result = cob2trans.generate(
                    toTempFile(cobolSource, cobolCharset), cobolCharset,
                    packageName, createTempDirectory(), classPath);

            // Deploy the jar to the user folder
            FileUtils.copyFileToDirectory(result.jarFile,
                    new File(CobolToPdi.getPluginUserLocation()));

            return result;
        } catch (IOException e) {
            throw new Cob2TransException(e);
        } catch (CoxbGenException e) {
            throw new Cob2TransException(e);
        }

    }

    /**
     * The JAXB package name is built from a prefix and the group of artifacts
     * package name.
     * 
     * @param jaxbPackageNamePrefix the JAXB package name prefix (or null if
     *        none)
     * @param packageName the artifacts package name
     * @return a more meaningful package name
     */
    public static String getJaxbPackageName(final String jaxbPackageNamePrefix,
            final String packageName) {
        if (Const.isEmpty(packageName)) {
            return jaxbPackageNamePrefix;
        }
        if (jaxbPackageNamePrefix == null) {
            return packageName;
        } else {
            return jaxbPackageNamePrefix + '.' + packageName;
        }
    }

    /**
     * Artifacts generated are grouped in a package which name is determined
     * here. </p> The name is built from the step name and one of the COBOL file
     * path base name or a hash from the COBOL source itself if we don't have a
     * file path.
     * 
     * @param stepName the step name
     * @param cobolSource the COBOL source code
     * @param cobolCharset the COBOL code encoding
     * @param cobolFilePath the COBOL file path (null if none)
     * @return a package name usage as a java package name part or a jar file
     *         base name
     * @throws Cob2TransException if name cannot be determined
     */
    public static String getPackageName(final String stepName,
            final String cobolSource, final String cobolCharset,
            final String cobolFilePath) throws Cob2TransException {
        try {
            String baseName = null;
            if (cobolFilePath == null) {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] cobolCodeDigest = md5.digest(cobolSource
                        .getBytes(cobolCharset));
                baseName = toJavaIdentifier(HostData
                        .toHexString(cobolCodeDigest));
            } else {
                baseName = toJavaIdentifier(FilenameUtils
                        .getBaseName(cobolFilePath));
            }
            if (Const.isEmpty(stepName)) {
                return baseName;
            }
            return toJavaIdentifier(stepName) + '.' + baseName;

        } catch (NoSuchAlgorithmException e) {
            throw new Cob2TransException(e);
        } catch (UnsupportedEncodingException e) {
            throw new Cob2TransException(e);
        }
    }

    /**
     * Turn a character string into a valid java identifier usable as both a
     * package name part and a file base name.
     * 
     * @param str the character string
     * @return a valid java identifier usable as a package name part or a file
     *         base name
     */
    public static String toJavaIdentifier(final String str) {
        String variable = NameUtil.toVariableName(str.replace(" ", ""))
                .toLowerCase();
        // Make sure first char is valid
        if (!Character.isJavaIdentifierStart(variable.charAt(0))) {
            variable = '_' + variable;
        }
        return variable;
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
     * 
     * @return the temporary folder
     * @throws IOException if temp folder cannot be created
     */
    public static File createTempDirectory() throws IOException {
        File dir = File.createTempFile("legstar", "");
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();
        return dir;
    }

    /**
     * Load the configuration file into a Model.
     * 
     * @throws Cob2TransException if configuration file missing or file corrupt
     */
    public static Cob2TransModel getCob2TransModel() throws Cob2TransException {
        File configFile = (CobolToPdi.getPluginConfLocation() == null) ? null
                : new File(CobolToPdi.getPluginConfLocation() + '/'
                        + CobolToPdi.CONF_FILE_NAME);
        return getCob2TransModel(configFile);
    }

    /**
     * Load the configuration file into a Model.
     * 
     * @param configFile the configuration file to load
     * @throws Cob2TransException if configuration file missing or file corrupt
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

}

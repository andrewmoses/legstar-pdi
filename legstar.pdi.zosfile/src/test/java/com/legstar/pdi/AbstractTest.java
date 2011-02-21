package com.legstar.pdi;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public abstract class AbstractTest extends TestCase {

    /** COBOL copybooks test cases folder. */
    public static final File COPYBOOKS_DIR = new File(
            "src/test/resources/copybooks");

    /** COBOL copybooks encoding. */
    public static final String COPYBOOKS_ENC = "ISO-8859-1";

    /** Test cases folder. */
    public static final File CASES_DIR = new File("src/test/resources/cases");

    /** Reference folder. */
    public static final File REF_DIR = new File("src/test/resources/reference");

    /** Generated PDI schema folder. */
    public static final File GEN_PDISCHEMA_DIR = new File(
            "target/gen/pdischema");

    /** Generated ANT script folder. */
    public static final File GEN_ANT_DIR = new File("target/gen/ant");

    /** This means references should be created instead of compared to results. */
    private boolean _createReferences = false;

    /** Current context class loader. */
    private ClassLoader _tccl;

    /**
     * Put a set of test case jars onto the context class loader.
     * 
     * @throws Exception
     */
    public void setUp() throws Exception {

        _tccl = Thread.currentThread().getContextClassLoader();
        setTestContextClassLoader(_tccl);
    }

    public void tearDown() {
        Thread.currentThread().setContextClassLoader(_tccl);
    }

    /**
     * Set a class loader with the COBOL Transformer test cases in.
     * 
     * @param parentCl the parent class loader
     * @throws Exception if setting class loader fails
     */
    public static void setTestContextClassLoader(final ClassLoader parentCl)
            throws Exception {
        String[] jarFileNames = Cob2Pdi.getClasspath("src/test/resources/user")
                .split(";");
        URL[] jarFileUrls = new URL[jarFileNames.length];
        for (int i = 0; i < jarFileNames.length; i++) {
            jarFileUrls[i] = new File(jarFileNames[i]).toURI().toURL();
        }
        URLClassLoader cl = new URLClassLoader(jarFileUrls, parentCl);
        Thread.currentThread().setContextClassLoader(cl);
    }

    /**
     * Check a result against a reference.
     * 
     * @param fileName the input file
     * @param extension the reference file name extension to use
     * @param result the actual results
     * @throws Exception if something fails
     */
    protected void check(final String fileName, final String extension,
            final String result) throws Exception {
        File referenceFile = new File(REF_DIR, getUnqualName(getClass())
                + "/"
                + fileName
                + ((extension == null || extension.length() == 0) ? "" : "."
                        + extension));

        if (isCreateReferences()) {
            FileUtils.writeStringToFile(referenceFile, result, "UTF-8");
        } else {
            String expected = FileUtils
                    .readFileToString(referenceFile, "UTF-8");
            assertEquals(expected, result);
        }

    }

    /**
     * Check a result against a reference.
     * 
     * @param fileName the input file
     * @param extension the reference file name extension to use
     * @param resultFile a file containing actual results
     * @throws Exception if something fails
     */
    protected void check(final String fileName, final String extension,
            final File resultFile) throws Exception {
        check(fileName, extension, FileUtils.readFileToString(resultFile));

    }

    /**
     * @return true if references should be created instead of compared to
     *         results
     */
    public boolean isCreateReferences() {
        return _createReferences;
    }

    /**
     * @param createReferences true if references should be created instead of
     *        compared to results
     */
    public void setCreateReferences(boolean createReferences) {
        _createReferences = createReferences;
    }

    /**
     * Return a class unqualified (no package prefix) name.
     * 
     * @param clazz the class
     * @return the unqualified name
     */
    public static String getUnqualName(final Class<?> clazz) {
        String unqname = clazz.getName();
        if (unqname.lastIndexOf('.') > 0) {
            unqname = unqname.substring(unqname.lastIndexOf('.') + 1);
        }
        return unqname;
    }

    /**
     * Execute an ant script.
     * 
     * @param buildFile the ant script
     * @throws Exception if ant script execution fails
     */
    public void runAnt(final File buildFile) throws Exception {
        final Project project = new Project();
        project.setCoreLoader(this.getClass().getClassLoader());
        project.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        project.addReference("ant.projectHelper", helper);
        helper.parse(project, buildFile);
        Vector<String> targets = new Vector<String>();
        targets.addElement(project.getDefaultTarget());
        project.setBaseDir(new File("."));
        project.executeTargets(targets);
    }

}

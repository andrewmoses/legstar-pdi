package com.legstar.pdi.zosfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

import com.legstar.coxb.cob2trans.Cob2TransGenerator.Cob2TransResult;
import com.legstar.coxb.util.ClassUtil;
import com.legstar.pdi.Cob2PdiFields;
import com.legstar.pdi.Cob2PdiTrans;
import com.legstar.pdi.Cob2Pdi;

/**
 * Tests to run after the package was deployed. Here Kettle should have
 * registered our plugin on startup. Can't inherit from TransformationTestCase
 * here because its constructor initializes Kettle and then it is too late to
 * force the plugin base location folder.
 * 
 */
public class ZosFileITCase extends TestCase {

    /**
     * {@inheritDoc} We tell Kettle to look at our target folder for plugins to
     * load into the registry.
     * 
     * */
    public void setUp() throws KettleException {
        System.setProperty(Cob2Pdi.PLUGIN_FOLDER_PROPERTY, "target/"
                + Cob2Pdi.DEFAULT_PLUGIN_FOLDER);
        System.setProperty(Const.KETTLE_PLUGIN_CLASSES,
                "com.legstar.pdi.zosfile.ZosFileInputMeta");
        KettleEnvironment.init();
    }

    /**
     * Create a transformation, generate the transformers and run them. This
     * simulates the complete process as if performed interactively.
     * 
     * @throws Exception if test fails
     */
    public void testGenerateAndExecute() throws Exception {

        String zosFileInputStepName = "zosFileInputStep";

        // First generate and deploy artifacts from COBOL source
        File cobolSourceFile = new File("src/test/resources/copybooks/CUSDATCC");
        String cobolCharset = "ISO-8859-1";
        String cobolSource = FileUtils.readFileToString(cobolSourceFile,
                cobolCharset);

        Cob2TransResult cob2transResult = Cob2PdiTrans.generateTransformer(
                null, zosFileInputStepName, cobolSource, cobolCharset,
                cobolSourceFile.getPath(), getCompilerClassPath());

        String compositeJaxbClassName = Cob2Pdi.getCompositeJaxbClassName(
                ClassUtil.toQualifiedClassName(
                        cob2transResult.coxbgenResult.jaxbPackageName,
                        cob2transResult.coxbgenResult.rootClassNames.get(0)),
                cob2transResult.jarFile.getName());

        // Get our plugin from the PDI registry
        PluginRegistry registry = PluginRegistry.getInstance();

        // Create a z/OS file input step using the generated artifacts
        PluginInterface sp = registry.findPluginWithId(StepPluginType.class,
                "com.legstar.pdi.zosfile");
        ZosFileInputMeta zosFileInputMeta = (ZosFileInputMeta) registry
                .loadClass(sp);
        zosFileInputMeta.setCompositeJaxbClassName(compositeJaxbClassName);

        // Get the meta fields descriptions
        zosFileInputMeta.setInputFields(Cob2PdiFields.getCobolFields(
                compositeJaxbClassName, getClass()));

        // Specify the z/OS file location
        zosFileInputMeta.setFilename("src/main/file/ZOS.FCUSTDAT.RDW.bin");
        zosFileInputMeta.setIsVariableLength(true);
        zosFileInputMeta.setHasRecordDescriptorWord(true);

        // Setup the a PDI Transformation [injector -> zosFileInput -> dummy]
        // and run it
        TransMeta transMeta = TransTestFactory.generateTestTransformation(
                new Variables(), zosFileInputMeta, zosFileInputStepName);

        List<RowMetaAndData> result = TransTestFactory
                .executeTestTransformation(transMeta,
                        TransTestFactory.INJECTOR_STEPNAME,
                        zosFileInputStepName, TransTestFactory.DUMMY_STEPNAME,
                        createSourceData());

        // Chack that our 10000 lines made it to the dummy step
        assertEquals(10000, result.size());

    }

    /**
     * A variable file with no RDW.
     * 
     * @throws Exception if test fails
     */
    public void testVariableNoRDW() throws Exception {
        runTrans("src/test/resources/variable.ktr");
    }

    /**
     * A variable file with RDW.
     * 
     * @throws Exception if test fails
     */
    public void testVariableRDW() throws Exception {
        runTrans("src/test/resources/variable-rdw.ktr");
    }

    /**
     * Generic execution of a transformation from a kettle XML description.
     * 
     * @param fileName the kettle transformation description (ktr file)
     * @throws Exception if execution fails
     */
    protected void runTrans(final String fileName) throws Exception {
        TransMeta transMeta = new TransMeta(fileName);
        Trans trans = new Trans(transMeta);
        trans.setLogLevel(LogLevel.DEBUG);
        trans.initializeVariablesFrom(null);
        trans.getTransMeta().setInternalKettleVariables(trans);

        trans.setSafeModeEnabled(true);

        // see if the transformation checks ok
        List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
        trans.getTransMeta().checkSteps(remarks, false, null);
        for (CheckResultInterface remark : remarks) {
            if (remark.getType() == CheckResultInterface.TYPE_RESULT_ERROR) {
                fail("Check error: " + fileName + ", " + remark.getErrorCode());
            }
        }

        // execute transformation
        trans.execute(null);

        trans.waitUntilFinished();
        Result result = trans.getResult();
        assertEquals(0, result.getNrErrors());
    }

    /**
     * The PDI test framework has an injector step before our own.
     * 
     * @return row meta and data for the injector step
     */
    public List<RowMetaAndData> createSourceData() {
        return createData(createSourceRowMetaInterface(),
                new Object[][] { new Object[] { "abc" } });
    }

    /**
     * This is the meta data for the injector step.
     * 
     * @return meta data for the injector
     */
    public RowMetaInterface createSourceRowMetaInterface() {
        return createRowMetaInterface(new ValueMeta("field1",
                ValueMeta.TYPE_STRING));
    }

    /**
     * Creates a classpath with all legstar-pdi dependencies. This is needed
     * because transformers generation process needs to compile classes and
     * typically javac does not inherit class loaders.
     * 
     * @return a classpath with all compile time dependencies
     */
    @SuppressWarnings("unchecked")
    protected String getCompilerClassPath() {
        Collection<File> jarFiles = FileUtils.listFiles(new File(
                "target/plugins/compile/lib"), new String[] { "jar" }, false);
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
     * Following code is duplicate from
     * org.pentaho.di.trans.TransformationTestCase.
     * ------------------------------------------------------------------------
     */
    public List<RowMetaAndData> createData(RowMetaInterface rm, Object[][] rows) {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

        for (Object[] row : rows) {
            list.add(new RowMetaAndData(rm, row));
        }

        return list;
    }

    public RowMetaInterface createRowMetaInterface(ValueMeta... valueMetas) {
        RowMetaInterface rm = new RowMeta();

        for (ValueMeta vm : valueMetas) {
            rm.addValueMeta(vm);
        }

        return rm;
    }

}

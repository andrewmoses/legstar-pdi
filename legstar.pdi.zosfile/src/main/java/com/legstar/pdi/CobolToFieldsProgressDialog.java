package com.legstar.pdi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

import com.legstar.coxb.cob2trans.Cob2TransException;
import com.legstar.coxb.cob2trans.Cob2TransGenerator;
import com.legstar.coxb.cob2trans.Cob2TransInterruptedException;
import com.legstar.coxb.cob2trans.Cob2TransGenerator.Cob2TransResult;
import com.legstar.coxb.util.ClassUtil;
import com.legstar.pdi.zosfile.ZosFileInputDialog;

/**
 * Displays a progress dialog while the COBOL code is parsed and Transformers
 * are generated.
 * </P>
 * If successful, the dialog returns a set of JAXB root class names as Strings
 * which concatenate the class name and the containing jar file.
 * </p>
 * The cancel button is operational and interrupts the generation (in which case
 * the list of returned root class names is null).
 * 
 */
public class CobolToFieldsProgressDialog {

    /** For i18n purposes. */
    private static Class<?> PKG = ZosFileInputDialog.class;

    /** The SWT shell. */
    private Shell _shell;
    
    /** The COBOL code to translate. */
    private String _cobolCode;
    
    /** The generated JAXB root classes suffixed with their containing jar file.*/
    private List < String > _compositeJaxbClassNames;

    /**
     * Creates a new dialog that will handle the wait while loading a
     * transformation...
     */
    public CobolToFieldsProgressDialog(final Shell shell, final String cobolCode) {
        _shell = shell;
        _cobolCode = cobolCode;
    }
    
    /**
     * Generator is executed asynchronously while progress is reported.
     * 
     * @return true if the generation process went through. False if interrupted.
     */
    public boolean open() {

        IRunnableWithProgress op = new IRunnableWithProgress() {

            public void run(final IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask(
                            getI18N("ZosFileInputDialog.Cob2TransBegin.DialogMessage"),
                            Cob2TransGenerator.TOTAL_STEPS);

                    Cob2TransResult result = CobolToPdi.generateTransformer(
                            monitor, _cobolCode);

                    _compositeJaxbClassNames = new ArrayList < String >();
                    for (String rootClassName : result.coxbgenResult.rootClassNames) {
                        _compositeJaxbClassNames
                                .add(CobolToPdi.getCompositeJaxbClassName(
                                        ClassUtil
                                                .toQualifiedClassName(
                                                        result.coxbgenResult.jaxbPackageName,
                                                        rootClassName),
                                        result.jarFile.getName()));
                    }

                } catch (Cob2TransException e) {
                    throw new InvocationTargetException(
                            e,
                            getI18N("ZosFileInputDialog.Cob2TransFailure.DialogMessage"));
                } finally {
                    monitor.done();
                }
            }
        };
        
        try {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(_shell);
            pmd.run(true, true, op);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Cob2TransInterruptedException) {
                return false;
            }
            errorDialog(_shell, "ZosFileInputDialog.Cob2TransFailure.DialogMessage", e);
            return false;
        } catch (InterruptedException e) {
            errorDialog(_shell, "ZosFileInputDialog.Cob2TransInterrupted.DialogMessage", e);
            return false;
        }

        return true;
    }

    /**
     * Popup an error dialog.
     * @param shell the Eclipse shell
     * @param messageKey the message I18N key.
     * @param e the associated exception
     */
    protected void errorDialog(final Shell shell, final String messageKey, final Throwable e) {
        new ErrorDialog(
                shell,
                getI18N("ZosFileInputDialog.FailedToGetFields.DialogTitle"),
                getI18N(messageKey),
                e);

    }

    /**
     * @return the generated JAXB class classes suffixed with their containing
     *         jar file name
     */
    public List < String > getCompositeJaxbClassNames() {
        return _compositeJaxbClassNames;
    }

    /**
     * Shortcut to I18N messages.
     * @param key the message identifier
     * @param parameters parameters if any
     * @return the localized message
     */
    public static String getI18N(final String key, final String...parameters) {
        return BaseMessages.getString(PKG, key, parameters);
    }
}

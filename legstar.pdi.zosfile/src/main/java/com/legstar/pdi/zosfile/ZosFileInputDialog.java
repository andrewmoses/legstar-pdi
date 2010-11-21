package com.legstar.pdi.zosfile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import com.legstar.pdi.CobolFileInputField;
import com.legstar.pdi.CobolToFieldsProgressDialog;
import com.legstar.pdi.CobolToPdi;

/**
 * A PDI dialog to setup the z/OS File Input step.
 * TODO Add possibility of getting the file name from a previous step
 * TODO Add possibility to pass the file name to the next step (when it is received as a parameter)
 * TODO Add possibility to rename fields
 */
public class ZosFileInputDialog extends BaseStepDialog implements StepDialogInterface {

	/** For i18n purposes. */
	private static Class<?> PKG = ZosFileInputMeta.class;
	
	/** The step meta data. */
	private ZosFileInputMeta _inputMeta;

    /** The tab folder. */
	private CTabFolder   wTabFolder;

    /** File/Cobol/Fields tabs. */
    private CTabItem wFileTab, wCobolTab, wFieldsTab;

    /** File/Cobol/Fields composites. */
    private Composite wFileComp, wCobolComp, wFieldsComp;

    /** The z/OS file name. */
    private TextVar      wFilename;

    /** Browse button to locate the z/OS file. */
    private Button       wbbFilename;
    
    /** The host character set. */
    private Combo        wHostCharset;

    /** A checkbox for variable length records. */
    private Button       wcbIsVariableLength;

    /** A checkbox in case records start with an RDW. */
    private Button       wcbHasRecordDescriptorWord;
    
    /** The COBOL source input button. */
    private Button       wrbCobolSourceInput;

    /** The COBOL-annotated JAXB input button. */
    private Button       wrbCobolJAXBInput;
    
    /** New Transformer widgets group.*/
    private Composite    wNewTransformerComposite;
    
    /** Exsiting Transformer widgets group.*/
    private Composite    wExistingTransformerComposite;
    
    /** Browse button to locate the COBOL file. */
    private Button       wbbCobolFile;
    
    /** The COBOL code describing the z/OS file records. */
    private Text         wCobolSource;
    
    /** The COBOL-annotated JAXB classes combo box. */
    private Combo        wCompositeJaxbClassNames;

    /** The z/OS file record fields that need to be extracted.*/
    private TableView    wFields;
    
    /** A simple ruler to help with COBOL instructions entry. */
    private static final String COBOL_RULE_LABEL =
        "0--------1---------2---------3---------4---------5---------6"
        + "---------7--    ";

	
    /**
     * Create an instance of the dialog.
     * @param parent the parent SWT shell
     * @param stepMeta the step meta data
     * @param transMeta the transformation (we are part of) meta
     * @param stepname the step name
     */
    public ZosFileInputDialog(Shell parent, Object stepMeta, TransMeta transMeta,
            String stepname) {
        super(parent, (BaseStepMeta) stepMeta, transMeta, stepname);
        _inputMeta = (ZosFileInputMeta) stepMeta;
    }

	/**
	 * Assemble the dialog widgets and display.
	 */
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, _inputMeta);

		changed = _inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(getI18N("ZosFileInputDialog.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Add the step name
		addStepName(middle, margin);
		Control lastControl = wStepname;
		
        // All other properties are in tab folders
		addTabFolder(lastControl, middle, margin);
        lastControl = wTabFolder;
        
		// Add OK, cancel, and preview buttons
		addButtons(lastControl, margin);      

		// Add listeners
		addListeners();
		
		// Set the shell size
		setSize();

		setDialogFromMetaData();
		_inputMeta.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Add a step name widget.
	 * @param middle percentage of the parent widget 
	 * @param margin offset from that position
	 */
	protected void addStepName(final int middle, final int margin) {
		// Step name line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(getI18N("System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
	}
	
    /**
     * Add the tab folder and tab items.
     * @param lastControl control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     */
    protected void addTabFolder(final Control lastControl, final int middle,
            final int margin) {

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        // Add the File tab
        addFileTab(middle, margin);

        // Add the Cobol tab
        addCobolTab(middle, margin);

        // Add the Fields tab
        addFieldsTab(middle, margin);

        wTabFolder.setSelection(0);

        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(lastControl, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);
    }
	
    /**
     * The File tab contains properties related with the z/OS file.
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     */
    protected void addFileTab(final int middle, final int margin) {
        wFileTab = new CTabItem(wTabFolder, SWT.NONE);
        wFileTab.setText(getI18N("ZosFileInputDialog.File.Tab"));

        wFileComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFileComp);

        FormLayout fileLayout = new FormLayout();
        fileLayout.marginWidth = 3;
        fileLayout.marginHeight = 3;
        wFileComp.setLayout(fileLayout);
        
        // Add the File Location group
        Control last = addFileLocationGroup(wFileComp, middle, margin);
        
        // Add the Record Format Group
        last = addRecordFormatGroup(wFileComp, last, middle, margin);

        FormData fdFileComp=new FormData();
        fdFileComp.left  = new FormAttachment(0, 0);
        fdFileComp.top   = new FormAttachment(0, 0);
        fdFileComp.right = new FormAttachment(100, 0);
        fdFileComp.bottom= new FormAttachment(100, 0);
        wFileComp.setLayoutData(fdFileComp);

        wFileComp.layout();
        wFileTab.setControl(wFileComp);
    }
    
    /**
     * Add File location group.
     * 
     * @param parent the parent composite
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addFileLocationGroup(
            final Composite parent,
            final int middle,
            final int margin) {

        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.FileLocationGroup.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 3;
        groupLayout.marginHeight = 3;
        group.setLayout(groupLayout);
        props.setLook(group);

        // Add the z/OS file name
        addFilename(group, middle, margin);
        
        //TODO add file name from previous step

        FormData fdGroup= new FormData();
        fdGroup.left = new FormAttachment(0, 0);
        fdGroup.right = new FormAttachment(100, 0);
        fdGroup.top = new FormAttachment(0, margin);
        group.setLayoutData(fdGroup);

        return group;

    }
    
    /**
     * Add the z/OS file name widget and associated browse button.
     * 
     * @param parent the parent composite
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     */
    protected Control addFilename(
            final Composite parent,
            final int middle,
            final int margin) {

        // The filename browse button
        wbbFilename = new Button(parent, SWT.PUSH | SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename
                .setText(getI18N("System.Button.Browse"));
        wbbFilename
                .setToolTipText(getI18N("System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdb = new FormData();
        fdb.top = new FormAttachment(0, margin);
        fdb.right = new FormAttachment(100, 0);
        wbbFilename.setLayoutData(fdb);

        // The field itself...
        Label wlFilename = new Label(parent, SWT.RIGHT);
        wlFilename.setText(getI18N(
                "ZosFileInputDialog.Filename.Label"));
        props.setLook(wlFilename);
        FormData fdl = new FormData();
        fdl.top = new FormAttachment(0, margin);
        fdl.left = new FormAttachment(0, 0);
        fdl.right = new FormAttachment(middle, -margin);
        wlFilename.setLayoutData(fdl);
        wFilename = new TextVar(transMeta, parent, SWT.SINGLE | SWT.LEFT
                | SWT.BORDER);
        props.setLook(wFilename);
        FormData fd = new FormData();
        fd.top = new FormAttachment(0, margin);
        fd.left = new FormAttachment(middle, 0);
        fd.right = new FormAttachment(wbbFilename, -margin);
        wFilename.setLayoutData(fd);
        
        return wFilename;
    }
    
    /**
     * Add Record Format group.
     * 
     * @param parent the parent composite
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addRecordFormatGroup(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.RecordFormatGroup.Label"));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginWidth = 3;
        groupLayout.marginHeight = 3;
        group.setLayout(groupLayout);
        props.setLook(group);

        Control last = lastControl;
        // Add variable length checkbox
        last = addIsVariableLength(group, last, middle, margin);

        // Add RDW checkbox
        last = addHasRecordDescriptorWord(group, last, middle, margin);

        // Add the host character set combo box
        last = addHostCharset(group, last, middle, margin);

        FormData fdGroup= new FormData();
        fdGroup.left = new FormAttachment(0, 0);
        fdGroup.right = new FormAttachment(100, 0);
        fdGroup.top = new FormAttachment(lastControl, margin);
        group.setLayoutData(fdGroup);

        return group;

    }
    
    /**
     * Add the z/OS character set.
     * @param parent the parent composite
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addHostCharset(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {
        Label wlHostCharset = new Label(parent, SWT.RIGHT);
        wlHostCharset.setText(getI18N(
                "ZosFileInputDialog.HostCharset.Label"));
        props.setLook(wlHostCharset);
        FormData fdl = new FormData();
        fdl.left = new FormAttachment(0, 0);
        fdl.top = new FormAttachment(lastControl, margin);
        fdl.right = new FormAttachment(middle, -margin);
        wlHostCharset.setLayoutData(fdl);

        wHostCharset = new Combo(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHostCharset.setToolTipText(getI18N(
                "ZosFileInputDialog.HostCharset.Tooltip"));
        wHostCharset.setItems(availableCharsets());
        props.setLook(wHostCharset);
        
        FormData fd = new FormData();
        fd.left = new FormAttachment(middle, 0);
        fd.top = new FormAttachment(lastControl, margin);
        fd.right = new FormAttachment(100, 0);
        wHostCharset.setLayoutData(fd);
        
        return wHostCharset;

    }
    
    /**
     * Retrieve all available character sets on this VM.
     * @return an array of available character sets
     */
    protected String[] availableCharsets() {
        SortedMap < String, Charset > charsets = Charset.availableCharsets();
        Set < String > names = charsets.keySet();
        return names.toArray(new String[names.size()]);
    }
    
    /**
     * Add a check box for variable length records.
     * 
     * @param parent the parent composite
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addIsVariableLength(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {
        Label wlIsVariableLength = new Label(parent, SWT.RIGHT);
        wlIsVariableLength.setText(getI18N(
                "ZosFileInputDialog.IsVariableLength.Label"));
        props.setLook(wlIsVariableLength);
        FormData fdl = new FormData();
        fdl.top = new FormAttachment(lastControl, margin);
        fdl.left = new FormAttachment(0, 0);
        fdl.right = new FormAttachment(middle, -margin);
        wlIsVariableLength.setLayoutData(fdl);
        wcbIsVariableLength = new Button(parent, SWT.CHECK);
        wcbIsVariableLength.setToolTipText(getI18N(
                "ZosFileInputDialog.Tooltip.IsVariableLength"));
        props.setLook(wcbIsVariableLength);
        FormData fdb = new FormData();
        fdb.top = new FormAttachment(lastControl, margin);
        fdb.left = new FormAttachment(middle, 0);
        fdb.right = new FormAttachment(100, 0);
        wcbIsVariableLength.setLayoutData(fdb);
        
        return wcbIsVariableLength;
    }
    
    /**
     * Add a check box for records starting with an RDW.
     * 
     * @param parent the parent composite
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addHasRecordDescriptorWord(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {
        Label wlHasRecordDescriptorWord = new Label(parent, SWT.RIGHT);
        wlHasRecordDescriptorWord.setText(getI18N(
                "ZosFileInputDialog.HasRecordDescriptorWord.Label"));
        props.setLook(wlHasRecordDescriptorWord);
        FormData fdl = new FormData();
        fdl.top = new FormAttachment(lastControl, margin);
        fdl.left = new FormAttachment(0, 0);
        fdl.right = new FormAttachment(middle, -margin);
        wlHasRecordDescriptorWord.setLayoutData(fdl);
        wcbHasRecordDescriptorWord = new Button(parent, SWT.CHECK);
        wcbHasRecordDescriptorWord.setToolTipText(getI18N(
                "ZosFileInputDialog.Tooltip.HasRecordDescriptorWord"));
        props.setLook(wcbHasRecordDescriptorWord);
        FormData fdb = new FormData();
        fdb.top = new FormAttachment(lastControl, margin);
        fdb.left = new FormAttachment(middle, 0);
        fdb.right = new FormAttachment(100, 0);
        wcbHasRecordDescriptorWord.setLayoutData(fdb);
        
        return wcbHasRecordDescriptorWord;
    }
    
    /**
     * The COBOL tab contains properties related with the COBOL structure
     * describing the file records.
     * 
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     */
    protected void addCobolTab(final int middle, final int margin) {
        wCobolTab = new CTabItem(wTabFolder, SWT.NONE);
        wCobolTab.setText(getI18N(
                "ZosFileInputDialog.Cobol.Tab"));

        wCobolComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wCobolComp);

        FormLayout cobolLayout = new FormLayout();
        cobolLayout.marginWidth = 3;
        cobolLayout.marginHeight = 3;
        wCobolComp.setLayout(cobolLayout);

        // Allow user to select new or existing Transformer
        Control lastControl = addSelectInputType(wCobolComp, null, middle,
                margin);

        // Add new Transformer group
        wNewTransformerComposite = addNewTransformerComposite(wCobolComp,
                lastControl, middle, margin);

        // Add existing Transformer group
        wExistingTransformerComposite = addExistingTransformerComposite(
                wCobolComp, lastControl, middle, margin);

        FormData fdCobolComp = new FormData();
        fdCobolComp.left = new FormAttachment(0, 0);
        fdCobolComp.top = new FormAttachment(0, 0);
        fdCobolComp.right = new FormAttachment(100, 0);
        fdCobolComp.bottom = new FormAttachment(100, 0);
        wCobolComp.setLayoutData(fdCobolComp);

        wCobolComp.layout();
        wCobolTab.setControl(wCobolComp);
    }

    /**
     * Add 2 radio buttons to select between creating a new transformer from COBOL code
     * and reusing an existing one.
     * @param parent the parent composite
     * @param lastControl the last control to attach to
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the composite containing both radio buttons
     */
    protected Control addSelectInputType(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        Composite inputTypes = new Composite(parent, SWT.NULL);
        inputTypes.setLayout(new RowLayout());

        // The COBOL source input button
        wrbCobolSourceInput = new Button(inputTypes, SWT.RADIO);
        props.setLook(wrbCobolSourceInput);
        wrbCobolSourceInput
                .setText(getI18N("ZosFileInputDialog.CobolSourceInput.Label"));
        wrbCobolSourceInput.setToolTipText(getI18N(
                "ZosFileInputDialog.Tooltip.CobolSourceInput"));
        wrbCobolSourceInput.setSelection(true);

        // The COBOL-annotated JAXB input button
        wrbCobolJAXBInput = new Button(inputTypes, SWT.RADIO);
        props.setLook(wrbCobolJAXBInput);
        wrbCobolJAXBInput
                .setText(getI18N("ZosFileInputDialog.CobolJAXBInput.Label"));
        wrbCobolJAXBInput.setToolTipText(getI18N(
                "ZosFileInputDialog.Tooltip.CobolJAXBInput"));
        wrbCobolJAXBInput.setSelection(false);

        FormData fd = new FormData();
        if (lastControl == null) {
            fd.top = new FormAttachment(0, margin);
        } else {
            fd.top = new FormAttachment(lastControl, margin);
        }
        fd.left = new FormAttachment(0, margin);
        inputTypes.setLayoutData(fd);

        return inputTypes;
    }

    /**
     * Creates widgets to create a new Transformer from COBOL source.
     * @param parent the parent composite
     * @param lastControl the last control to attach to
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the group containing the widgets
     */
    protected Composite addNewTransformerComposite(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        Composite composite = new Composite(parent, SWT.NULL);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 3;
        fl.marginHeight = 3;
        composite.setLayout(fl);
        props.setLook(composite);

        // Add the button to select COBOL file
        Control last = addSelectCobolFileButton(composite, middle, margin);
        
        // Add the COBOL code edit box
        last = addCobolEditBox(composite, last, middle, margin);


        FormData fd = new FormData();
        if (lastControl == null) {
            fd.top = new FormAttachment(0, margin);
        } else {
            fd.top = new FormAttachment(lastControl, margin);
        }
        fd.left = new FormAttachment(0, margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom= new FormAttachment(100, 0);
        composite.setLayoutData(fd);

        return composite;
    }

    /**
     * This button will popup the resource selection dialog in order
     * to pick up COBOL code from the file system.
     * 
     * @param parent the parent container
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addSelectCobolFileButton(
            final Composite parent,
            final int middle,
            final int margin) {

        Display display = parent.getDisplay();

        // The COBOL file browse button
        wbbCobolFile = new Button(parent, SWT.PUSH | SWT.CENTER);
        props.setLook(wbbCobolFile);
        wbbCobolFile
                .setText(getI18N("ZosFileInputDialog.SelectCobolFile.Label"));
        wbbCobolFile.setToolTipText(getI18N(
                "ZosFileInputDialog.Tooltip.SelectCobolFile"));
        ImageData data = new ImageData(getClass().getResourceAsStream(
                "/import_cbl.gif"));
        Image image = new Image(display, data);
        wbbCobolFile.setImage(image);

        FormData fdb = new FormData();
        fdb.top = new FormAttachment(0, margin);
        fdb.left = new FormAttachment(0, margin);
        wbbCobolFile.setLayoutData(fdb);

        return wbbCobolFile;

    }
    
    /**
     * Add an edit box where users can copy/paste COBOL code.
     * 
     * @param parent the parent composite
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the new control
     */
    protected Control addCobolEditBox(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        // Fixed sized font so that COBOL displays nicely.

        FontData defaultFont = new FontData("Courier New", 8, SWT.NORMAL);
        Font cobolFont = new Font(parent.getDisplay(), defaultFont);

        Label wlCobolRule = new Label(parent, SWT.LEFT);
        wlCobolRule.setText(COBOL_RULE_LABEL);
        wlCobolRule.setFont(cobolFont);
        FormData fdl = new FormData();
        fdl.top = new FormAttachment(lastControl, margin);
        fdl.left = new FormAttachment(0, 0);
        fdl.right = new FormAttachment(100, -margin);
        wlCobolRule.setLayoutData(fdl);

        wCobolSource = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL);
        props.setLook(wCobolSource);
        wCobolSource.setFont(cobolFont);

        FormData fdt = new FormData();
        fdt.left = new FormAttachment(0, 0);
        fdt.top = new FormAttachment(wlCobolRule, margin);
        fdt.right = new FormAttachment(100, 0);
        fdt.bottom = new FormAttachment(100, 0);
        wCobolSource.setLayoutData(fdt);
        
        return wCobolSource;
    }

    /**
     * Creates widgets to select an existing Transformer.
     * @param parent the parent composite
     * @param lastControl the last control to attach to
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the group containing the widgets
     */
    protected Composite addExistingTransformerComposite(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        Composite composite = new Composite(parent, SWT.NULL);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 3;
        fl.marginHeight = 3;
        composite.setLayout(fl);
        props.setLook(composite);

        // Add the COBOL-annotated JAXB classes combo box
        addCompositeJaxbClassNamesCombo(composite, null, middle, margin);
        
        FormData fd = new FormData();
        if (lastControl == null) {
            fd.top = new FormAttachment(0, margin);
        } else {
            fd.top = new FormAttachment(lastControl, margin);
        }
        fd.left = new FormAttachment(0, margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom= new FormAttachment(100, 0);
        composite.setLayoutData(fd);

        return composite;
    }
    
    /**
     * A non modifiable combo box to select an existing COBOL-annotated JAXB
     * class.
     * 
     * @param parent the parent composite
     * @param lastControl the last control to attach to
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     * @return the new combo box
     */
    protected Control addCompositeJaxbClassNamesCombo(
            final Composite parent,
            final Control lastControl,
            final int middle,
            final int margin) {

        wCompositeJaxbClassNames = new Combo(parent, SWT.READ_ONLY | SWT.SIMPLE);
        wCompositeJaxbClassNames.setToolTipText(getI18N(
                "ZosFileInputDialog.CobolAnnotatedJaxbClass.Tooltip"));
        props.setLook(wCompositeJaxbClassNames);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.top = new FormAttachment(lastControl, margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom = new FormAttachment(100, 0);
        wCompositeJaxbClassNames.setLayoutData(fd);

        return wCompositeJaxbClassNames;

    }

    /**
     * The fields tab contains the fields list as derived from the COBOL code.
     * 
     * @param middle percentage of the parent widget
     * @param margin offset from that position
     */
    protected void addFieldsTab(final int middle, final int margin) {
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(getI18N(
                "ZosFileInputDialog.Fields.Tab"));

        wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFieldsComp);

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;
        wFieldsComp.setLayout(fieldsLayout);

        // Add the fields table view
        addFields(wFieldsComp, middle, margin);

        FormData fdFieldsComp = new FormData();
        fdFieldsComp.left = new FormAttachment(0, 0);
        fdFieldsComp.top = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom = new FormAttachment(100, 0);
        wFieldsComp.setLayoutData(fdFieldsComp);

        wFieldsComp.layout();
        wFieldsTab.setControl(wFieldsComp);
    }

    /**
     * Add a grid for z/OS record fields.
     * @param parent the parent composite
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     */
    protected void addFields(
            final Composite parent,
            final int middle,
            final int margin) {

        boolean readOnly = true;
        ColumnInfo[] colinf = new ColumnInfo[] {
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.NameColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.TypeColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.LengthColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.PrecisionColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.TrimTypeColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
                new ColumnInfo(getI18N(
                        "ZosFileInputDialog.RedefinedColumn.Column"),
                        ColumnInfo.COLUMN_TYPE_BUTTON, false, readOnly)
                };

        wFields = new TableView(transMeta, parent, SWT.FULL_SELECTION
                | SWT.MULTI, colinf, 1, readOnly, null, props);

        FormData fdFields = new FormData();
        fdFields.left  = new FormAttachment(0, 0);
        fdFields.top   = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom= new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);
    }
    
	/**
	 * Add the OK, cancel, get and preview buttons.
     * @param lastControl control to position from
	 * @param margin offset from parent widget
	 */
	protected void addButtons(final Control lastControl, final int margin) {
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(getI18N("System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(getI18N("System.Button.Cancel")); 
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(getI18N("System.Button.Preview"));
		wPreview.setEnabled(false);
        wGet=new Button(shell, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
        wGet.setEnabled(false);

		setButtonPositions(new Button[] { wOK, wGet, wPreview, wCancel }, margin, lastControl);
	}
	
	/**
	 * Add various listeners to widgets and shell.
	 */
	protected void addListeners() {

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                _inputMeta.setChanged();
                enableOrDisable();
            }
        };
        wStepname.addModifyListener(lsMod);
        wFilename.addModifyListener(lsMod);
        wHostCharset.addModifyListener(lsMod);
        wCobolSource.addModifyListener(lsMod);
        wCompositeJaxbClassNames.addModifyListener(lsMod);

        lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsPreview = new Listener() {
			public void handleEvent(Event e) {
				preview();
			}
		};

		lsGet = new Listener() {
            public void handleEvent(Event e) {
                getCobolFields();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wPreview.addListener(SWT.Selection, lsPreview);
        wGet.addListener(SWT.Selection, lsGet);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		addHostCharsetListeners();
		addFilenameListeners();
		
		addCobolInputTypeListener();
		addCobolFileListeners();
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		
	}
	
	/**
	 * Add listeners to the file name related widgets.
	 * Listen to the browse button next to the file name.
	 */
	protected void addFilenameListeners() {

		wFilename.addSelectionListener(lsDef);

        wbbFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.dat;*.bin", "*" });
                if (wFilename.getText() != null) {
                    String fname = transMeta.environmentSubstitute(wFilename
                            .getText());
                    dialog.setFileName(fname);
                }

                dialog.setFilterNames(new String[] {
                            getI18N("ZosFileInputDialog.FileType.DataFiles"),
                                      getI18N("System.FileType.AllFiles") });

                if (dialog.open() != null) {
                    String filePath = dialog.getFilterPath()
                            + System.getProperty("file.separator")
                            + dialog.getFileName();
                    wFilename.setText(filePath);
                }
            }
        });

	}
	
	/**
	 * Detect if user is switching from one input type to another.
	 */
	protected void addCobolInputTypeListener() {
	    wrbCobolSourceInput.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                doSelectInputType();
            }
	        
	    });
	    
	}
	
    /**
     * Show or hide appropriate group depending on user selection.
     */
    protected void doSelectInputType() {
        
        if (wrbCobolSourceInput.getSelection()) {
            wNewTransformerComposite.setVisible(true);
            wExistingTransformerComposite.setVisible(false);
        } else {
            wNewTransformerComposite.setVisible(false);
            wExistingTransformerComposite.setVisible(true);
        }
    }
    
	
    /**
     * Retrieve all available COBOL-annotated JAXB classes and set the Combo box
     * with these classes. Each class is associated with a containing jar.
     * 
     * @param compositeJaxbClassName the currently selected composite class name
     *            or null if no selection
     */
    protected void initCompositeJaxbClassNamesCombo(
            final String compositeJaxbClassName) {
        try {
            List < String > compositeJaxbClassNames =
                    CobolToPdi.getAvailableCompositeJaxbClassNames();
            if (compositeJaxbClassNames == null) {
                cobolAnnotatedJaxbClassErrorDialog(getI18N(
                        "ZosFileInputDialog.NoJarsInLib.DialogMessage",
                                CobolToPdi.getPluginLibFolder().getFolder()));
            } else {
                if (compositeJaxbClassNames.size() == 0) {
                    cobolAnnotatedJaxbClassErrorDialog(getI18N(
                            "ZosFileInputDialog.NoJAXBCOBOLClassesInLib.DialogMessage",
                                    CobolToPdi
                                            .getPluginLibFolder()
                                            .getFolder()));
                } else {
                    Collections.sort(compositeJaxbClassNames);
                    wCompositeJaxbClassNames
                            .setItems(compositeJaxbClassNames
                                    .toArray(new String[compositeJaxbClassNames
                                            .size()]));
                    if (compositeJaxbClassName != null) {
                        wCompositeJaxbClassNames.select(
                                wCompositeJaxbClassNames
                                        .indexOf(compositeJaxbClassName));
                    }
                }
            }
        } catch (KettleFileException e1) {
            compositeJaxbClassNameErrorDialog(e1);
        }
    }

    /**
     * A generic error dialog when an exception is raise while looking up
     * JAXB/COBOL classes.
     * 
     * @param e the exception that occurred
     */
    protected void compositeJaxbClassNameErrorDialog(final Exception e) {
        cobolAnnotatedJaxbClassErrorDialog(
                getI18N("ZosFileInputDialog.FailedToGetJaxbQualifiedClassName.DialogMessage"),
                e);
    }

    /**
     * Error dialog while looking up JAXB/COBOL classes.
     * 
     * @param message the message to display
     */
    protected void cobolAnnotatedJaxbClassErrorDialog(final String message) {
        cobolAnnotatedJaxbClassErrorDialog(message, null);
    }

    /**
     * Error dialog while looking up COBOL-annotated JAXB classes.
     * 
     * @param message the message to display
     * @param e the exception (null if none)
     */
    protected void cobolAnnotatedJaxbClassErrorDialog(final String message,
            final Exception e) {
        new ErrorDialog(
                shell,
                getI18N("ZosFileInputDialog.FailedToGetCobolAnnotatedJaxbClass.DialogTitle"),
                message, e);
    }

    /**
     * Add listeners to the COBOL related widgets.
     * Listen to the browse button to select a COBOL file from the file system.
     */
    protected void addCobolFileListeners() {

        wbbCobolFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String filePath = null;
                try {
                    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                    dialog.setFilterExtensions(new String[] { "*.cbl;*.cob",
                            "*" });

                    dialog.setFilterNames(new String[] {
                                getI18N("ZosFileInputDialog.FileType.CobolFiles"),
                                          getI18N("System.FileType.AllFiles") });

                    if (dialog.open() != null) {
                        filePath = dialog.getFilterPath()
                                + System.getProperty("file.separator")
                                + dialog.getFileName();
                        // TODO add COBOL file encoding
                        wCobolSource.setText(FileUtils.readFileToString(new File(
                                filePath)));
                    }
                } catch (IOException e1) {
                    invalidCobolFileDialog(filePath, e1.getMessage());
                }
            }
        });

    }
	
    /**
     * Add listeners to the host charset combo box.
     * Make sure the text entered is in the available charsets list
     */
    protected void addHostCharsetListeners() {

        wHostCharset.addFocusListener(new FocusListener() {
            
            public void focusGained(FocusEvent arg0) {
                return;
            }

            public void focusLost(FocusEvent arg0) {
                for (String item : wHostCharset.getItems()) {
                    if (item.equals(wHostCharset.getText())) {
                        return;
                    }
                }
                invalidHostCharsetDialog(wHostCharset.getText());
            }
        });

    }
	
    /**
     * Enable or disable buttons depending on context.
     */
    protected void enableOrDisable() {

        if (wCobolSource.getText().length() > 0
                || wCompositeJaxbClassNames.getText().length() > 0) {
            wGet.setEnabled(true);
        } else {
            wGet.setEnabled(false);
        }

        if (wFields.table.getItemCount() > 0
                && wFilename.getText().length() > 0) {
            wPreview.setEnabled(true);
        } else {
            wPreview.setEnabled(false);
        }

    }
	
    /**
     * Tell the user we were not able to read the COBOL file.
     * 
     * @param filePath the requested COBOL file path
     * @param exceptionMsg the exception message we got
     */
    protected void invalidCobolFileDialog(final String filePath,
            final String exceptionMsg) {
        new ErrorDialog(
                wHostCharset.getShell(),
                getI18N("ZosFileInputDialog.InvalidCobolFile.DialogTitle"),
                getI18N("ZosFileInputDialog.InvalidCobolFile.DialogMessage",
                        filePath, exceptionMsg),
                null);

    }
    
	/**
	 * User abandons.
	 */
	private void cancel() {
		stepname = null;
		_inputMeta.setChanged(changed);
		dispose();
	}
	
	/**
	 * If user didn't bother to name the step, don't save the meta. 
	 */
	private void ok() {
		if (Const.isEmpty(wStepname.getText())) return;

		setMetaDataFromDialog(_inputMeta);
		stepname = wStepname.getText();
		dispose();
	}
	
	/**
	 * Fields are extracted from a COBOL-annotated JAXB class.
	 * <p/>
	 * If such a class has been identified in the GUI we use it directly.
	 * Otherwise, if we have COBOL code, then the first step if to generate
	 * the COBOL-annotated JAXB classes and then use these.
	 * <p/>
	 * Since these classes are in a location unknown to PDI, we set
	 * a class loader that will contain them.
	 */
    protected void getCobolFields() {

        if (wCobolSource.getText().length() > 0) {
            if (!generateTransformer()) {
                // An error dialog should have been displayed
                return;
            }
        }

        try {
            setDialogFieldsFromMetaData(CobolToPdi.getCobolFields(
                    wCompositeJaxbClassNames.getText(),
                    getClass()));
            wTabFolder.setSelection(2);

        } catch (KettleException e) {
            fieldsErrorDialog(
                    "ZosFileInputDialog.FailedToGetFields.DialogMessage", e);
            return;
        }

    }
    
    /**
     * From COBOL code, generate the Transformer and associated COBOL-annotated
     * JAXB classes.
     * Store results in widgets;
     * 
     * @return true if generation succeeded, false otherwise
     */
    protected boolean generateTransformer() {

        CobolToFieldsProgressDialog progressDialog =
                new CobolToFieldsProgressDialog(wFields.getShell(),
                        wCobolSource.getText());

        if (progressDialog.open()) {

            if (progressDialog.getCompositeJaxbClassNames().size() == 0) {
                fieldsErrorDialog(
                        "ZosFileInputDialog.NoRootClassFailure.DialogMessage",
                        null);
            } else {
                // Update the class list since we just added a jar
                // TODO If more than one JAXB class, popup a selection dialog
                initCompositeJaxbClassNamesCombo(progressDialog
                        .getCompositeJaxbClassNames()
                        .get(0));

                return true;
            }

        }
        return false;
    }

    /**
     * Popup an error dialog.
     * @param messageKey the message I18N key.
     * @param e the associated exception
     */
    protected void fieldsErrorDialog(final String messageKey, final Throwable e) {
        new ErrorDialog(
                wFields.getShell(),
                getI18N("ZosFileInputDialog.FailedToGetFields.DialogTitle"),
                getI18N(messageKey),
                e);

    }

	/**
	 * Service the preview button.
	 */
	protected void preview() {
        ZosFileInputMeta oneMeta = new ZosFileInputMeta();
        setMetaDataFromDialog(oneMeta);

        TransMeta previewMeta = TransPreviewFactory
                .generatePreviewTransformation(transMeta, oneMeta,
                        wStepname.getText());

        EnterNumberDialog numberDialog = new EnterNumberDialog(shell,
                props.getDefaultPreviewSize(),
                getI18N("ZosFileInputDialog.PreviewSize.DialogTitle"),
                getI18N("ZosFileInputDialog.PreviewSize.DialogMessage"));

        int previewSize = numberDialog.open();
        if (previewSize > 0) {
            TransPreviewProgressDialog progressDialog =
                new TransPreviewProgressDialog(
                    shell, previewMeta, new String[] { wStepname.getText() },
                    new int[] { previewSize });
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled()) {
                if (trans.getResult() != null
                        && trans.getResult().getNrErrors() > 0) {
                    EnterTextDialog etd = new EnterTextDialog(shell,
                            getI18N("System.Dialog.PreviewError.Title"),
                            getI18N("System.Dialog.PreviewError.Message"),
                            loggingText, true);
                    etd.setReadOnly();
                    etd.open();
                }
            }

            PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta,
                    SWT.NONE, wStepname.getText(),
                    progressDialog.getPreviewRowsMeta(wStepname.getText()),
                    progressDialog.getPreviewRows(wStepname.getText()),
                    loggingText);
            prd.open();
        }
		
	}
	
    /*
     * ------------------------------------------------------------------------
     * Model Read/Update section
     * ------------------------------------------------------------------------
     */

	/**
     * Copy information from the dialog fields to the meta-data (model).
     * @param meta a set of meta data
     */ 
    private void setMetaDataFromDialog(final ZosFileInputMeta meta) {
        
        meta.setFilename(wFilename.getText());
        meta.setIsVariableLength(wcbIsVariableLength.getSelection());
        meta.setHasRecordDescriptorWord(wcbHasRecordDescriptorWord
                .getSelection());
        meta.setHostCharset(wHostCharset.getText());

        meta.setFromCobolSource(wCobolSource.getText().length() > 0);
        meta.setCompositeJaxbClassName(wCompositeJaxbClassNames.getText());
        meta.setCobolSource(wCobolSource.getText());
        
        int nrNonEmptyFields = wFields.nrNonEmpty(); 
        meta.setInputFields(new CobolFileInputField[nrNonEmptyFields]);

        for (int i=0;i<nrNonEmptyFields;i++) {
            TableItem item = wFields.getNonEmpty(i);
            meta.getInputFields()[i] = new CobolFileInputField();
            
            int colnr=1;
            meta.getInputFields()[i].setName( item.getText(colnr++) );
            meta.getInputFields()[i].setType( ValueMeta.getType( item.getText(colnr++) ) );
            meta.getInputFields()[i].setLength( Const.toInt(item.getText(colnr++), -1) );
            meta.getInputFields()[i].setPrecision( Const.toInt(item.getText(colnr++), -1) );
            meta.getInputFields()[i].setTrimType(ValueMeta.getTrimTypeByDesc( item.getText(colnr++) ));
            meta.getInputFields()[i].setRedefined(item.getText(colnr++).equals("*"));
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);
        
        meta.setChanged();
    }

    /**
     * Copy information from the meta-data (model) to the dialog fields.
     */ 
    public void setDialogFromMetaData() {
        wStepname.setText(stepname);
        
        wFilename.setText(Const.NVL(_inputMeta.getFilename(), ""));
        wcbIsVariableLength.setSelection(_inputMeta.isVariableLength());
        wcbHasRecordDescriptorWord.setSelection(_inputMeta.hasRecordDescriptorWord());
        setHostCharsetFromMetaData();

        if (_inputMeta.isFromCobolSource()) {
            wrbCobolSourceInput.setSelection(true);
            wrbCobolJAXBInput.setSelection(false);
        } else {
            wrbCobolSourceInput.setSelection(false);
            wrbCobolJAXBInput.setSelection(true);
        }
        initCompositeJaxbClassNamesCombo(_inputMeta.getCompositeJaxbClassName());
        wCobolSource.setText(Const.NVL(_inputMeta.getCobolSource(), ""));
        doSelectInputType();
        
        setDialogFieldsFromMetaData(_inputMeta.getInputFields());

        wStepname.selectAll();
    }
    
    /**
     * The host character set might not exist in this VM in which case
     * we need to warn the user that he needs to get charsets.jar.
     */
    public void setHostCharsetFromMetaData() {
        for (String item : wHostCharset.getItems()) {
            if (item.equals(_inputMeta.getHostCharset())) {
                wHostCharset.setText(item);
                return;
            }
        }
        invalidHostCharsetDialog(_inputMeta.getHostCharset());
    }
    
    /**
     * Tell the user the host charset is not good. Probably due to a missing
     * charsets.jar.
     * 
     * @param hostCharset the erroneous charset
     */
    protected void invalidHostCharsetDialog(final String hostCharset) {
        new ErrorDialog(
                wHostCharset.getShell(),
                getI18N("ZosFileInputDialog.InvalidHostCharset.DialogTitle"),
                getI18N("ZosFileInputDialog.InvalidHostCharset.DialogMessage",
                        hostCharset),
                null);

    }
    
    /**
     * Copy the fields info from meta data to the dialog fields.
     * @param fields the fields meta data
     * 
     */
    public void setDialogFieldsFromMetaData(final CobolFileInputField[] fields) {
        wFields.table.removeAll();
        for (int i=0; i< fields.length; i++) {
            CobolFileInputField field = fields[i];
            
            TableItem item = new TableItem(wFields.table, SWT.NONE);
            int colnr=1;
            item.setText(colnr++, Const.NVL(field.getName(), ""));
            item.setText(colnr++, ValueMeta.getTypeDesc(field.getType()));
            item.setText(colnr++, field.getLength()>=0?Integer.toString(field.getLength()):"") ;
            item.setText(colnr++, field.getPrecision()>=0?Integer.toString(field.getPrecision()):"") ;
            item.setText(colnr++, Const.NVL(field.getTrimTypeDesc(), ""));
            if (field.isRedefined()) {
                item.setText(colnr++, "*");
            }
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);
    }

    /*
     * ------------------------------------------------------------------------
     * I18N methods.
     * ------------------------------------------------------------------------
     */
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

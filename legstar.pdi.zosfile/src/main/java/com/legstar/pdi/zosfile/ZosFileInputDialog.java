package com.legstar.pdi.zosfile;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
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

	/** The JAXB qualified root class name. */
	private TextVar      wJaxbQualifiedClassName;
	
	/** Browse button to locate the JAXB qualified root class name. */
	private Button       wbbJaxbQualifiedClassName; // 
	
    /** The host character set. */
	private Combo        wHostCharset;

	/** The z/OS file name. */
	private TextVar      wFilename;

	/** Browse button to locate the z/OS file. */
	private Button       wbbFilename;
	
    /** A checkbox for variable length records. */
    private Button       wcbIsVariableLength;

	/** A checkbox in case records start with an RDW. */
	private Button       wcbHasRecordDescriptorWord;

	
	/** The z/OS file record fields that need to be extracted.*/
	private TableView    wFields;
	
	public ZosFileInputDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		_inputMeta = (ZosFileInputMeta) in;
	}

	/**
	 * Assemple the dialog widgets and display.
	 */
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, _inputMeta);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				_inputMeta.setChanged();
				enableOrDisable();
			}
		};
		changed = _inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "ZosFileInputDialog.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Add the step name
		addStepName(middle, margin, lsMod);
		Control lastControl = wStepname;
		
		// Add the JAXB qualified class name
		addJaxbQualifiedClassName(lastControl, middle, margin, lsMod);
		lastControl = wJaxbQualifiedClassName;
		
        // Add the host character set combo box
        addHostCharset(lastControl, middle, margin, lsMod);
        lastControl = wHostCharset;
        
		// Add the z/OS file name
		addFilename(lastControl, middle, margin, lsMod);
		lastControl = wFilename;
		
        // Add variable length checkbox
        addIsVariableLength(lastControl, middle, margin, lsMod);
        lastControl = wcbIsVariableLength;

		// Add RDW checkbox
		addHasRecordDescriptorWord(lastControl, middle, margin, lsMod);
        lastControl = wcbHasRecordDescriptorWord;

		// Add OK, cancel, and preview buttons
		addButtons(margin);      

		// Add Fields
		addFields(lastControl, middle, margin, lsMod);
		
		// Add listeners
		addListeners();
		
		// Set the shell size, based upon previous time...
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
	 * Add a setp name widget.
	 * @param middle percentage of the parent widget 
	 * @param margin offset from that position
	 * @param lsMod a modification listener
	 */
	protected void addStepName(
			final int middle,
			final int margin,
			final ModifyListener lsMod) {
		// Step name line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
	}
	
	
	/**
	 * Add the JAXB qualified class name widget and associated browse button.
	 * @param lastControl the last control to position from
	 * @param middle percentage of the parent widget 
	 * @param margin offset from that position
	 * @param lsMod a modification listener
	 */
	protected void addJaxbQualifiedClassName(
			final Control lastControl,
			final int middle,
			final int margin,
			final ModifyListener lsMod) {
		
        //
        // The JAXB qualified class browse button
        //
        wbbJaxbQualifiedClassName = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbbJaxbQualifiedClassName);
        wbbJaxbQualifiedClassName.setText(BaseMessages.getString(PKG,
                "System.Button.Browse"));
        wbbJaxbQualifiedClassName.setToolTipText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.Tooltip.BrowseForJAXBClass"));
        FormData fdbJaxbQualifiedClassName = new FormData();
        fdbJaxbQualifiedClassName.top = new FormAttachment(lastControl, margin);
        fdbJaxbQualifiedClassName.right = new FormAttachment(100, 0);
        wbbJaxbQualifiedClassName.setLayoutData(fdbJaxbQualifiedClassName);

        // The field itself...
        //
        Label wlJaxbQualifiedClassName = new Label(shell, SWT.RIGHT);
        wlJaxbQualifiedClassName.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.JaxbQualifiedClassName.Label")); //$NON-NLS-1$
        props.setLook(wlJaxbQualifiedClassName);
        FormData fdlJaxbQualifiedClassName = new FormData();
        fdlJaxbQualifiedClassName.top = new FormAttachment(lastControl, margin);
        fdlJaxbQualifiedClassName.left = new FormAttachment(0, 0);
        fdlJaxbQualifiedClassName.right = new FormAttachment(middle, -margin);
        wlJaxbQualifiedClassName.setLayoutData(fdlJaxbQualifiedClassName);
        wJaxbQualifiedClassName = new TextVar(transMeta, shell, SWT.SINGLE
                | SWT.LEFT | SWT.BORDER);
        props.setLook(wJaxbQualifiedClassName);
        wJaxbQualifiedClassName.addModifyListener(lsMod);
        FormData fdJaxbQualifiedClassName = new FormData();
        fdJaxbQualifiedClassName.top = new FormAttachment(lastControl, margin);
        fdJaxbQualifiedClassName.left = new FormAttachment(middle, 0);
        fdJaxbQualifiedClassName.right = new FormAttachment(
                wbbJaxbQualifiedClassName, -margin);
        wJaxbQualifiedClassName.setLayoutData(fdJaxbQualifiedClassName);
	}
	
    /**
     * Add the z/OS character set.
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @param lsMod a modification listener
     */
    protected void addHostCharset(
            final Control lastControl,
            final int middle,
            final int margin,
            final ModifyListener lsMod) {
        Label wlHostCharset = new Label(shell, SWT.RIGHT);
        wlHostCharset.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.HostCharset.Label"));
        props.setLook(wlHostCharset);
        FormData fdlHostCharset = new FormData();
        fdlHostCharset.left = new FormAttachment(0, 0);
        fdlHostCharset.top = new FormAttachment(lastControl, margin);
        fdlHostCharset.right = new FormAttachment(middle, -margin);
        wlHostCharset.setLayoutData(fdlHostCharset);
        wHostCharset = new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHostCharset.setToolTipText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.HostCharset.Tooltip"));
        wHostCharset.setItems(availableCharsets());
        props.setLook(wHostCharset);
        FormData fdHostCharset = new FormData();
        fdHostCharset.left = new FormAttachment(middle, 0);
        fdHostCharset.top = new FormAttachment(lastControl, margin);
        fdHostCharset.right = new FormAttachment(100, 0);
        wHostCharset.setLayoutData(fdHostCharset);
        wHostCharset.addModifyListener(lsMod);

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
	 * Add the z/OS file name widget and associated browse button.
	 * @param lastControl the last control to position from
	 * @param middle percentage of the parent widget 
	 * @param margin offset from that position
	 * @param lsMod a modification listener
	 */
	protected void addFilename(
			final Control lastControl,
			final int middle,
			final int margin,
			final ModifyListener lsMod) {
		
		//
		// The filename browse button
		//
        wbbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbFilename = new FormData();
        fdbFilename.top  = new FormAttachment(lastControl, margin);
        fdbFilename.right= new FormAttachment(100, 0);
        wbbFilename.setLayoutData(fdbFilename);

        // The field itself...
        //
		Label wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "ZosFileInputDialog.Filename.Label")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		FormData fdlFilename = new FormData();
		fdlFilename.top  = new FormAttachment(lastControl, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top  = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
	}
	
    /**
     * Add a check box for variable length records.
     * 
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @param lsMod a modification listener
     */
    protected void addIsVariableLength(
            final Control lastControl,
            final int middle,
            final int margin,
            final ModifyListener lsMod) {
        Label wlIsVariableLength = new Label(shell, SWT.RIGHT);
        wlIsVariableLength.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.IsVariableLength.Label"));
        props.setLook(wlIsVariableLength);
        FormData fdlIncludeFilename = new FormData();
        fdlIncludeFilename.top = new FormAttachment(lastControl, margin);
        fdlIncludeFilename.left = new FormAttachment(0, 0);
        fdlIncludeFilename.right = new FormAttachment(middle, -margin);
        wlIsVariableLength.setLayoutData(fdlIncludeFilename);
        wcbIsVariableLength = new Button(shell, SWT.CHECK);
        wcbIsVariableLength.setToolTipText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.Tooltip.IsVariableLength"));
        props.setLook(wcbIsVariableLength);
        FormData fdIncludeFilename = new FormData();
        fdIncludeFilename.top = new FormAttachment(lastControl, margin);
        fdIncludeFilename.left = new FormAttachment(middle, 0);
        fdIncludeFilename.right = new FormAttachment(100, 0);
        wcbIsVariableLength.setLayoutData(fdIncludeFilename);
    }
    
	/**
	 * Add a check box for records starting with an RDW.
	 * 
     * @param lastControl the last control to position from
     * @param middle percentage of the parent widget 
     * @param margin offset from that position
     * @param lsMod a modification listener
	 */
	protected void addHasRecordDescriptorWord(
            final Control lastControl,
            final int middle,
            final int margin,
            final ModifyListener lsMod) {
        Label wlHasRecordDescriptorWord = new Label(shell, SWT.RIGHT);
        wlHasRecordDescriptorWord.setText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.HasRecordDescriptorWord.Label"));
        props.setLook(wlHasRecordDescriptorWord);
        FormData fdlIncludeFilename = new FormData();
        fdlIncludeFilename.top = new FormAttachment(lastControl, margin);
        fdlIncludeFilename.left = new FormAttachment(0, 0);
        fdlIncludeFilename.right = new FormAttachment(middle, -margin);
        wlHasRecordDescriptorWord.setLayoutData(fdlIncludeFilename);
        wcbHasRecordDescriptorWord = new Button(shell, SWT.CHECK);
        wcbHasRecordDescriptorWord.setToolTipText(BaseMessages.getString(PKG,
                "ZosFileInputDialog.Tooltip.HasRecordDescriptorWord"));
        props.setLook(wcbHasRecordDescriptorWord);
        FormData fdIncludeFilename = new FormData();
        fdIncludeFilename.top = new FormAttachment(lastControl, margin);
        fdIncludeFilename.left = new FormAttachment(middle, 0);
        fdIncludeFilename.right = new FormAttachment(100, 0);
        wcbHasRecordDescriptorWord.setLayoutData(fdIncludeFilename);
	}
	
	/**
	 * Add the OK, cancel, get and preview buttons.
	 * @param margin offset from parent widget
	 */
	protected void addButtons(final int margin) {
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wPreview.setEnabled(false);

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);
	}
	
	/**
	 * Add a grid for z/OS record fields.
	 * @param lastControl the last control to position from
	 * @param middle percentage of the parent widget 
	 * @param margin offset from that position
	 * @param lsMod a modification listener
	 */
	protected void addFields(
			final Control lastControl,
			final int middle,
			final int margin,
			final ModifyListener lsMod) {

		boolean readOnly = true;
		ColumnInfo[] colinf = new ColumnInfo[] {
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.NameColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.TypeColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.LengthColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.PrecisionColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.TrimTypeColumn.Column"),
						ColumnInfo.COLUMN_TYPE_TEXT, false, readOnly),
				new ColumnInfo(BaseMessages.getString(PKG,
						"ZosFileInputDialog.RedefinedColumn.Column"),
						ColumnInfo.COLUMN_TYPE_BUTTON, false, readOnly)
				};

		wFields = new TableView(transMeta, shell, SWT.FULL_SELECTION
				| SWT.MULTI, colinf, 1, readOnly, lsMod, props);

		FormData fdFields = new FormData();
		fdFields.top = new FormAttachment(lastControl, margin * 2);
		fdFields.bottom = new FormAttachment(wOK, -margin * 2);
		fdFields.left = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);
	}
	
	/**
	 * Add various listeners to widgets and shell.
	 */
	protected void addListeners() {

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

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wPreview.addListener(SWT.Selection, lsPreview);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		addJaxbQualifiedClassNameListeners();
        addHostCharsetListeners();
		addFilenameListeners();

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
				dialog.setFilterExtensions(new String[] { "*.dat;*.bin",
						"*.dat", "*.bin", "*" });
				if (wFilename.getText() != null) {
					String fname = transMeta.environmentSubstitute(wFilename
							.getText());
					dialog.setFileName(fname);
				}

				dialog
						.setFilterNames(new String[] {
								BaseMessages
										.getString(PKG,
												"ZosFileInputDialog.FileType.DataFiles")
										+ ", "
										+ BaseMessages
												.getString(PKG,
														"ZosFileInputDialog.FileType.BinaryFiles"),
								BaseMessages
										.getString(PKG,
												"ZosFileInputDialog.FileType.DataFiles"),
								BaseMessages
										.getString(PKG,
												"ZosFileInputDialog.FileType.BinaryFiles"),
								BaseMessages.getString(PKG,
										"System.FileType.AllFiles") });

				if (dialog.open() != null) {
					String str = dialog.getFilterPath()
							+ System.getProperty("file.separator")
							+ dialog.getFileName();
					wFilename.setText(str);
				}
			}
		});

	}
	
	/**
	 * Add listeners to the JAXB qualified class name related widgets.
	 * Listen to the browse button next to JAXB qualified class name.
	 */
	protected void addJaxbQualifiedClassNameListeners() {

		wJaxbQualifiedClassName.addSelectionListener(lsDef);

        wbbJaxbQualifiedClassName.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    List < String > jaxbclassNames = CobolToPdi
                            .getAvailableJaxbClassNames();
                    if (jaxbclassNames == null) {
                        jaxbQualifiedClassNameErrorDialog(BaseMessages
                                .getString(
                                        PKG,
                                        "ZosFileInputDialog.NoJarsInLib.DialogMessage",
                                        CobolToPdi.getPluginLibFolder()
                                                .getFolder()));
                    } else {
                        if (jaxbclassNames.size() == 0) {
                            jaxbQualifiedClassNameErrorDialog(BaseMessages
                                    .getString(
                                            PKG,
                                            "ZosFileInputDialog.NoJAXBCOBOLClassesInLib.DialogMessage",
                                            CobolToPdi
                                                    .getPluginLibFolder()
                                                    .getFolder()));
                        } else {
                            jaxbQualifiedClassNameSelectionDialog(
                                    jaxbclassNames
                                            .toArray(new String[jaxbclassNames
                                                    .size()]));
                        }
                    }
                } catch (KettleFileException e1) {
                    jaxbQualifiedClassNameErrorDialog(e1);
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
	 * Display the list of available JAXB/COBOL classes and allow
	 * user to select one.
	 * Also initialize the fields list.
	 * @param jaxbClassNames the list of available JAXB/COBOL classes.
	 */
	protected void jaxbQualifiedClassNameSelectionDialog(
			final String[] jaxbClassNames) {

		EnterSelectionDialog dialog = new EnterSelectionDialog(shell,
				jaxbClassNames, BaseMessages.getString(PKG,
						"ZosFileInputDialog.SelectJAXBClass.ShellText"),
				BaseMessages.getString(PKG,
						"ZosFileInputDialog.SelectJAXBClass.Message"));
		dialog.setMulti(false);
		if (dialog.open() != null) {
			if (dialog.getSelectionNr() > -1) {
				wJaxbQualifiedClassName
						.setText(jaxbClassNames[dialog.getSelectionNr()]);
				getCobolFields();
			}
		}
	}
	
	/**
	 * A generic error dialog when an exception is raise while looking up JAXB/COBOL classes.
	 * @param e the exception that occured
	 */
	protected void jaxbQualifiedClassNameErrorDialog(final Exception e) {
		jaxbQualifiedClassNameErrorDialog(
				BaseMessages
						.getString(PKG,
								"ZosFileInputDialog.FailedToGetJaxbQualifiedClassName.DialogMessage"),
				e);
	}
	
	/**
	 * Error dialog while looking up JAXB/COBOL classes.
	 * @param message the message to display
	 */
	protected void jaxbQualifiedClassNameErrorDialog(final String message) {
		jaxbQualifiedClassNameErrorDialog(message, null);
	}

	/**
	 * Error dialog while looking up JAXB/COBOL classes.
	 * @param message the message to display
	 * @param e the excption (null if none)
	 */
	protected void jaxbQualifiedClassNameErrorDialog(final String message,
			final Exception e) {
		new ErrorDialog(
				wbbJaxbQualifiedClassName.getShell(),
				BaseMessages
						.getString(PKG,
								"ZosFileInputDialog.FailedToGetJaxbQualifiedClassName.DialogTitle"),
				message, e);
	}

	/**
	 * Enable or disable buttons depending on context.
	 */
	protected void enableOrDisable() {
		if (wJaxbQualifiedClassName.getText().length() > 0
				&& wFilename.getText().length() > 0) {
			wPreview.setEnabled(true);
		} else {
			wPreview.setEnabled(false);
		}

	}
	
	/**
	 * Copy information from the meta-data (model) to the dialog fields.
	 */ 
	public void setDialogFromMetaData() {
		wStepname.setText(stepname);
		wJaxbQualifiedClassName.setText(Const.NVL(_inputMeta
				.getJaxbQualifiedClassName(), ""));
		setHostCharsetDialogFromMetaData();
		wFilename.setText(Const.NVL(_inputMeta.getFilename(), ""));
		wcbIsVariableLength.setSelection(_inputMeta.isVariableLength());
		wcbHasRecordDescriptorWord.setSelection(_inputMeta.hasRecordDescriptorWord());
		
		setDialogFields(_inputMeta.getInputFields());

		wStepname.selectAll();
	}
	
	/**
	 * The host character set might not exist in this VM in which case
	 * we need to warn the user that he needs to get charsets.jar.
	 */
	public void setHostCharsetDialogFromMetaData() {
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
                wbbJaxbQualifiedClassName.getShell(),
                BaseMessages.getString(PKG,
                        "ZosFileInputDialog.InvalidHostCharset.DialogTitle"),
                BaseMessages.getString(PKG,
                        "ZosFileInputDialog.InvalidHostCharset.DialogMessage", hostCharset),
                null);

    }
	
	/**
	 * Copy the fields info from meta data to the dialog fields.
	 * @param fields the fields meta data
	 * 
	 */
	public void setDialogFields(final CobolFileInputField[] fields) {
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

	/**
	 * Copy information from the dialog fields to the meta-data (model).
	 * @param meta a set of meta data
	 */ 
	private void setMetaDataFromDialog(final ZosFileInputMeta meta) {
		
        meta.setJaxbQualifiedClassName(wJaxbQualifiedClassName.getText());
        meta.setHostCharset(wHostCharset.getText());
        meta.setFilename(wFilename.getText());
        meta.setIsVariableLength(wcbIsVariableLength.getSelection());
        meta.setHasRecordDescriptorWord(wcbHasRecordDescriptorWord
                .getSelection());
		
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
	 * Retrieve fields from the COBOL-annotated JAXB class.
	 * Since these classes are in a location unknown to PDI, we set
	 * a class loader that will contain them.
	 */
	protected void getCobolFields() {
		try {
		    CobolToPdi.setLibClassLoader(getClass());
			
			setDialogFields(CobolToPdi.toFieldArray(
					wJaxbQualifiedClassName.getText()));

		} catch (KettleException e) {
			new ErrorDialog(
					wFields.getShell(),
					BaseMessages.getString(PKG,
							"ZosFileInputDialog.FailedToGetFields.DialogTitle"),
					BaseMessages.getString(PKG,
							"ZosFileInputDialog.FailedToGetFields.DialogMessage"),
					e);
		}

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
                props.getDefaultPreviewSize(), BaseMessages.getString(PKG,
                        "ZosFileInputDialog.PreviewSize.DialogTitle"),
                BaseMessages.getString(PKG,
                        "ZosFileInputDialog.PreviewSize.DialogMessage"));
        int previewSize = numberDialog.open();
        if (previewSize > 0) {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(
                    shell, previewMeta, new String[] { wStepname.getText() },
                    new int[] { previewSize });
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled()) {
                if (trans.getResult() != null
                        && trans.getResult().getNrErrors() > 0) {
                    EnterTextDialog etd = new EnterTextDialog(shell,
                            BaseMessages.getString(PKG,
                                    "System.Dialog.PreviewError.Title"),
                            BaseMessages.getString(PKG,
                                    "System.Dialog.PreviewError.Message"),
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
}

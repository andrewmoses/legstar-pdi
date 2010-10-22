package com.legstar.pdi.zosfile;

import java.util.List;


import org.eclipse.swt.SWT;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import com.legstar.pdi.CobolFileInputField;
import com.legstar.pdi.CobolToPdi;

/**
 * A PDI dialog to setup the z/OS File Input step.
 * TODO Add possibility of getting the file name from a previous step
 * TODO Add possibility to pass the file name to the next step (when it is received as a parameter)
 * TODO Add a preview button
 *
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

	/** The z/OS file name. */
	private TextVar      wFilename;

	/** Browse button to locate the z/OS file. */
	private Button       wbbFilename; // 
	
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
		
		// Add the z/OS file name
		addFilename(lastControl, middle, margin, lsMod);
		lastControl = wFilename;

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
        wbbJaxbQualifiedClassName=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbJaxbQualifiedClassName);
        wbbJaxbQualifiedClassName.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        wbbJaxbQualifiedClassName.setToolTipText(BaseMessages.getString(PKG, "ZosFileInputDialog.Tooltip.BrowseForJAXBClass"));
        FormData fdbJaxbQualifiedClassName = new FormData();
        fdbJaxbQualifiedClassName.top  = new FormAttachment(lastControl, margin);
        fdbJaxbQualifiedClassName.right= new FormAttachment(100, 0);
        wbbJaxbQualifiedClassName.setLayoutData(fdbJaxbQualifiedClassName);

        // The field itself...
        //
		Label wlJaxbQualifiedClassName = new Label(shell, SWT.RIGHT);
		wlJaxbQualifiedClassName.setText(BaseMessages.getString(PKG, "ZosFileInputDialog.JaxbQualifiedClassName.Label")); //$NON-NLS-1$
 		props.setLook(wlJaxbQualifiedClassName);
		FormData fdlJaxbQualifiedClassName = new FormData();
		fdlJaxbQualifiedClassName.top  = new FormAttachment(lastControl, margin);
		fdlJaxbQualifiedClassName.left = new FormAttachment(0, 0);
		fdlJaxbQualifiedClassName.right= new FormAttachment(middle, -margin);
		wlJaxbQualifiedClassName.setLayoutData(fdlJaxbQualifiedClassName);
		wJaxbQualifiedClassName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wJaxbQualifiedClassName);
		wJaxbQualifiedClassName.addModifyListener(lsMod);
		FormData fdJaxbQualifiedClassName = new FormData();
		fdJaxbQualifiedClassName.top  = new FormAttachment(lastControl, margin);
		fdJaxbQualifiedClassName.left = new FormAttachment(middle, 0);
		fdJaxbQualifiedClassName.right= new FormAttachment(wbbJaxbQualifiedClassName, -margin);
		wJaxbQualifiedClassName.setLayoutData(fdJaxbQualifiedClassName);
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
		wFilename.setText(Const.NVL(_inputMeta.getFilename(), ""));
		setDialogFields(_inputMeta.getInputFields());

		wStepname.selectAll();
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
	 */ 
	private void setMetaDataFromDialog() {
		
		_inputMeta.setJaxbQualifiedClassName(wJaxbQualifiedClassName.getText());
		_inputMeta.setFilename(wFilename.getText());
		
    	int nrNonEmptyFields = wFields.nrNonEmpty(); 
    	_inputMeta.setInputFields(new CobolFileInputField[nrNonEmptyFields]);

		for (int i=0;i<nrNonEmptyFields;i++) {
			TableItem item = wFields.getNonEmpty(i);
			_inputMeta.getInputFields()[i] = new CobolFileInputField();
			
			int colnr=1;
			_inputMeta.getInputFields()[i].setName( item.getText(colnr++) );
			_inputMeta.getInputFields()[i].setType( ValueMeta.getType( item.getText(colnr++) ) );
			_inputMeta.getInputFields()[i].setLength( Const.toInt(item.getText(colnr++), -1) );
			_inputMeta.getInputFields()[i].setPrecision( Const.toInt(item.getText(colnr++), -1) );
			_inputMeta.getInputFields()[i].setTrimType(ValueMeta.getTrimTypeByDesc( item.getText(colnr++) ));
			_inputMeta.getInputFields()[i].setRedefined(item.getText(colnr++).equals("*"));
		}
		wFields.removeEmptyRows();
		wFields.setRowNums();
		wFields.optWidth(true);
		
		_inputMeta.setChanged();
	}

	private void cancel() {
		stepname = null;
		_inputMeta.setChanged(changed);
		dispose();
	}
	
	// let the plugin know about the entered data
	private void ok() {
		if (Const.isEmpty(wStepname.getText())) return;

		setMetaDataFromDialog();
		stepname = wStepname.getText();
		dispose();
	}
	
	/**
	 * Retrieve fields from the JAXB/COBOL class.
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

	protected void preview() {
		
	}
}

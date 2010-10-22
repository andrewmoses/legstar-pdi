package com.legstar.pdi.zosfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import com.legstar.pdi.CobolToPdi;


/**
 * This is the actual processing for a given row.
 * We read an input file and transform to java in order to populate output row fields.
 * TODO add multiple input files support
 * TODO add parallel processing
 */
public class ZosFileInput extends BaseStep implements StepInterface {

	private static Class<?> PKG = ZosFileInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	private ZosFileInputData data;
	private ZosFileInputMeta meta;
	
	public ZosFileInput(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	/**
	 * Step is initialized.
	 * Set the classpath so that JAXB/COXB classes are located.
	 * Instantiate the transformers.
	 * @param smi Step meta 
	 * @param sdi Step data
	 * @return false if anything goes wrong
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (ZosFileInputMeta) smi;
        data = (ZosFileInputData) sdi;

        try {
            CobolToPdi.setLibClassLoader(getClass());

            data.jaxbQualifiedClassname = environmentSubstitute(meta
                    .getJaxbQualifiedClassName());
            if (Const.isEmpty(data.jaxbQualifiedClassname)) {
                logError(BaseMessages.getString(PKG,
                        "ZosFileInput.MissingJaxbClassName.Message"));
                return false;
            }
            data.tf = CobolToPdi
                    .newTransformers(data.jaxbQualifiedClassname);
        } catch (KettleException e) {
            logError(BaseMessages.getString(PKG,
                    "ZosFileInput.TransformersNotFound.Message",
                    data.jaxbQualifiedClassname), e);
            return false;
        }

        data.filename = environmentSubstitute(meta.getFilename());
        if (Const.isEmpty(data.filename)) {
            logError(BaseMessages.getString(PKG,
                    "ZosFileInput.MissingFilename.Message"));
            return false;
        }
        logBasic(BaseMessages.getString(PKG,
                "ZosFileInput.ReadingFromFile.Message", data.filename));

        return super.init(smi, sdi);
	}

	/**
	 * Process a single row (this is repeated for all rows on input).
	 * The file is lazily opened on the first row. 
	 * @param smi Step meta 
	 * @param sdi Step data
	 * @throws KettleException if something goes wrong
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (ZosFileInputMeta) smi;
		data = (ZosFileInputData) sdi;

		if (first) {
			first = false;

			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
    		try {
				data.fis = new FileInputStream(new File(data.filename));
				data.hostRecord = new byte[CobolToPdi.hostByteLength(data.tf)];
			} catch (FileNotFoundException e) {
				throw new KettleException(e);
			}

			logBasic(BaseMessages.getString(PKG,
					"ZosFileInput.FileOpened.Message", data.filename));

		}

		try {
			int count = data.fis.read(data.hostRecord);
			
			if (count > 0) {
				Object[] outputRowData = CobolToPdi.toOutputRowData(
						data.outputRowMeta, data.tf, data.hostRecord);
				putRow(data.outputRowMeta, outputRowData);
				if (checkFeedback(getLinesRead())) {
					logBasic(BaseMessages.getString(PKG,
							"ZosFileInput.LinesRead.Message", getLinesRead(),
							data.filename));
				}

			} else {
				setOutputDone();
				return false;
			}
		} catch (IOException e) {
			throw new KettleException(e);
		}

		return true;
	}

	/**
	 * Step is no longer needed.
	 * Make sure resources are freed.
	 * @param smi Step meta 
	 * @param sdi Step data 
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (ZosFileInputMeta) smi;
		data = (ZosFileInputData) sdi;
		
		closeFile();

		super.dispose(smi, sdi);
	}
	
	/**
	 * Close the input file.
	 */
	public void closeFile() {
		try {
			if (data.fis != null) {
				data.fis.close();
			}
		} catch (IOException e) {
			logError(BaseMessages.getString(PKG,
					"ZosFileInput.FileCloseError.Message", data.filename), e);
		}
	}
	
}

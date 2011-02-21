package com.legstar.pdi.zosfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.pdi.Cob2Pdi;
import com.legstar.pdi.ZosFileInputStreamFactory;


/**
 * This is the actual processing for a given row.
 * We read an input file and transform to java in order to populate output row fields.
 * TODO add multiple input files support
 * TODO add parallel processing
 */
public class ZosFileInput extends BaseStep implements StepInterface {

    /** For i18n purposes. */
	private static Class<?> PKG = ZosFileInput.class;
	
	/** Step per-thread data. */
	private ZosFileInputData data;

	/** Step meta-data. */
	private ZosFileInputMeta meta;
	
    /**
     * Creates an instance of the runtime processor.
     * 
     * @param stepMeta The StepMeta object to run.
     * @param stepDataInterface the data object to store temporary data,
     *            database connections, caches, result sets,
     *            hashtables etc.
     * @param copyNr The copynumber for this step.
     * @param transMeta The TransInfo of which the step stepMeta is part of.
     * @param trans The (running) transformation to obtain information shared
     *            among the steps.
     */
    public ZosFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
            int copyNr,
            TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

	/**
	 * Step is initialized.
	 * Sanity checks the parameters.
	 * @param smi Step meta 
	 * @param sdi Step data
	 * @return false if anything goes wrong
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (ZosFileInputMeta) smi;
        data = (ZosFileInputData) sdi;

        data.filename = environmentSubstitute(meta.getFilename());
        if (Const.isEmpty(data.filename)) {
            logError(BaseMessages.getString(PKG,
                    "ZosFileInput.MissingFilename.Message"));
            return false;
        }
        logBasic(BaseMessages.getString(PKG,
                "ZosFileInput.ReadingFromFile.Message", data.filename));

        data.compositeJaxbClassName = environmentSubstitute(meta
                .getCompositeJaxbClassName());
        if (Const.isEmpty(data.compositeJaxbClassName)) {
            logError(BaseMessages.getString(PKG,
                    "ZosFileInput.MissingJaxbClassName.Message"));
            return false;
        }

        logBasic(BaseMessages.getString(PKG,
                "ZosFileInput.UsingJAXBClass.Message", data.compositeJaxbClassName));

        return super.init(smi, sdi);
	}

    /**
     * Process a single row (this is repeated for all rows on input).
     * On the first row, we do the following:
     * <ul>
     * <li>Setup a class loader that contains the Transformer jar file</li>
     * <li>Use this class loader to create a new instance of the transformer</li>
     * <li>The transformer will be reused for all subsequent rows</li>
     * <li>Open the zos file</li>
     * </ul>
     * Here we also keep track of how many bytes from the file record were
     * actually consumed by the transformers. This allows the leftover to
     * be processed on the next call to this method.
     * 
     * @param smi Step meta
     * @param sdi Step data
     * @throws KettleException if something goes wrong
     */
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
            throws KettleException {
        meta = (ZosFileInputMeta) smi;
        data = (ZosFileInputData) sdi;

        if (first) {
            first = false;

            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Cob2Pdi.setTransformerClassLoader(getClass(),
                        Cob2Pdi.getJarFileName(data.compositeJaxbClassName));
                data.tf = Cob2Pdi.newTransformers(
                        Cob2Pdi
                                .getJaxbClassName(data.compositeJaxbClassName));
                data.fis = ZosFileInputStreamFactory.create(meta, new File(
                        data.filename));
                data.hostRecord = Cob2Pdi.newHostRecord(data.tf);
                data.hostCharset = meta.getHostCharset();
                data.status = new HostTransformStatus();
            } catch (FileNotFoundException e) {
                throw new KettleException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }

            logBasic(BaseMessages.getString(PKG,
                    "ZosFileInput.FileOpened.Message", data.filename));

        }

        try {
            // Tell the reader how many bytes we processed last time
            int count = data.fis.read(data.hostRecord,
                    data.status.getHostBytesProcessed());

            if (count > 0) {
                Object[] outputRowData = Cob2Pdi.toOutputRowData(
                        data.outputRowMeta,
                        data.tf,
                        data.hostRecord,
                        data.hostCharset,
                        data.status);
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

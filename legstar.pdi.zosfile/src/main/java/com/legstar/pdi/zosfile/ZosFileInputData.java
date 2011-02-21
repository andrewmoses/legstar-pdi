package com.legstar.pdi.zosfile;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.legstar.coxb.ICobolComplexBinding;
import com.legstar.coxb.transform.HostTransformStatus;
import com.legstar.pdi.io.ZosFileInputStream;

/**
 * Each thread have a separate instance of this class. So this class is
 * basically a thread context where the process can keep data.
 * 
 */
public class ZosFileInputData extends BaseStepData implements StepDataInterface {

    public RowMetaInterface outputRowMeta;

    public String filename;

    public String compositeJaxbClassName;

    public String hostCharset;

    public ZosFileInputStream fis;

    public HostTransformStatus status;

    public ICobolComplexBinding cobolBinding;

    byte[] hostRecord;

    public ZosFileInputData() {
        super();
    }
}

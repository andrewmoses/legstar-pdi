package com.legstar.pdi.zosfile;

import java.io.FileInputStream;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.legstar.coxb.transform.AbstractTransformers;

/**
 * Each thread have a separate instance of this class. So this class is
 * basically a thread context where the process can keep data.
 * 
 */
public class ZosFileInputData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;

	public String filename;
	
	public String jaxbQualifiedClassname;
	
	public FileInputStream fis;

	AbstractTransformers tf;
	byte[] hostRecord;

	public ZosFileInputData() {
		super();
	}
}	

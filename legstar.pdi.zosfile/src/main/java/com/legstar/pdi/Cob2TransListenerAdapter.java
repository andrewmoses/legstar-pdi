package com.legstar.pdi;

import org.eclipse.core.runtime.IProgressMonitor;

import com.legstar.coxb.cob2trans.Cob2TransEvent;
import com.legstar.coxb.cob2trans.Cob2TransEvent.EventType;
import com.legstar.coxb.cob2trans.Cob2TransGenerator;
import com.legstar.coxb.cob2trans.Cob2TransListener;

/**
 * Uses events produced by the COBOL to Transformers generator to
 * report progress in a Eclipse fashion.
 * 
 * TODO Step descriptions should be localized
 *
 */
public class Cob2TransListenerAdapter implements Cob2TransListener {

    /** Eclipse progress listener.*/
    private IProgressMonitor _progressListener;
    
    /** Instance of the COBOL to Transformers generator.*/
    private Cob2TransGenerator _cob2trans;
    
    /**
     * Construct the adapter.
     * @param cob2trans the COBOL to Transformers generator
     * @param progressListener the Eclipse progress listener
     */
    public Cob2TransListenerAdapter(
            final Cob2TransGenerator cob2trans,
            final IProgressMonitor progressListener) {
        _cob2trans = cob2trans;
        _progressListener = progressListener;
    }
    
    /**
     * Signals a step performed.
     * 
     * @param e the step event object
     */
    public void stepPerformed(final Cob2TransEvent e) {
        if (_progressListener.isCanceled()) {
            _cob2trans.interrupt();
            
        }else {
            if (e.getEventType() == EventType.START) {
                _progressListener.subTask(e.getStepDescription());
            } else {
                _progressListener.worked(1);
            }
        }
        
    }

}

package com.legstar.test.coxb.tcobwvb.bind;

import com.legstar.coxb.CobolContext;
import com.legstar.coxb.ICobolComplexBinding;
import com.legstar.coxb.transform.AbstractHostToJavaTransformer;
import com.legstar.coxb.CobolBindingException;

/**
 * Transforms mainframe data to a CustomerData data object.
 * <p/>
 * This is a typical use of this class:
 * <pre>
 *  CustomerDataHostToJavaTransformer transformer = new CustomerDataHostToJavaTransformer();
 *  CustomerData javaValue = (CustomerData) transformer.transform(hostByteArray);
 * </pre>
 *
 */
public class CustomerDataHostToJavaTransformer extends AbstractHostToJavaTransformer {

    
    /**
     * Create a Host to Java transformer using default COBOL parameters.
     */
    public CustomerDataHostToJavaTransformer() {
        super();
    }
    
    /**
     * Create a Host to Java transformer using a specific COBOL parameters set.
     * @param cobolContext the COBOL parameters set.
     */
    public CustomerDataHostToJavaTransformer(final CobolContext cobolContext) {
        super(cobolContext);
    }

    /**
     * Create a Host to Java transformer using a specific host character set while
     * other COBOL parameters are set by default.
     * @param hostCharset the host character set
     */
    public CustomerDataHostToJavaTransformer(final String hostCharset) {
        super(hostCharset);
    }
    
    /**
     * Binding is statically produced by {@link com.legstar.coxb.gen.CoxbBindingGenerator}.
     * @return the binding corresponding to the host structure type.
     * @throws CobolBindingException if binding cannot be returned
     */
    public ICobolComplexBinding getBinding() throws CobolBindingException {
        return new CustomerDataBinding();
    }
    
}

package com.legstar.pdi;


import com.legstar.coxb.impl.reflect.ReflectBindingException;
import com.legstar.pdi.BindingReflectHelper;

import junit.framework.TestCase;

/**
 * Tests for BindingReflectHelper class.
 *
 */
public class BindingReflectHelperTest extends TestCase {
	
	/**
	 * Test ability to create new JAXB object factories dynamically.
	 */
	public void testNewJaxbObjectFactory() {
		try {
			Object factory = BindingReflectHelper
					.newJaxbObjectFactory("com.legstar.test.coxb.alltypes");
			assertTrue(factory != null);
			assertTrue(factory instanceof com.legstar.test.coxb.alltypes.ObjectFactory);
		} catch (ReflectBindingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test ability to create new JAXB object dynamically.
	 */
	public void testNewJaxbObject() {
		try {
			Object object = BindingReflectHelper.newJaxbObject(
					new com.legstar.test.coxb.alltypes.ObjectFactory(),
					"Dfhcommarea");
			assertTrue(object != null);
			assertTrue(object instanceof com.legstar.test.coxb.alltypes.Dfhcommarea);
		} catch (ReflectBindingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test ability to create new Transformers dynamically.
	 */
	public void testnewTransformers() {
		try {
			Object transformers = BindingReflectHelper.newTransformers(
					"com.legstar.test.coxb.alltypes", "Dfhcommarea");
			assertTrue(transformers != null);
			assertTrue(transformers instanceof com.legstar.test.coxb.alltypes.bind.DfhcommareaTransformers);
		} catch (ReflectBindingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

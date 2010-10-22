package com.legstar.pdi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.legstar.coxb.impl.reflect.ReflectBindingException;
import com.legstar.coxb.transform.IHostTransformers;

/**
 * Utility class to help with JAXB and Binding classes reflection.
 *
 */
public class BindingReflectHelper {

	/**
	 * Utility class. No instantiation.
	 */
	private BindingReflectHelper() {
		
	}

	/**
     * Dynamically create an instance of a java class.
     * This assumes the classes are available on the classpath and returns a
     * new instance.
     * 
     * @param qualifiedClassName the package containing the class
     * @return a java object
     * @throws ReflectBindingException if class not found
     */
    public static Object newObject(
            final String qualifiedClassName) throws ReflectBindingException {

		Class < ? > clazz = null;
		try {
			clazz = Class.forName(qualifiedClassName);
		} catch (ClassNotFoundException cnfe) {
			try {
				ClassLoader contextClassLoader = Thread.currentThread()
						.getContextClassLoader();
				if (contextClassLoader != null) {
					clazz = contextClassLoader
							.loadClass(qualifiedClassName);
				} else {
					throw new ReflectBindingException(qualifiedClassName
							+ " not found. Make sure it is on the classpath");
				}
			} catch (ClassNotFoundException e) {
				throw new ReflectBindingException(e);
			}
		}

		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new ReflectBindingException(e);
		} catch (IllegalAccessException e) {
			throw new ReflectBindingException(e);
		}
    }

	/**
     * Loads a JAXB object factory class using a combination of current and
     * thread class loader.
     * This assumes the JAXB classes are available on the classpath and returns a
     * new instance of of the object factory.
     * 
     * @param packageName the package containing a JAXB Object Factory
     * @return a JAXB Object factory
     * @throws ReflectBindingException if JAXB classes are not found
     */
    public static Object newJaxbObjectFactory(
            final String packageName) throws ReflectBindingException {

		return newObject(toQualifiedClassName(packageName,"ObjectFactory"));
    }
    
    /**
	 * Loads a JAXB object using a JAXB Object factory.
	 * @param jaxbObjectFactory the JAXB Object factory
	 * @param jaxbClassName the JAXB class name
	 * @return a new instance of the JAXB object
	 * @throws ReflectBindingException if instantiation failed
	 */
	public static Object newJaxbObject(final Object jaxbObjectFactory,
			final String jaxbClassName) throws ReflectBindingException {
		try {
			String createName = "create" + jaxbClassName;
			Method creator = jaxbObjectFactory.getClass().getMethod(createName);
			return creator.invoke(jaxbObjectFactory);
		} catch (IllegalAccessException e) {
			throw new ReflectBindingException(e);
		} catch (SecurityException e) {
			throw new ReflectBindingException(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectBindingException(e);
		} catch (IllegalArgumentException e) {
			throw new ReflectBindingException(e);
		} catch (InvocationTargetException e) {
			throw new ReflectBindingException(e);
		}
	}
	
	/**
	 * Create an instance of Transformers for a given JAXB root class name.
	 * Assumes binding classes were generated for this JAXB class.
	 * TODO reuse COXBGEN code for COXB package name and Transformers name.
	 * @param jaxbPackageName the JAXB package name
	 * @param jaxbClassName the JAXB root class name
	 * @return a new instance of Transformers
	 * @throws ReflectBindingException if transformers cannot be created
	 */
	public static IHostTransformers newTransformers(
			final String jaxbPackageName,
			final String jaxbClassName) throws ReflectBindingException {
		String coxbPackageName = (jaxbPackageName == null) ? "bind"
				: jaxbPackageName + ".bind";
		String transformersClassName = jaxbClassName + "Transformers";
		return (IHostTransformers) newObject(toQualifiedClassName(
				coxbPackageName, transformersClassName));
		
	}
	
	/**
	 * Qualifies a class name.
	 * @param packageName the package name, null if none
	 * @param className the class name
	 * @return a qualified class name
	 */
	public static String toQualifiedClassName(final String packageName,
			final String className) {
		return (packageName == null) ? className : packageName + '.'
				+ className;
	}
}

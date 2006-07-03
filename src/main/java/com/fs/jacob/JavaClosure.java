/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

import com.fs.jacob.vpu.JacobVPU;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base class for constructs which rely on a Java method body to represent
 * some aspect of the process.
 */
public abstract class JavaClosure implements Serializable {
  private static final Log __log = LogFactory.getLog(JavaClosure.class);

  public abstract Set getImplementedMethods();

  /**
   * @see Object#toString
   */
  public String toString() {
    StringBuffer buf = new StringBuffer("<JMB:");
    buf.append(getClassName());
    buf.append(">");

    return buf.toString();
  }

  /**
   * Get the unadorned (no package) name of this class.
   *
   * @return name of this class sans package prefix
   */
  protected String getClassName() {
    return getClassName(getClass());
  }

  /**
   * Get the unadorned (no package) name of a class.
   *
   * @param clazz class whose name should be returned
   *
   * @return name of the passed in calss sans package prefix
   */
  protected static String getClassName(Class clazz) {
    String className = clazz.getName();

    return (className.indexOf('.') == -1)
           ? className
           : className.substring(clazz.getPackage().getName().length() + 1);
  }

  protected Object getExtension(Class extensionClass) {
    return JacobVPU.activeJacobThread().getExtension(extensionClass);
  }

  @SuppressWarnings("unchecked")
  protected <T extends Channel> T importChannel(String channelId, Class<T> channelClass) {
    return (T) JacobVPU.activeJacobThread().importChannel(channelId, channelClass);
  }

  /**
   * Instantiation; the Java code <code>instance(new F(x,y,z))</code> is
   * equivalent to <code>F(x,y,z)</code> in the process calculus.
   *
   * @param concretion the concretion of a process template
   */
  protected void instance(Abstraction concretion) {
    JacobVPU.activeJacobThread().instance(concretion);
  }

  /**
   * Get the logger for this class.
   *
   * @return static class-level {@link Log} object
   */
  protected Log log() {
    return __log;
  }

  protected <T extends Channel> T newChannel(Class<T> channelType) throws IllegalArgumentException {
    return newChannel(channelType, null);

  }
  /**
   * Channel creation; the Java code <code>Channel x =
   * newChannel(XChannel.class) ...</code> is equivalent to <code>(new x) ...
   * </code> in the process calculus.
   *
   * @param channelType
   * @param description DOCUMENTME
   *
   * @return
   *
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  protected <T extends Channel> T newChannel(Class<T> channelType, String description)
                        throws IllegalArgumentException {
    return (T) JacobVPU.activeJacobThread().newChannel(channelType, toString(), description);
  }

  /**
   * Object; the Java code "object(x, ML)" is equivalent to <code>x ?
   * ML</code> in the process algebra.
   *
   * @param methodList method list for the communication reduction
   *
   * @see JacobThread#object
   */
  protected <T extends Channel> T object(ML<T> methodList) {
    JacobVPU.activeJacobThread().object(false, methodList);
    return methodList.getChannel();
  }

  protected void object(boolean replication, ML methodList) {
    JacobVPU.activeJacobThread().object(replication, methodList);
  }

  protected void object(boolean replication, ML[] methodLists) {
    JacobVPU.activeJacobThread().object(replication, methodLists);
  }

  protected void object(boolean replication, Set<ML> methodLists) {
    JacobVPU.activeJacobThread().object(replication, methodLists.toArray(new ML[methodLists.size()]));
  }

  protected  <T extends Channel> T replication(ML<T> methodList) {
    JacobVPU.activeJacobThread().object(true, methodList);
    return methodList.getChannel();
  }

  /**
   * Obtain a replicated channel broadcaster.
   * @param channel target channel
   * @return replicated channel broadcaster
   */
  protected <T extends Channel> T replication(T channel) {
    // TODO: we should create a replicated wrapper here. 
    return channel;
  }

  public Method getMethod(Object methodName) {
    Set implementedMethods = getImplementedMethods();
    for (Iterator i = implementedMethods.iterator(); i.hasNext();) {
      Method method = (Method) i.next();

      if (method.getName().equals(methodName))
        return method;
    }

    throw new IllegalArgumentException("No such method \"" + methodName + "\"!");
  }

}

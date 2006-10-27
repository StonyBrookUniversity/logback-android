/**
 * Logback: the generic, reliable, fast and flexible logging framework for Java.
 * 
 * Copyright (C) 2000-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.xml.sax.Locator;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.event.InPlayListener;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;


/**
 * 
 * The ExecutionContext contains the contextual state of a Joran parsing
 * session. {@link Action} objects depend on this context to exchange 
 * and store information.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class InterpretationContext extends ContextAwareBase {
  Stack<Object> objectStack;
  Map<String, Object> objectMap;
  Map<String, String> substitutionMap;
  Interpreter joranInterpreter;
  final List<InPlayListener> listenerList = new ArrayList<InPlayListener>();
  
  public InterpretationContext(Context context, Interpreter joranInterpreter) {
    this.joranInterpreter = joranInterpreter;
    objectStack = new Stack<Object> ();
    objectMap = new HashMap<String, Object>(5);
    substitutionMap = new HashMap<String, String>();
  }
  
  String updateLocationInfo(String msg) {
    Locator locator = joranInterpreter.getLocator();

    if (locator != null) {
      return msg + locator.getLineNumber() + ":" + locator.getColumnNumber();
    } else {
      return msg;
    }
  }
  
  public Locator getLocator() {
    return joranInterpreter.getLocator();
  }

  public Interpreter getJoranInterpreter() {
    return joranInterpreter;
  }

  public Stack<Object> getObjectStack() {
    return objectStack;
  }

  public boolean isEmpty() {
    return objectStack.isEmpty();
  }

  public Object peekObject() {
    return objectStack.peek();
  }

  public void pushObject(Object o) {
    objectStack.push(o);
  }

  public Object popObject() {
    return objectStack.pop();
  }

  public Object getObject(int i) {
    return objectStack.get(i);
  }

  public Map<String, Object> getObjectMap() {
    return objectMap;
  }

  /**
   * Add a property to the properties of this execution context. If the property
   * exists already, it is overwritten.
   */
  public void addProperty(String key, String value) {
    if (key == null || value == null) {
      return;
    }
    // if (substitutionProperties.contains(key)) {
    // LogLog.warn(
    // "key [" + key
    // + "] already contained in the EC properties. Overwriting.");
    // }

    // values with leading or trailing spaces are bad. We remove them now.
    value = value.trim();
    substitutionMap.put(key, value);
  }

  public void addProperties(Properties props) {
    if (props == null) {
      return;
    }
    Iterator i = props.keySet().iterator();
    while (i.hasNext()) {
      String key = (String) i.next();
      addProperty(key, props.getProperty(key));
    }
  }

  public String getSubstitutionProperty(String key) {
    return substitutionMap.get(key);
  }

  public String subst(String value) {
    if (value == null) {
      return null;
    }
    return OptionHelper.substVars(value, substitutionMap, context.getPropertyMap());
  }
  
  public void addInPlayListener(InPlayListener ipl) {
    if(listenerList.contains(ipl)) {
      addWarn("InPlayListener "+ipl+" has been already registered");
    } else {
      listenerList.add(ipl);
    }
  }
  
  public boolean removeInPlayListener(InPlayListener ipl) {
    return listenerList.remove(ipl);
  }
  
  void fireInPlay(SaxEvent event) {
    for(InPlayListener ipl: listenerList) {
      ipl.inPlay(event);
    }
  }
}
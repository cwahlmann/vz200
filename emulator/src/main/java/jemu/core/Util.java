package jemu.core;

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Title:        Bitwise Utilities
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      Bindata Ltd
 * @author Richard Wilson
 * @version 1.0
 */

public class Util {

  /**
   * Undefined (not yet determined) Java Virtual Machine type.
   */
  public static final int JVM_UNDEFINED = 0;

  /**
   * Microsoft Java Virtual Machine type.
   */
  public static final int JVM_MS        = 1;

  /**
   * Netscape Java Virtual Machine type.
   */
  public static final int JVM_NETSCAPE  = 2;

  /**
   * Unknown Java Virtual Machine type.
   */
  public static final int JVM_UNKNOWN   = 3;

  /**
   * The currently determined Java Virtual Machine type.
   */
  public static int jvmType = JVM_UNDEFINED;

  /**
   * Data used to invoke secure methods or constructors on the Java Virtual
   * Machine.
   */
  private static Object[] jvmData;

  /**
   * assertPermission method of com.ms.secutiry.PolicyEngine for MS JVM.
   */
  private static Method assertPermission;

  /**
   * The System Event Queue once determined.
   */
  private static EventQueue systemEventQueue = null;

  /**
   * Compares two Objects for equality. This allows Objects to be compared with
   * null, and will return true if null is compared to null.
   *
   * @param o1 The first Object
   * @param o2 The second Object
   * @return true If both o1 and o2 are null or the equals() method for the
   *              instances returns true
   */
  public static boolean equals(Object o1, Object o2) {
    boolean result;
    if (o1 == null)
      result = o2 == null;
    else
      result = o1.equals(o2);
    return result;
  }

  /**
   * Inserts new elements into an int array.
   *
   * @param source The source array
   * @param start The start index of the elements to insert
   * @param count The number of elements to insert
   * @param value The value for the new elements
   * @return The new array with elements inserted
   */
  public static int[] arrayInsert(int[] source, int start, int count, int value)
  {
    int[] result;
    if (count <= 0) {
      result = new int[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else {
      result = new int[source.length + count];
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      int rem = source.length - start;
      if (rem > 0)
        System.arraycopy(source,start,result,start + count,rem);

      for (int i = start; i < start + count; i++)
        result[i] = value;
    }
    return result;
  }

  /**
   * Deletes elements from an int array.
   *
   * @param source The source array
   * @param start The start index of the first column removed
   * @param count The number of elements to remove
   * @return An int array with the given elements removed
   */
  public static int[] arrayDelete(int[] source, int start, int count) {
    int[] result;
    if (count <= 0) {
      result = new int[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else {
      result = new int[source.length - count];
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      if (start + count < source.length)
        System.arraycopy(source,start + count,result,start,source.length -
          (start + count));
    }
    return result;
  }

  /**
   * Ensures an array of ints is a specified size.
   *
   * @param source The source array to be shrunk or grown
   * @param size The required size of the array
   * @param value A value to be placed in any new additional elements
   * @return An array containing as many of the original values as possible, but
   *         of the required size
   */
  public static int[] ensureArraySize(int[] source, int size, int value) {
    int[] result = source;
    if (result.length < size)
      result = arrayInsert(result,result.length,size - result.length,value);
    else if (result.length > size)
      result = arrayDelete(result,size,result.length - size);
    return result;
  }

  /**
   * Inserts new elements into a double array.
   *
   * @param source The source array
   * @param start The start index of the elements to insert
   * @param count The number of elements to insert
   * @param value The value for the new elements
   * @return The new array with elements inserted
   */
  public static double[] arrayInsert(double[] source, int start, int count,
    double value)
  {
    double[] result;
    if (count <= 0) {
      result = new double[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else {
      result = new double[source.length + count];
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      int rem = source.length - start;
      if (rem > 0)
        System.arraycopy(source,start,result,start + count,rem);

      for (int i = start; i < start + count; i++)
        result[i] = value;
    }
    return result;
  }

  /**
   * Deletes elements from a double array.
   *
   * @param source The source array
   * @param start The start index of the first column removed
   * @param count The number of elements to remove
   * @return An int array with the given elements removed
   */
  public static double[] arrayDelete(double[] source, int start, int count) {
    double[] result;
    if (count <= 0) {
      result = new double[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else {
      result = new double[source.length - count];
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      if (start + count < source.length)
        System.arraycopy(source,start + count,result,start,source.length -
          (start + count));
    }
    return result;
  }

  /**
   * Ensures an array of doubles is a specified size.
   *
   * @param source The source array to be shrunk or grown
   * @param size The required size of the array
   * @param value A value to be placed in any new additional elements
   * @return An array containing as many of the original values as possible, but
   *         of the required size
   */
  public static double[] ensureArraySize(double[] source, int size,
    double value)
  {
    double[] result = source;
    if (result.length < size)
      result = arrayInsert(result,result.length,size - result.length,value);
    else if (result.length > size)
      result = arrayDelete(result,size,result.length - size);
    return result;
  }

  /**
   * Inserts new elements into an Object array.
   *
   * @param source The source array
   * @param start The start index of the elements to insert
   * @param count The number of elements to insert
   * @param value The value for the new elements
   * @return The new array with elements inserted
   */
  public static Object[] arrayInsert(Object[] source, int start, int count,
    Object value)
  {
    Object[] result = source;
    if (count > 0) {
      result = (Object[])Array.newInstance(source.getClass().getComponentType(),
        source.length + count);
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      int rem = source.length - start;
      if (rem > 0)
        System.arraycopy(source,start,result,start + count,rem);

      for (int i = start; i < start + count; i++)
        result[i] = value;
    }
    return result;
  }

  /**
   * Deletes elements from an Object array.
   *
   * @param source The source array
   * @param start The start index of the first column removed
   * @param count The number of elements to remove
   * @return An Object array with the given elements removed
   */
  public static Object[] arrayDelete(Object[] source, int start, int count) {
    Object[] result;
    if (count <= 0) {
      result = new Object[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else {
      result = new Object[source.length - count];
      if (start > 0)
        System.arraycopy(source,0,result,0,start);

      if (start + count < source.length)
        System.arraycopy(source,start + count,result,start,source.length -
          (start + count));
    }
    return result;
  }

  /**
   * Deletes an element from an Object array.
   *
   * @param source The source array
   * @param element The element to be deleted (matched using ==)
   * @return An Object array with the given element removed
   */
  public static Object[] arrayDeleteElement(Object[] source, Object element) {
    for (int i = 0; i < source.length; i++)
      if (source[i] == element)
        return arrayDelete(source, i, 1);
    return source;
  }

  /**
   * Ensures an array of Objects is a specified size.
   *
   * @param source The source array to be shrunk or grown
   * @param size The required size of the array
   * @param value A value to be placed in any new additional elements
   * @return An array containing as many of the original values as possible, but
   *         of the required size
   */
  public static Object[] ensureArraySize(Object[] source, int size,
    Object value)
  {
    Object[] result = source;
    if (result.length < size)
      result = arrayInsert(result,result.length,size - result.length,value);
    else if (result.length > size)
      result = arrayDelete(result,size,result.length - size);
    return result;
  }

  /**
   * Invokes a static method or a method on an instance after requesting
   * priviledges required for an Applet to invoke the method successfully.
   *
   * @param cl The Class containing the method
   * @param methodName The name of the method
   * @param paramTypes The Classes of the parameters for the method
   * @param instance The instance on which to invoke the method, or null for a
   *                 static method
   * @param params The values for the parameters for the method
   * @return The return result for the method
   */
  public static final Object secureMethod(Class cl, String methodName,
    Class[] paramTypes, Object instance, Object[] params) throws Exception
  {
    return secureExecute(cl.getMethod(methodName,paramTypes),instance,params);
  }

  /**
   * Invokes a constructor after requesting priviledges required for an Applet
   * to invoke the constructor successfully.
   *
   * @param cl The Class of the instance to be constructed
   * @param paramTypes The Classes of the parameters for the constructor
   * @param params The values for the parameters for the constructor
   * @return The newly constructed instance
   */
  public static final Object secureConstructor(Class cl, Class[] paramTypes,
    Object[] params) throws Exception
  {
    return secureExecute(cl.getConstructor(paramTypes),null,params);
  }

  /**
   * Invokes a static method, instance method or constructor after requesting
   * priviledges required for an Applet to invoke it successfully.
   *
   * @param routine The Method or Constructor instance
   * @param instance The instance on which to invoke the method, or null for a
   *                 static method or constructor
   * @param params The values for the parameters for the method or constructor
   * @return The return result of the method, or the newly created instance for
   *         a constructor
   */
  private static final Object secureExecute(Object routine,
    Object instance, Object[] params) throws Exception
  {
    // Assert permissions
    switch(determineJVM()) {
      case JVM_MS:
        for (int i = 0; i < jvmData.length; i++)
          assertPermission.invoke(null,new Object[] { jvmData[i] });
        break;

      case JVM_NETSCAPE:
        break;
    }
    try {
      if (routine instanceof Method)
        return ((Method)routine).invoke(instance,params);
      else
        return ((Constructor)routine).newInstance(params);
    } catch (InvocationTargetException i) {
      System.out.println("InvocationTarget Exception in secureExecute");
      Throwable t = i.getTargetException();
      if (t instanceof Exception)
        throw (Exception)t;
      else
        throw i;
    }
  }

  /**
   * Determines the type of Java Virtual Machine on which the Application or
   * Applet is running.
   *
   * @param The type of Java Virtual Machine
   */
  public static int determineJVM() {
    if (jvmType == JVM_UNDEFINED) {
      try {
        if (Class.forName("com.ms.security.PermissionUtils") != null) {
          jvmData = new Object[4];
          Class pi = Class.forName("com.ms.security.PermissionID");
          jvmData[0] = pi.getField("SYSTEM").get(null);
          jvmData[1] = pi.getField("FILEIO").get(null);
          jvmData[2] = pi.getField("UI").get(null);
          jvmData[3] = pi.getField("PROPERTY").get(null);
          Class pe = Class.forName("com.ms.security.PolicyEngine");
          assertPermission = pe.getMethod("assertPermission",
            new Class[] { pi });
          jvmType = JVM_MS;
        }
      } catch (Throwable t) {
        if (!(t instanceof ClassNotFoundException))
          t.printStackTrace();
      }
      if (jvmType == JVM_UNDEFINED) {
        try {
          if (Class.forName("netscape.security.AppletSecurity") != null) {
            jvmType = JVM_NETSCAPE;
          }
        } catch(Throwable t) {
          if (!(t instanceof ClassNotFoundException))
            t.printStackTrace();
        }
      }
      if (jvmType == JVM_UNDEFINED)
        jvmType = JVM_UNKNOWN;
    }
    return jvmType;
  }

  /**
   * Sorts an array of ints using an ascending sort order.
   *
   * @param source The source array
   * @param copy true to make a copy rather than sorting in the source
   * @return The sorted array
   */
  public static int[] sort(int[] source, boolean copy) {
    int[] result;
    if (copy) {
      result = new int[source.length];
      System.arraycopy(source,0,result,0,source.length);
    }
    else
      result = source;
    for (int i = 0; i < result.length - 1; i++)
      for (int j = i + 1; j < result.length; j++)
        if (result[i] > result[j])
          swap(result,i,j);
    return result;
  }

  /**
   * Swaps two entries in an int array.
   *
   * @param source The source int array
   * @param index1 The index of the first element
   * @param index2 The index of the element with which to swap the first element
   */
  public static void swap(int[] source, int index1, int index2) {
    int temp = source[index1];
    source[index1] = source[index2];
    source[index2] = temp;
  }

  /**
   * Converts from a filename to a file URL.
   *
   * @param filename The filename to be converted
   * @return A URL String representing the filename
   */
  public static String fileNameToURLString(String filename) throws Exception {
    String path = ((File)secureConstructor(
      File.class,
      new Class[] { String.class },
      new Object[] { filename })).getAbsolutePath();
    if (File.separatorChar != '/')
      path = path.replace(File.separatorChar, '/');
    if (!path.startsWith("/"))
      path = "/" + path;
    return "file:" + path;
  }

  /**
   * Converts an array of Objects to a Vector.
   *
   * @param items The source array
   * @return A Vector containing the input items
   */
  public static Vector arrayToVector(Object[] items) {
    Vector result = new Vector(items.length);
    for (int i = 0; i < items.length; i++)
      result.addElement(items[i]);
    return result;
  }

  /**
   * Converts two arrays to a Hashtable.
   *
   * @param keys The keys for the Hashtable
   * @param items The elements for each of the keys
   * @return A Hashtable containing the given keys and elements
   */
  public static Hashtable arraysToHashtable(Object[] keys, Object[] items) {
    Hashtable result = new Hashtable(keys.length);
    for (int i = 0; i < keys.length; i++)
      result.put(keys[i],items[i]);
    return result;
  }

  /**
   * Compares two Objects.
   *
   * @param o1 The first Object
   * @param o2 The second Object
   * @return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2
   */
  public static int compare(Object o1, Object o2) {
    return compare(o1,o2,true);
  }

  /**
   * Compares two Objects.
   *
   * @param o1 The first Object
   * @param o2 The second Object
   * @param caseSensitive true for case sensitive, false to ignore case
   * @return -1 if o1 < o2, 0 if o1 == o2, 1 if o1 > o2
   */
  public static int compare(Object o1, Object o2, boolean caseSensitive) {
    int result;
    if (o1 == null)
      result = o2 == null ? 0 : -1;
    else if (o2 == null)
      result = 1;
    else if (o1.equals(o2))
      result = 0;
    else {
      if ((o1 instanceof Number) && (o2 instanceof Number)) {
        double d1 = ((Number)o1).doubleValue();
        double d2 = ((Number)o2).doubleValue();
        if (d1 == d2)
          result = 0;
        else
          result = d1 > d2 ? 1 : -1;
      }
      else if (caseSensitive)
        result = o1.toString().compareTo(o2.toString());
      else
        result = o1.toString().toUpperCase().compareTo(
          o2.toString().toUpperCase());
    }
    return result;
  }

  /**
   * Sorts a Vector of items using the toString() values of the elements in
   * ascending order.
   *
   * @param v The Vector to be sorted
   */
  public static void sort(Vector v) {
    sort(v,true,false);
  }

  /**
   * Sorts a Vector of items using the toString() values of the elements.
   *
   * @param v The Vector to be sorted
   * @param ascending true for ascending order, false for descending order
   * @param caseSensitive true for case sensitive, false to ignore case
   */
  public static void sort(Vector v, boolean ascending, boolean caseSensitive) {
    int mul = ascending ? 1 : -1;
    for (int i = 0; i < v.size() - 1; i++) {
      for (int j = i + 1; j < v.size(); j++)
        if (mul * compare(v.elementAt(i),v.elementAt(j),caseSensitive) > 0)
          swap(v,i,j);
    }
  }

  /**
   * Sorts a Vector of items using the given method to retrieve the values of
   * the elements.
   *
   * @param v The Vector to be sorted
   * @param ascending true for ascending order, false for descending order
   * @param method The method to get the values of the elements for sorting
   * @param caseSensitive true for case sensitive, false to ignore case
   */
  public static void sort(Vector v, boolean ascending, Method method,
    boolean caseSensitive)
    throws InvocationTargetException, IllegalAccessException
  {
    Object[] noParms = new Object[0];
    int mul = ascending ? 1 : -1;
    for (int i = 0; i < v.size() - 1; i++) {
      for (int j = i + 1; j < v.size(); j++)
        if (mul * compare(method.invoke(v.elementAt(i),noParms),
          method.invoke(v.elementAt(j),noParms),caseSensitive) > 0)
        {
          swap(v,i,j);
        }
    }
  }

  /**
   * Converts an Enumeration to a Vector.
   *
   * @param enum The source Enumeration
   * @return A Vector containing all elements in the Enumeration
   */
  public static Vector enumerationToVector(Enumeration en) {
    Vector result = new Vector();
    while (en.hasMoreElements())
      result.addElement(en.nextElement());
    return result;
  }

  /**
   * Swaps two elements in a Vector.
   *
   * @param v The Vector containing the elements
   * @param index1 The index of the first element
   * @param index2 The index of the element to swap with the first element
   */
  public static void swap(Vector v, int index1, int index2) {
    Object o1 = v.elementAt(index1);
    Object o2 = v.elementAt(index2);
    v.setElementAt(o2,index1);
    v.setElementAt(o1,index2);
  }

  /**
   * Returns a blank String ("") if the input String is null, otherwise returns
   * the input String.
   *
   * @param value The source String
   * @return The source String, or a blank String if null
   */
  public static String nullString(String value) {
    return value == null ? "" : value;
  }

  /**
   * Appends the elements from a Vector to another Vector.
   *
   * @param source The Vector to which the elements will be added
   * @param elements The elements to be added to the source Vector
   */
  public static void vectorAddVector(Vector source, Vector elements) {
    if (elements != null) {
      int count = elements.size();
      for (int i = 0; i < count; i++)
        source.addElement(elements.elementAt(i));
    }
  }

  /**
   * Gets a String representation of an Object.
   *
   * @param value The Object for which to get the String representation
   * @return null if value is null, otherwise value.toString()
   */
  public static String asString(Object value) {
    return value == null ? null : value.toString();
  }

  /**
   * Gets a boolean from an Object. If the Object is a String and the Upper Case
   * value of the String is one of "TRUE", "T", "YES", "Y" or "1" then the
   * result will be true. If the Object is a Boolean, the result will be the
   * value of the Boolean, otherwise false is returned.
   *
   * @param source The source Object
   * @return The boolean value
   */
  public static boolean getBoolean(Object source) {
    return getBoolean(source,false);
  }

  /**
   * Gets a boolean from an Object. If the Object is a String and the Upper Case
   * value of the String is one of "TRUE", "T", "YES", "Y" or "1" then the
   * result will be true. If the Object is a Boolean, the result will be the
   * value of the Boolean, otherwise the default value is returned.
   *
   * @param source The source Object
   * @param defValue The default value
   * @return The boolean value
   */
  public static boolean getBoolean(Object source, boolean defValue) {
    if (source instanceof String) {
      source = ((String)source).toUpperCase();
      return "TRUE".equals(source) || "T".equals(source) || "YES".equals(source) || "Y".equals(source) ||
             "1".equals(source);
    }
    else if (source instanceof Boolean)
      return ((Boolean)source).booleanValue();
    return defValue;
  }

  /**
   * Converts an Object to a char.
   *
   * @param source The source Object
   * @return An char value, default char 0
   */
  public static char getChar(Object source) {
    return getChar(source,(char)0);
  }

  /**
   * Converts an Object to a char.
   *
   * @param source The source Object
   * @param defValue The default value if it cannot be converted
   * @return An char value
   */
  public static char getChar(Object source, char defValue) {
    if (source instanceof String) {
      String str = (String)source;
      return str.length() == 0 ? defValue : str.charAt(0);
    }
    else if (source instanceof Character)
      return ((Character)source).charValue();

    return defValue;
  }

  /**
   * Converts an Object to an int.
   *
   * @param source The source Object
   * @return An int value, default zero
   */
  public static int getInt(Object source) {
    return getInt(source,0);
  }

  /**
   * Converts an Object to an int.
   *
   * @param source The source Object
   * @param defValue The default value if it cannot be converted
   * @return An int value
   */
  public static int getInt(Object source, int defValue) {
    if (source instanceof String) {
      String str = (String)source;
      try {
        if (str.length() > 0 && str.charAt(0) == '#')
          return hexValue(str.substring(1));

        return Integer.parseInt(str);
      } catch (Exception e) { }
    }
    else if (source instanceof Number)
      return ((Number)source).intValue();

    return defValue;
  }

  /**
   * Converts an Object to a double.
   *
   * @param source The source Object
   * @return A double value
   */
  public static double getDouble(Object source) {
    return getDouble(source,0);
  }

  /**
   * Converts an Object to a double.
   *
   * @param source The source Object
   * @param defValue The default value if it cannot be converted
   * @return A double value
   */
  public static double getDouble(Object source, double defValue) {
    if (source instanceof String)
      try {
        return Double.valueOf((String)source).doubleValue();
      } catch (Exception e) {
        return defValue;
      }
    else if (source instanceof Number)
      return ((Number)source).doubleValue();
    else
      return defValue;
  }

  /**
   * Converts an Object to a long.
   *
   * @param source The source Object
   * @return A long value, default zero
   */
  public static long getLong(Object source) {
    return getLong(source,0);
  }

  /**
   * Converts an Object to a long.
   *
   * @param source The source Object
   * @param defValue The default value if it cannot be converted
   * @return A long value
   */
  public static long getLong(Object source, long defValue) {
    if (source instanceof String) {
      try {
        return Long.parseLong((String)source);
      } catch (Exception e) { }
    }
    else if (source instanceof Number)
      return ((Number)source).longValue();

    return defValue;
  }

  /**
   * Converts a Hexadecimal character to a byte value.
   *
   * @param value The Hexadecimal character to convert
   * @return The byte value of the character
   */
  public static byte hexValue(char value) {
    if (value >= 'a')
      return (byte)(value - 'a' + 10);
    else if (value > '9')
      return (byte)(value - 'A' + 10);
    else
      return (byte)(value - '0');
  }

  /**
   * Converts a hexadecimal String to an int.
   *
   * @param source The source string
   * @return A hexadecimal integer
   */
  public static int hexValue(String source) throws Exception {
    int result = 0;
    source = source.trim();
    for (int i = 0; i < source.length(); i++) {
      byte val = hexValue(source.charAt(i));
      if (val < 0 || val > 15)
        throw new Exception("Illegal hex character in " + source);
      result = (result << 4) + val;
    }
    return result;
  }

  /**
   * Creates directories. This provides the same functionality as the
   * mkdirs() method in File, but works on all VMs.
   *
   * @param path The path to create
   */
  public static void mkdirs(String path) throws Exception {
    path = path.replace('\\','/');
    if (!path.endsWith("/"))
      path += "/";
    int index = 0;
    while ((index = path.indexOf('/',index)) != -1) {
      File file = new File(path.substring(0,++index));
      boolean exists = ((Boolean)secureMethod(File.class,"exists",new Class[0],
        file,new Object[0])).booleanValue();
      if (!exists)
        secureMethod(File.class,"mkdir",new Class[0],file,new Object[0]);
      boolean directory = ((Boolean)secureMethod(File.class,"isDirectory",
        new Class[0],file,new Object[0])).booleanValue();
      if (!directory)
        throw new Exception("File \"" + file.getPath() +
          "\" is not a directory");
    }
  }

  /**
   * Finds a Class given a default package and class name.
   *
   * @param packageName The default Package
   * @param className The class name
   * @return The Class, or null if not found
   */
  public static Class findClass(String packageName, String className) {
    if (className.indexOf('.') == -1)
      className = packageName + "." + className;
    Class result;
    try {
      result = Class.forName(className);
    } catch(Exception e) {
      result = null;
    }
    return result;
  }

  /**
   * Creates a new instance of a Class given a default package and class name.
   *
   * @param packageName The default Package
   * @param className The class name
   * @return An instance of the class, or null if not found
   */
  public static Object getClassInstance(String packageName, String className)
    throws Exception
  {
    Class cl = findClass(packageName,className);
    return cl != null ? cl.newInstance() : null;
  }

  /**
   * Does a Hexadecimal/ASCII dump of a buffer.
   *
   * @param buffer The buffer to dump
   */
  public static String dumpBytes(byte[] buffer) {
    return dumpBytes(buffer,0,buffer.length,true,true,true);
  }

  /**
   * Does a Hexadecimal/ASCII dump of a buffer. The format will be
   * aaaaaaaa: bb bb bb bb bb bb bb bb bb bb bb bb bb bb bb bb cccccccccccccccc
   * where aaaaaaaa is the address offset of the data, bb represents a byte of
   * the data, and c is the ASCII character representing the data. ASCII values
   * below 0x20 and above 0x7e are displayed as the '.' character.
   *
   * @param buffer The buffer to dump
   * @param offset The start offset of the data in the buffer
   * @param length The length of the data to dump
   * @param showAddr true to include the address
   * @param showChars true to dump ASCII characters
   * @param lineFeed true to add a line feed
   * @return The dump String
   */
  public static String dumpBytes(byte[] buffer, int offset, int length,
    boolean showAddr, boolean showChars, boolean lineFeed)
  {
    length += offset;
    StringBuffer buff = new StringBuffer(80 * (length + 15) / 16);
    for (int i = offset; i < length; i += 16) {
      String end = "";
      if (showAddr)
        buff.append(hex(i) + ": ");
      int j = 0;
      for (; j < 16 && i + j < length; j++) {
        byte data = buffer[i + j];
        buff.append(hex(data) + " ");
        end += data >= ' ' && data < 127 ? (char)data : '.';
      }
      for (;j < 16; j++)
        buff.append("   ");
      if (showChars)
        buff.append(end);
      if (lineFeed)
        buff.append("\n");
    }
    return buff.toString();
  }

  protected static final String HEX_CHARS = "0123456789ABCDEF";

  /**
   * Converts a byte to a Hexadecimal String.
   *
   * @param value The byte to convert
   * @return The Hexadecimal String
   */
  public static String hex(byte value) {
    return "" + HEX_CHARS.charAt((value & 0xF0) >> 4) + HEX_CHARS.charAt(value & 0x0F);
  }

  /**
   * Converts a short to a Hexadecimal String
   *
   * @param value The short to convert
   * @return The Hexadecimal String
   */
  public static String hex(short value) {
    return hex((byte)(value >> 8)) + hex((byte)value);
  }

  /**
   * Converts an int to a Hexadecimal String
   *
   * @param value The int to convert
   * @return The Hexadecimal String
   */
  public static String hex(int value) {
    return hex((short)(value >> 16)) + hex((short)value);
  }

}
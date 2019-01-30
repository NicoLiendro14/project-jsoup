package org.jsoup.nodes;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import org.jsoup.SerializationException;
import org.jsoup.helper.Validate;



public class Attribute
  implements Map.Entry<String, String>, Cloneable
{
  private static final String[] booleanAttributes = { "allowfullscreen", "async", "autofocus", "checked", "compact", "declare", "default", "defer", "disabled", "formnovalidate", "hidden", "inert", "ismap", "itemscope", "multiple", "muted", "nohref", "noresize", "noshade", "novalidate", "nowrap", "open", "readonly", "required", "reversed", "seamless", "selected", "sortable", "truespeed", "typemustmatch" };
  


  private String key;
  


  private String val;
  


  Attributes parent;
  


  public Attribute(String key, String value)
  {
    this(key, value, null);
  }
  





  public Attribute(String key, String val, Attributes parent)
  {
    Validate.notNull(key);
    this.key = key.trim();
    Validate.notEmpty(key);
    this.val = val;
    this.parent = parent;
  }
  



  public String getKey()
  {
    return key;
  }
  



  public void setKey(String key)
  {
    Validate.notNull(key);
    key = key.trim();
    Validate.notEmpty(key);
    if (parent != null) {
      int i = parent.indexOfKey(this.key);
      if (i != -1)
        parent.keys[i] = key;
    }
    this.key = key;
  }
  



  public String getValue()
  {
    return val;
  }
  



  public String setValue(String val)
  {
    String oldVal = parent.get(key);
    if (parent != null) {
      int i = parent.indexOfKey(key);
      if (i != -1)
        parent.vals[i] = val;
    }
    this.val = val;
    return oldVal;
  }
  



  public String html()
  {
    StringBuilder accum = new StringBuilder();
    try
    {
      html(accum, new Document("").outputSettings());
    } catch (IOException exception) {
      throw new SerializationException(exception);
    }
    return accum.toString();
  }
  
  protected static void html(String key, String val, Appendable accum, Document.OutputSettings out) throws IOException {
    accum.append(key);
    if (!shouldCollapseAttribute(key, val, out)) {
      accum.append("=\"");
      Entities.escape(accum, Attributes.checkNotNull(val), out, true, false, false);
      accum.append('"');
    }
  }
  
  protected void html(Appendable accum, Document.OutputSettings out) throws IOException {
    html(key, val, accum, out);
  }
  




  public String toString()
  {
    return html();
  }
  





  public static Attribute createFromEncoded(String unencodedKey, String encodedValue)
  {
    String value = Entities.unescape(encodedValue, true);
    return new Attribute(unencodedKey, value, null);
  }
  
  protected boolean isDataAttribute() {
    return isDataAttribute(key);
  }
  
  protected static boolean isDataAttribute(String key) {
    return (key.startsWith("data-")) && (key.length() > "data-".length());
  }
  





  protected final boolean shouldCollapseAttribute(Document.OutputSettings out)
  {
    return shouldCollapseAttribute(key, val, out);
  }
  
  protected static boolean shouldCollapseAttribute(String key, String val, Document.OutputSettings out) {
    return 
      (out.syntax() == Document.OutputSettings.Syntax.html) && ((val == null) || (
      (("".equals(val)) || (val.equalsIgnoreCase(key))) && (isBooleanAttribute(key))));
  }
  
  /**
   * @deprecated
   */
  protected boolean isBooleanAttribute() {
    return (Arrays.binarySearch(booleanAttributes, key) >= 0) || (val == null);
  }
  


  protected static boolean isBooleanAttribute(String key)
  {
    return Arrays.binarySearch(booleanAttributes, key) >= 0;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) return false;
    Attribute attribute = (Attribute)o;
    if (key != null ? !key.equals(key) : key != null) return false;
    return val == null ? true : val != null ? val.equals(val) : false;
  }
  
  public int hashCode()
  {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (val != null ? val.hashCode() : 0);
    return result;
  }
  
  public Attribute clone()
  {
    try {
      return (Attribute)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}

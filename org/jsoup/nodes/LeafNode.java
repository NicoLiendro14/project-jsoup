package org.jsoup.nodes;

import java.util.Collections;
import java.util.List;
import org.jsoup.helper.Validate;

abstract class LeafNode extends Node
{
  private static final List<Node> EmptyNodes = ;
  Object value;
  
  LeafNode() {}
  
  protected final boolean hasAttributes() { return value instanceof Attributes; }
  

  public final Attributes attributes()
  {
    ensureAttributes();
    return (Attributes)value;
  }
  
  private void ensureAttributes() {
    if (!hasAttributes()) {
      Object coreValue = value;
      Attributes attributes = new Attributes();
      value = attributes;
      if (coreValue != null)
        attributes.put(nodeName(), (String)coreValue);
    }
  }
  
  String coreValue() {
    return attr(nodeName());
  }
  
  void coreValue(String value) {
    attr(nodeName(), value);
  }
  
  public String attr(String key)
  {
    Validate.notNull(key);
    if (!hasAttributes()) {
      return key.equals(nodeName()) ? (String)value : "";
    }
    return super.attr(key);
  }
  
  public Node attr(String key, String value)
  {
    if ((!hasAttributes()) && (key.equals(nodeName()))) {
      this.value = value;
    } else {
      ensureAttributes();
      super.attr(key, value);
    }
    return this;
  }
  
  public boolean hasAttr(String key)
  {
    ensureAttributes();
    return super.hasAttr(key);
  }
  
  public Node removeAttr(String key)
  {
    ensureAttributes();
    return super.removeAttr(key);
  }
  
  public String absUrl(String key)
  {
    ensureAttributes();
    return super.absUrl(key);
  }
  
  public String baseUri()
  {
    return hasParent() ? parent().baseUri() : "";
  }
  


  protected void doSetBaseUri(String baseUri) {}
  

  public int childNodeSize()
  {
    return 0;
  }
  
  protected List<Node> ensureChildNodes()
  {
    return EmptyNodes;
  }
}

package org.jsoup.nodes;

import java.io.IOException;
import org.jsoup.SerializationException;
import org.jsoup.helper.Validate;








public class XmlDeclaration
  extends LeafNode
{
  private final boolean isProcessingInstruction;
  
  public XmlDeclaration(String name, boolean isProcessingInstruction)
  {
    Validate.notNull(name);
    value = name;
    this.isProcessingInstruction = isProcessingInstruction;
  }
  




  /**
   * @deprecated
   */
  public XmlDeclaration(String name, String baseUri, boolean isProcessingInstruction)
  {
    this(name, isProcessingInstruction);
  }
  
  public String nodeName() {
    return "#declaration";
  }
  



  public String name()
  {
    return coreValue();
  }
  



  public String getWholeDeclaration()
  {
    StringBuilder sb = new StringBuilder();
    try {
      getWholeDeclaration(sb, new Document.OutputSettings());
    } catch (IOException e) {
      throw new SerializationException(e);
    }
    return sb.toString().trim();
  }
  
  private void getWholeDeclaration(Appendable accum, Document.OutputSettings out) throws IOException {
    for (Attribute attribute : attributes()) {
      if (!attribute.getKey().equals(nodeName())) {
        accum.append(' ');
        attribute.html(accum, out);
      }
    }
  }
  

  void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out)
    throws IOException
  {
    accum.append("<").append(isProcessingInstruction ? "!" : "?").append(coreValue());
    getWholeDeclaration(accum, out);
    accum
      .append(isProcessingInstruction ? "!" : "?")
      .append(">");
  }
  

  void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {}
  
  public String toString()
  {
    return outerHtml();
  }
}

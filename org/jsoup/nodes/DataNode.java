package org.jsoup.nodes;

import java.io.IOException;







public class DataNode
  extends LeafNode
{
  public DataNode(String data)
  {
    value = data;
  }
  


  /**
   * @deprecated
   */
  public DataNode(String data, String baseUri)
  {
    this(data);
  }
  
  public String nodeName() {
    return "#data";
  }
  



  public String getWholeData()
  {
    return coreValue();
  }
  




  public DataNode setWholeData(String data)
  {
    coreValue(data);
    return this;
  }
  
  void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
    accum.append(getWholeData());
  }
  
  void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {}
  
  public String toString()
  {
    return outerHtml();
  }
  





  public static DataNode createFromEncoded(String encodedData, String baseUri)
  {
    String data = Entities.unescape(encodedData);
    return new DataNode(data);
  }
}

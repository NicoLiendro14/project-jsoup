package org.jsoup.select;

import org.jsoup.nodes.Node;




















public abstract interface NodeFilter
{
  public abstract FilterResult head(Node paramNode, int paramInt);
  
  public abstract FilterResult tail(Node paramNode, int paramInt);
  
  public static enum FilterResult
  {
    CONTINUE, 
    
    SKIP_CHILDREN, 
    
    SKIP_ENTIRELY, 
    
    REMOVE, 
    
    STOP;
    
    private FilterResult() {}
  }
}

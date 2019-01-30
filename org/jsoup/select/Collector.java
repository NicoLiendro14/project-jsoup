package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;















public class Collector
{
  private Collector() {}
  
  public static Elements collect(Evaluator eval, Element root)
  {
    Elements elements = new Elements();
    NodeTraversor.traverse(new Accumulator(root, elements, eval), root);
    return elements;
  }
  
  private static class Accumulator implements NodeVisitor {
    private final Element root;
    private final Elements elements;
    private final Evaluator eval;
    
    Accumulator(Element root, Elements elements, Evaluator eval) {
      this.root = root;
      this.elements = elements;
      this.eval = eval;
    }
    
    public void head(Node node, int depth) {
      if ((node instanceof Element)) {
        Element el = (Element)node;
        if (eval.matches(root, el)) {
          elements.add(el);
        }
      }
    }
    
    public void tail(Node node, int depth) {}
  }
  
  public static Element findFirst(Evaluator eval, Element root)
  {
    FirstFinder finder = new FirstFinder(root, eval);
    NodeTraversor.filter(finder, root);
    return match;
  }
  
  private static class FirstFinder implements NodeFilter {
    private final Element root;
    private Element match = null;
    private final Evaluator eval;
    
    FirstFinder(Element root, Evaluator eval) {
      this.root = root;
      this.eval = eval;
    }
    
    public NodeFilter.FilterResult head(Node node, int depth)
    {
      if ((node instanceof Element)) {
        Element el = (Element)node;
        if (eval.matches(root, el)) {
          match = el;
          return NodeFilter.FilterResult.STOP;
        }
      }
      return NodeFilter.FilterResult.CONTINUE;
    }
    
    public NodeFilter.FilterResult tail(Node node, int depth)
    {
      return NodeFilter.FilterResult.CONTINUE;
    }
  }
}

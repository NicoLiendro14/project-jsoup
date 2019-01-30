package ej2;

import org.jsoup.select.Elements;

public class clsScrap
{
  private Elements titulo;
  private Elements autor;
  private Elements puntos;
  private Elements links;
  private org.jsoup.nodes.Document doc;
  
  public clsScrap(Elements t, Elements a, Elements p, Elements l, String url) {
    titulo = t;
    autor = a;
    puntos = p;
    links = l;
    doc = getHtmlDocument(url);
  }
  
  public void buscaEntradas() {
    Elements entradas = doc.select("ul.top-week");
    links = entradas.select("a[href]");
    puntos = entradas.select("div[class]");
    titulo = entradas.select("h3");
    autor = entradas.select("p[class]");
  }
  
  public void buscaEntradas(String top) { Elements entradas = doc.select(top);
    links = entradas.select("a[href]");
    puntos = entradas.select("div[class]");
    titulo = entradas.select("h3");
    autor = entradas.select("p[class]");
  }
  
  public Elements getLink() { return links; }
  
  public Elements getTitulo() {
    return titulo;
  }
  
  public Elements getPuntos() { return puntos; }
  
  public Elements getAutor() {
    return autor;
  }
  
  public static org.jsoup.nodes.Document getHtmlDocument(String url)
  {
    org.jsoup.nodes.Document doc = null;
    try {
      doc = org.jsoup.Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).get();
    } catch (java.io.IOException ex) {
      System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
    }
    return doc;
  }
}

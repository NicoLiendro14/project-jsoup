package ej2;

import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.select.Elements;

public class ListLinks
{
  public static final String url = "https://www.taringa.net/";
  
  public ListLinks() {}
  
  public static void main(String[] args) throws IOException
  {
    clsVentana v1 = new clsVentana();
    v1.setVisible(true);
    
    if (getStatusConnectionCode("https://www.taringa.net/") == 200) {
      Elements a1 = null;Elements a2 = null;Elements a3 = null;Elements a4 = null;
      clsScrap localClsScrap = new clsScrap(a1, a2, a3, a4, "https://www.taringa.net/");
    }
    else
    {
      System.out.println("El Status Code no es OK es: " + getStatusConnectionCode("https://www.taringa.net/"));
    }
  }
  

  public static int getStatusConnectionCode(String url)
  {
    org.jsoup.helper.HttpConnection.Response response = null;
    try
    {
      response = (org.jsoup.helper.HttpConnection.Response)org.jsoup.Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
    } catch (IOException ex) {
      System.out.println("Excepción al obtener el Status Code: " + ex.getMessage());
    }
    return response.statusCode();
  }
}

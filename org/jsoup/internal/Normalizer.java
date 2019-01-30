package org.jsoup.internal;

import java.util.Locale;

public final class Normalizer
{
  public Normalizer() {}
  
  public static String lowerCase(String input)
  {
    return input != null ? input.toLowerCase(Locale.ENGLISH) : "";
  }
  
  public static String normalize(String input) {
    return lowerCase(input).trim();
  }
}

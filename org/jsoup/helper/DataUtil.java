package org.jsoup.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.UncheckedIOException;
import org.jsoup.internal.ConstrainableInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;



public final class DataUtil
{
  private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)");
  static final String defaultCharset = "UTF-8";
  private static final int firstReadBufferSize = 5120;
  static final int bufferSize = 32768;
  private static final char[] mimeBoundaryChars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    .toCharArray();
  

  static final int boundaryLength = 32;
  


  private DataUtil() {}
  


  public static Document load(File in, String charsetName, String baseUri)
    throws IOException
  {
    return parseInputStream(new FileInputStream(in), charsetName, baseUri, Parser.htmlParser());
  }
  






  public static Document load(InputStream in, String charsetName, String baseUri)
    throws IOException
  {
    return parseInputStream(in, charsetName, baseUri, Parser.htmlParser());
  }
  







  public static Document load(InputStream in, String charsetName, String baseUri, Parser parser)
    throws IOException
  {
    return parseInputStream(in, charsetName, baseUri, parser);
  }
  




  static void crossStreams(InputStream in, OutputStream out)
    throws IOException
  {
    byte[] buffer = new byte[32768];
    int len;
    while ((len = in.read(buffer)) != -1) {
      out.write(buffer, 0, len);
    }
  }
  
  static Document parseInputStream(InputStream input, String charsetName, String baseUri, Parser parser) throws IOException {
    if (input == null)
      return new Document(baseUri);
    input = ConstrainableInputStream.wrap(input, 32768, 0);
    
    Document doc = null;
    boolean fullyRead = false;
    

    input.mark(32768);
    ByteBuffer firstBytes = readToByteBuffer(input, 5119);
    fullyRead = input.read() == -1;
    input.reset();
    

    BomCharset bomCharset = detectCharsetFromBom(firstBytes);
    if (bomCharset != null) {
      charsetName = charset;
    }
    if (charsetName == null) {
      String docData = Charset.forName("UTF-8").decode(firstBytes).toString();
      doc = parser.parseInput(docData, baseUri);
      

      Elements metaElements = doc.select("meta[http-equiv=content-type], meta[charset]");
      String foundCharset = null;
      for (Element meta : metaElements) {
        if (meta.hasAttr("http-equiv"))
          foundCharset = getCharsetFromContentType(meta.attr("content"));
        if ((foundCharset == null) && (meta.hasAttr("charset")))
          foundCharset = meta.attr("charset");
        if (foundCharset != null) {
          break;
        }
      }
      
      if ((foundCharset == null) && (doc.childNodeSize() > 0) && ((doc.childNode(0) instanceof XmlDeclaration))) {
        XmlDeclaration prolog = (XmlDeclaration)doc.childNode(0);
        if (prolog.name().equals("xml"))
          foundCharset = prolog.attr("encoding");
      }
      foundCharset = validateCharset(foundCharset);
      if ((foundCharset != null) && (!foundCharset.equalsIgnoreCase("UTF-8"))) {
        foundCharset = foundCharset.trim().replaceAll("[\"']", "");
        charsetName = foundCharset;
        doc = null;
      } else if (!fullyRead) {
        doc = null;
      }
    } else {
      Validate.notEmpty(charsetName, "Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
    }
    if (doc == null) {
      if (charsetName == null)
        charsetName = "UTF-8";
      BufferedReader reader = new BufferedReader(new InputStreamReader(input, charsetName), 32768);
      if ((bomCharset != null) && (offset))
        reader.skip(1L);
      try {
        doc = parser.parseInput(reader, baseUri);
      }
      catch (UncheckedIOException e) {
        throw e.ioException();
      }
      doc.outputSettings().charset(charsetName);
    }
    input.close();
    return doc;
  }
  






  public static ByteBuffer readToByteBuffer(InputStream inStream, int maxSize)
    throws IOException
  {
    Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");
    ConstrainableInputStream input = ConstrainableInputStream.wrap(inStream, 32768, maxSize);
    return input.readToByteBuffer(maxSize);
  }
  
  static ByteBuffer readToByteBuffer(InputStream inStream) throws IOException {
    return readToByteBuffer(inStream, 0);
  }
  
  static ByteBuffer readFileToByteBuffer(File file) throws IOException {
    RandomAccessFile randomAccessFile = null;
    try {
      randomAccessFile = new RandomAccessFile(file, "r");
      byte[] bytes = new byte[(int)randomAccessFile.length()];
      randomAccessFile.readFully(bytes);
      return ByteBuffer.wrap(bytes);
    } finally {
      if (randomAccessFile != null)
        randomAccessFile.close();
    }
  }
  
  static ByteBuffer emptyByteBuffer() {
    return ByteBuffer.allocate(0);
  }
  





  static String getCharsetFromContentType(String contentType)
  {
    if (contentType == null) return null;
    Matcher m = charsetPattern.matcher(contentType);
    if (m.find()) {
      String charset = m.group(1).trim();
      charset = charset.replace("charset=", "");
      return validateCharset(charset);
    }
    return null;
  }
  
  private static String validateCharset(String cs) {
    if ((cs == null) || (cs.length() == 0)) return null;
    cs = cs.trim().replaceAll("[\"']", "");
    try {
      if (Charset.isSupported(cs)) return cs;
      cs = cs.toUpperCase(Locale.ENGLISH);
      if (Charset.isSupported(cs)) { return cs;
      }
    }
    catch (IllegalCharsetNameException localIllegalCharsetNameException) {}
    return null;
  }
  


  static String mimeBoundary()
  {
    StringBuilder mime = new StringBuilder(32);
    Random rand = new Random();
    for (int i = 0; i < 32; i++) {
      mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
    }
    return mime.toString();
  }
  
  private static BomCharset detectCharsetFromBom(ByteBuffer byteData) {
    Buffer buffer = byteData;
    buffer.mark();
    byte[] bom = new byte[4];
    if (byteData.remaining() >= bom.length) {
      byteData.get(bom);
      buffer.rewind();
    }
    if (((bom[0] == 0) && (bom[1] == 0) && (bom[2] == -2) && (bom[3] == -1)) || ((bom[0] == -1) && (bom[1] == -2) && (bom[2] == 0) && (bom[3] == 0)))
    {
      return new BomCharset("UTF-32", false); }
    if (((bom[0] == -2) && (bom[1] == -1)) || ((bom[0] == -1) && (bom[1] == -2)))
    {
      return new BomCharset("UTF-16", false); }
    if ((bom[0] == -17) && (bom[1] == -69) && (bom[2] == -65)) {
      return new BomCharset("UTF-8", true);
    }
    
    return null;
  }
  
  private static class BomCharset {
    private final String charset;
    private final boolean offset;
    
    public BomCharset(String charset, boolean offset) {
      this.charset = charset;
      this.offset = offset;
    }
  }
}

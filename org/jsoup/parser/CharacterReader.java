package org.jsoup.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;
import org.jsoup.UncheckedIOException;
import org.jsoup.helper.Validate;





public final class CharacterReader
{
  static final char EOF = '￿';
  private static final int maxStringCacheLen = 12;
  static final int maxBufferLen = 32768;
  private static final int readAheadLimit = 24576;
  private final char[] charBuf;
  private final Reader reader;
  private int bufLength;
  private int bufSplitPoint;
  private int bufPos;
  private int readerPos;
  private int bufMark;
  private final String[] stringCache = new String['Ȁ'];
  
  public CharacterReader(Reader input, int sz) {
    Validate.notNull(input);
    Validate.isTrue(input.markSupported());
    reader = input;
    charBuf = new char[sz > 32768 ? 32768 : sz];
    bufferUp();
  }
  
  public CharacterReader(Reader input) {
    this(input, 32768);
  }
  
  public CharacterReader(String input) {
    this(new StringReader(input), input.length());
  }
  
  private void bufferUp() {
    if (bufPos < bufSplitPoint) {
      return;
    }
    try {
      reader.skip(bufPos);
      reader.mark(32768);
      int read = reader.read(charBuf);
      reader.reset();
      if (read != -1) {
        bufLength = read;
        readerPos += bufPos;
        bufPos = 0;
        bufMark = 0;
        bufSplitPoint = (bufLength > 24576 ? 24576 : bufLength);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
  



  public int pos()
  {
    return readerPos + bufPos;
  }
  



  public boolean isEmpty()
  {
    bufferUp();
    return bufPos >= bufLength;
  }
  
  private boolean isEmptyNoBufferUp() {
    return bufPos >= bufLength;
  }
  



  public char current()
  {
    bufferUp();
    return isEmptyNoBufferUp() ? 65535 : charBuf[bufPos];
  }
  
  char consume() {
    bufferUp();
    char val = isEmptyNoBufferUp() ? 65535 : charBuf[bufPos];
    bufPos += 1;
    return val;
  }
  
  void unconsume() {
    bufPos -= 1;
  }
  


  public void advance()
  {
    bufPos += 1;
  }
  
  void mark() {
    bufMark = bufPos;
  }
  
  void rewindToMark() {
    bufPos = bufMark;
  }
  





  int nextIndexOf(char c)
  {
    bufferUp();
    for (int i = bufPos; i < bufLength; i++) {
      if (c == charBuf[i])
        return i - bufPos;
    }
    return -1;
  }
  





  int nextIndexOf(CharSequence seq)
  {
    bufferUp();
    
    char startChar = seq.charAt(0);
    for (int offset = bufPos; offset < bufLength; offset++)
    {
      if (startChar != charBuf[offset])
        do { offset++; } while ((offset < bufLength) && (startChar != charBuf[offset]));
      int i = offset + 1;
      int last = i + seq.length() - 1;
      if ((offset < bufLength) && (last <= bufLength)) {
        for (int j = 1; (i < last) && (seq.charAt(j) == charBuf[i]); j++) i++;
        if (i == last)
          return offset - bufPos;
      }
    }
    return -1;
  }
  




  public String consumeTo(char c)
  {
    int offset = nextIndexOf(c);
    if (offset != -1) {
      String consumed = cacheString(charBuf, stringCache, bufPos, offset);
      bufPos += offset;
      return consumed;
    }
    return consumeToEnd();
  }
  
  String consumeTo(String seq)
  {
    int offset = nextIndexOf(seq);
    if (offset != -1) {
      String consumed = cacheString(charBuf, stringCache, bufPos, offset);
      bufPos += offset;
      return consumed;
    }
    return consumeToEnd();
  }
  





  public String consumeToAny(char... chars)
  {
    bufferUp();
    int start = bufPos;
    int remaining = bufLength;
    char[] val = charBuf;
    
    while (bufPos < remaining) {
      for (char c : chars) {
        if (val[bufPos] == c)
          break label87;
      }
      bufPos += 1;
    }
    label87:
    return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos - start) : "";
  }
  
  String consumeToAnySorted(char... chars) {
    bufferUp();
    int start = bufPos;
    int remaining = bufLength;
    char[] val = charBuf;
    
    while ((bufPos < remaining) && 
      (Arrays.binarySearch(chars, val[bufPos]) < 0))
    {
      bufPos += 1;
    }
    
    return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos - start) : "";
  }
  
  String consumeData()
  {
    bufferUp();
    int start = bufPos;
    int remaining = bufLength;
    char[] val = charBuf;
    
    while (bufPos < remaining) {
      char c = val[bufPos];
      if ((c == '&') || (c == '<') || (c == 0))
        break;
      bufPos += 1;
    }
    
    return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos - start) : "";
  }
  
  String consumeTagName()
  {
    bufferUp();
    int start = bufPos;
    int remaining = bufLength;
    char[] val = charBuf;
    
    while (bufPos < remaining) {
      char c = val[bufPos];
      if ((c == '\t') || (c == '\n') || (c == '\r') || (c == '\f') || (c == ' ') || (c == '/') || (c == '>') || (c == 0))
        break;
      bufPos += 1;
    }
    
    return bufPos > start ? cacheString(charBuf, stringCache, start, bufPos - start) : "";
  }
  
  String consumeToEnd() {
    bufferUp();
    String data = cacheString(charBuf, stringCache, bufPos, bufLength - bufPos);
    bufPos = bufLength;
    return data;
  }
  
  String consumeLetterSequence() {
    bufferUp();
    int start = bufPos;
    while (bufPos < bufLength) {
      char c = charBuf[bufPos];
      if (((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z')) && (!Character.isLetter(c))) break;
      bufPos += 1;
    }
    


    return cacheString(charBuf, stringCache, start, bufPos - start);
  }
  
  String consumeLetterThenDigitSequence() {
    bufferUp();
    int start = bufPos;
    while (bufPos < bufLength) {
      char c = charBuf[bufPos];
      if (((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z')) && (!Character.isLetter(c))) break;
      bufPos += 1;
    }
    

    while (!isEmptyNoBufferUp()) {
      char c = charBuf[bufPos];
      if ((c < '0') || (c > '9')) break;
      bufPos += 1;
    }
    


    return cacheString(charBuf, stringCache, start, bufPos - start);
  }
  
  String consumeHexSequence() {
    bufferUp();
    int start = bufPos;
    while (bufPos < bufLength) {
      char c = charBuf[bufPos];
      if (((c < '0') || (c > '9')) && ((c < 'A') || (c > 'F')) && ((c < 'a') || (c > 'f'))) break;
      bufPos += 1;
    }
    

    return cacheString(charBuf, stringCache, start, bufPos - start);
  }
  
  String consumeDigitSequence() {
    bufferUp();
    int start = bufPos;
    while (bufPos < bufLength) {
      char c = charBuf[bufPos];
      if ((c < '0') || (c > '9')) break;
      bufPos += 1;
    }
    

    return cacheString(charBuf, stringCache, start, bufPos - start);
  }
  
  boolean matches(char c) {
    return (!isEmpty()) && (charBuf[bufPos] == c);
  }
  
  boolean matches(String seq)
  {
    bufferUp();
    int scanLength = seq.length();
    if (scanLength > bufLength - bufPos) {
      return false;
    }
    for (int offset = 0; offset < scanLength; offset++)
      if (seq.charAt(offset) != charBuf[(bufPos + offset)])
        return false;
    return true;
  }
  
  boolean matchesIgnoreCase(String seq) {
    bufferUp();
    int scanLength = seq.length();
    if (scanLength > bufLength - bufPos) {
      return false;
    }
    for (int offset = 0; offset < scanLength; offset++) {
      char upScan = Character.toUpperCase(seq.charAt(offset));
      char upTarget = Character.toUpperCase(charBuf[(bufPos + offset)]);
      if (upScan != upTarget)
        return false;
    }
    return true;
  }
  
  boolean matchesAny(char... seq) {
    if (isEmpty()) {
      return false;
    }
    bufferUp();
    char c = charBuf[bufPos];
    for (char seek : seq) {
      if (seek == c)
        return true;
    }
    return false;
  }
  
  boolean matchesAnySorted(char[] seq) {
    bufferUp();
    return (!isEmpty()) && (Arrays.binarySearch(seq, charBuf[bufPos]) >= 0);
  }
  
  boolean matchesLetter() {
    if (isEmpty())
      return false;
    char c = charBuf[bufPos];
    return ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (Character.isLetter(c));
  }
  
  boolean matchesDigit() {
    if (isEmpty())
      return false;
    char c = charBuf[bufPos];
    return (c >= '0') && (c <= '9');
  }
  
  boolean matchConsume(String seq) {
    bufferUp();
    if (matches(seq)) {
      bufPos += seq.length();
      return true;
    }
    return false;
  }
  
  boolean matchConsumeIgnoreCase(String seq)
  {
    if (matchesIgnoreCase(seq)) {
      bufPos += seq.length();
      return true;
    }
    return false;
  }
  

  boolean containsIgnoreCase(String seq)
  {
    String loScan = seq.toLowerCase(Locale.ENGLISH);
    String hiScan = seq.toUpperCase(Locale.ENGLISH);
    return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
  }
  
  public String toString()
  {
    return new String(charBuf, bufPos, bufLength - bufPos);
  }
  







  private static String cacheString(char[] charBuf, String[] stringCache, int start, int count)
  {
    if (count > 12)
      return new String(charBuf, start, count);
    if (count < 1) {
      return "";
    }
    
    int hash = 0;
    int offset = start;
    for (int i = 0; i < count; i++) {
      hash = 31 * hash + charBuf[(offset++)];
    }
    

    int index = hash & stringCache.length - 1;
    String cached = stringCache[index];
    
    if (cached == null) {
      cached = new String(charBuf, start, count);
      stringCache[index] = cached;
    } else {
      if (rangeEquals(charBuf, start, count, cached)) {
        return cached;
      }
      cached = new String(charBuf, start, count);
      stringCache[index] = cached;
    }
    
    return cached;
  }
  


  static boolean rangeEquals(char[] charBuf, int start, int count, String cached)
  {
    if (count == cached.length()) {
      int i = start;
      int j = 0;
      while (count-- != 0) {
        if (charBuf[(i++)] != cached.charAt(j++))
          return false;
      }
      return true;
    }
    return false;
  }
  
  boolean rangeEquals(int start, int count, String cached)
  {
    return rangeEquals(charBuf, start, count, cached);
  }
}

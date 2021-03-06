package org.jsoup.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import org.jsoup.helper.Validate;





public final class ConstrainableInputStream
  extends BufferedInputStream
{
  private static final int DefaultSize = 32768;
  private final boolean capped;
  private final int maxSize;
  private long startTime;
  private long timeout = 0L;
  private int remaining;
  private boolean interrupted;
  
  private ConstrainableInputStream(InputStream in, int bufferSize, int maxSize) {
    super(in, bufferSize);
    Validate.isTrue(maxSize >= 0);
    this.maxSize = maxSize;
    remaining = maxSize;
    capped = (maxSize != 0);
    startTime = System.nanoTime();
  }
  






  public static ConstrainableInputStream wrap(InputStream in, int bufferSize, int maxSize)
  {
    return (in instanceof ConstrainableInputStream) ? 
      (ConstrainableInputStream)in : 
      new ConstrainableInputStream(in, bufferSize, maxSize);
  }
  
  public int read(byte[] b, int off, int len) throws IOException
  {
    if ((interrupted) || ((capped) && (remaining <= 0)))
      return -1;
    if (Thread.interrupted())
    {
      interrupted = true;
      return -1;
    }
    if (expired()) {
      throw new SocketTimeoutException("Read timeout");
    }
    if ((capped) && (len > remaining)) {
      len = remaining;
    }
    try {
      int read = super.read(b, off, len);
      remaining -= read;
      return read;
    } catch (SocketTimeoutException e) {}
    return 0;
  }
  



  public ByteBuffer readToByteBuffer(int max)
    throws IOException
  {
    Validate.isTrue(max >= 0, "maxSize must be 0 (unlimited) or larger");
    boolean localCapped = max > 0;
    int bufferSize = (localCapped) && (max < 32768) ? max : 32768;
    byte[] readBuffer = new byte[bufferSize];
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(bufferSize);
    

    int remaining = max;
    for (;;)
    {
      int read = read(readBuffer);
      if (read == -1) break;
      if (localCapped) {
        if (read >= remaining) {
          outStream.write(readBuffer, 0, remaining);
          break;
        }
        remaining -= read;
      }
      outStream.write(readBuffer, 0, read);
    }
    return ByteBuffer.wrap(outStream.toByteArray());
  }
  
  public void reset() throws IOException
  {
    super.reset();
    remaining = (maxSize - markpos);
  }
  
  public ConstrainableInputStream timeout(long startTimeNanos, long timeoutMillis) {
    startTime = startTimeNanos;
    timeout = (timeoutMillis * 1000000L);
    return this;
  }
  
  private boolean expired() {
    if (timeout == 0L) {
      return false;
    }
    long now = System.nanoTime();
    long dur = now - startTime;
    return dur > timeout;
  }
}

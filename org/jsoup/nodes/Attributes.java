package org.jsoup.nodes;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jsoup.SerializationException;
import org.jsoup.helper.Validate;
import org.jsoup.internal.Normalizer;















public class Attributes
  implements Iterable<Attribute>, Cloneable
{
  protected static final String dataPrefix = "data-";
  private static final int InitialCapacity = 4;
  private static final int GrowthFactor = 2;
  private static final String[] Empty = new String[0];
  
  static final int NotFound = -1;
  private static final String EmptyString = "";
  private int size = 0;
  String[] keys = Empty;
  String[] vals = Empty;
  
  public Attributes() {}
  
  private void checkCapacity(int minNewSize) { Validate.isTrue(minNewSize >= size);
    int curSize = keys.length;
    if (curSize >= minNewSize) {
      return;
    }
    int newSize = curSize >= 4 ? size * 2 : 4;
    if (minNewSize > newSize) {
      newSize = minNewSize;
    }
    keys = copyOf(keys, newSize);
    vals = copyOf(vals, newSize);
  }
  
  private static String[] copyOf(String[] orig, int size)
  {
    String[] copy = new String[size];
    System.arraycopy(orig, 0, copy, 0, 
      Math.min(orig.length, size));
    return copy;
  }
  
  int indexOfKey(String key) {
    Validate.notNull(key);
    for (int i = 0; i < size; i++) {
      if (key.equals(keys[i]))
        return i;
    }
    return -1;
  }
  
  private int indexOfKeyIgnoreCase(String key) {
    Validate.notNull(key);
    for (int i = 0; i < size; i++) {
      if (key.equalsIgnoreCase(keys[i]))
        return i;
    }
    return -1;
  }
  
  static String checkNotNull(String val)
  {
    return val == null ? "" : val;
  }
  





  public String get(String key)
  {
    int i = indexOfKey(key);
    return i == -1 ? "" : checkNotNull(vals[i]);
  }
  




  public String getIgnoreCase(String key)
  {
    int i = indexOfKeyIgnoreCase(key);
    return i == -1 ? "" : checkNotNull(vals[i]);
  }
  
  private void add(String key, String value)
  {
    checkCapacity(size + 1);
    keys[size] = key;
    vals[size] = value;
    size += 1;
  }
  





  public Attributes put(String key, String value)
  {
    int i = indexOfKey(key);
    if (i != -1) {
      vals[i] = value;
    } else
      add(key, value);
    return this;
  }
  
  void putIgnoreCase(String key, String value) {
    int i = indexOfKeyIgnoreCase(key);
    if (i != -1) {
      vals[i] = value;
      if (!keys[i].equals(key)) {
        keys[i] = key;
      }
    } else {
      add(key, value);
    }
  }
  




  public Attributes put(String key, boolean value)
  {
    if (value) {
      putIgnoreCase(key, null);
    } else
      remove(key);
    return this;
  }
  




  public Attributes put(Attribute attribute)
  {
    Validate.notNull(attribute);
    put(attribute.getKey(), attribute.getValue());
    parent = this;
    return this;
  }
  
  private void remove(int index)
  {
    Validate.isFalse(index >= size);
    int shifted = size - index - 1;
    if (shifted > 0) {
      System.arraycopy(keys, index + 1, keys, index, shifted);
      System.arraycopy(vals, index + 1, vals, index, shifted);
    }
    size -= 1;
    keys[size] = null;
    vals[size] = null;
  }
  



  public void remove(String key)
  {
    int i = indexOfKey(key);
    if (i != -1) {
      remove(i);
    }
  }
  


  public void removeIgnoreCase(String key)
  {
    int i = indexOfKeyIgnoreCase(key);
    if (i != -1) {
      remove(i);
    }
  }
  



  public boolean hasKey(String key)
  {
    return indexOfKey(key) != -1;
  }
  




  public boolean hasKeyIgnoreCase(String key)
  {
    return indexOfKeyIgnoreCase(key) != -1;
  }
  



  public int size()
  {
    return size;
  }
  



  public void addAll(Attributes incoming)
  {
    if (incoming.size() == 0)
      return;
    checkCapacity(size + size);
    
    for (Attribute attr : incoming)
    {
      put(attr);
    }
  }
  
  public Iterator<Attribute> iterator()
  {
    new Iterator() {
      int i = 0;
      
      public boolean hasNext()
      {
        return i < size;
      }
      
      public Attribute next()
      {
        Attribute attr = new Attribute(keys[i], vals[i], Attributes.this);
        i += 1;
        return attr;
      }
      
      public void remove()
      {
        Attributes.this.remove(--i);
      }
    };
  }
  



  public List<Attribute> asList()
  {
    ArrayList<Attribute> list = new ArrayList(size);
    for (int i = 0; i < size; i++)
    {

      Attribute attr = vals[i] == null ? new BooleanAttribute(keys[i]) : new Attribute(keys[i], vals[i], this);
      list.add(attr);
    }
    return Collections.unmodifiableList(list);
  }
  




  public Map<String, String> dataset()
  {
    return new Dataset(this, null);
  }
  




  public String html()
  {
    StringBuilder accum = new StringBuilder();
    try {
      html(accum, new Document("").outputSettings());
    } catch (IOException e) {
      throw new SerializationException(e);
    }
    return accum.toString();
  }
  
  final void html(Appendable accum, Document.OutputSettings out) throws IOException {
    int sz = size;
    for (int i = 0; i < sz; i++)
    {
      String key = keys[i];
      String val = vals[i];
      accum.append(' ').append(key);
      

      if (!Attribute.shouldCollapseAttribute(key, val, out)) {
        accum.append("=\"");
        Entities.escape(accum, val == null ? "" : val, out, true, false, false);
        accum.append('"');
      }
    }
  }
  
  public String toString()
  {
    return html();
  }
  





  public boolean equals(Object o)
  {
    if (this == o) return true;
    if ((o == null) || (getClass() != o.getClass())) { return false;
    }
    Attributes that = (Attributes)o;
    
    if (size != size) return false;
    if (!Arrays.equals(keys, keys)) return false;
    return Arrays.equals(vals, vals);
  }
  




  public int hashCode()
  {
    int result = size;
    result = 31 * result + Arrays.hashCode(keys);
    result = 31 * result + Arrays.hashCode(vals);
    return result;
  }
  
  public Attributes clone()
  {
    try
    {
      clone = (Attributes)super.clone();
    } catch (CloneNotSupportedException e) { Attributes clone;
      throw new RuntimeException(e); }
    Attributes clone;
    size = size;
    keys = copyOf(keys, size);
    vals = copyOf(vals, size);
    return clone;
  }
  


  public void normalize()
  {
    for (int i = 0; i < size; i++) {
      keys[i] = Normalizer.lowerCase(keys[i]);
    }
  }
  
  private static class Dataset extends AbstractMap<String, String> {
    private final Attributes attributes;
    
    private Dataset(Attributes attributes) {
      this.attributes = attributes;
    }
    
    public Set<Map.Entry<String, String>> entrySet()
    {
      return new EntrySet(null);
    }
    
    public String put(String key, String value)
    {
      String dataKey = Attributes.dataKey(key);
      String oldValue = attributes.hasKey(dataKey) ? attributes.get(dataKey) : null;
      attributes.put(dataKey, value);
      return oldValue;
    }
    
    private class EntrySet extends AbstractSet<Map.Entry<String, String>> {
      private EntrySet() {}
      
      public Iterator<Map.Entry<String, String>> iterator() {
        return new Attributes.Dataset.DatasetIterator(Attributes.Dataset.this, null);
      }
      
      public int size()
      {
        int count = 0;
        Iterator iter = new Attributes.Dataset.DatasetIterator(Attributes.Dataset.this, null);
        while (iter.hasNext())
          count++;
        return count;
      } }
    
    private class DatasetIterator implements Iterator<Map.Entry<String, String>> { private DatasetIterator() {}
      
      private Iterator<Attribute> attrIter = iterator();
      
      public boolean hasNext() {
        while (attrIter.hasNext()) {
          attr = ((Attribute)attrIter.next());
          if (attr.isDataAttribute()) return true;
        }
        return false;
      }
      
      private Attribute attr;
      public Map.Entry<String, String> next() { return new Attribute(attr.getKey().substring("data-".length()), attr.getValue()); }
      
      public void remove()
      {
        remove(attr.getKey());
      }
    }
  }
  
  private static String dataKey(String key) {
    return "data-" + key;
  }
}

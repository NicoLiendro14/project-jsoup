package org.jsoup.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;





public class HtmlTreeBuilder
  extends TreeBuilder
{
  static final String[] TagsSearchInScope = { "applet", "caption", "html", "marquee", "object", "table", "td", "th" };
  static final String[] TagSearchList = { "ol", "ul" };
  static final String[] TagSearchButton = { "button" };
  static final String[] TagSearchTableScope = { "html", "table" };
  static final String[] TagSearchSelectScope = { "optgroup", "option" };
  static final String[] TagSearchEndTags = { "dd", "dt", "li", "optgroup", "option", "p", "rp", "rt" };
  static final String[] TagSearchSpecial = { "address", "applet", "area", "article", "aside", "base", "basefont", "bgsound", "blockquote", "body", "br", "button", "caption", "center", "col", "colgroup", "command", "dd", "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "iframe", "img", "input", "isindex", "li", "link", "listing", "marquee", "menu", "meta", "nav", "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script", "section", "select", "style", "summary", "table", "tbody", "td", "textarea", "tfoot", "th", "thead", "title", "tr", "ul", "wbr", "xmp" };
  
  public static final int MaxScopeSearchDepth = 100;
  
  private HtmlTreeBuilderState state;
  
  private HtmlTreeBuilderState originalState;
  
  private boolean baseUriSetFromDoc;
  
  private Element headElement;
  
  private FormElement formElement;
  
  private Element contextElement;
  
  private ArrayList<Element> formattingElements;
  private List<String> pendingTableCharacters;
  private Token.EndTag emptyEnd;
  private boolean framesetOk;
  private boolean fosterInserts;
  private boolean fragmentParsing;
  
  public HtmlTreeBuilder() {}
  
  ParseSettings defaultSettings()
  {
    return ParseSettings.htmlDefault;
  }
  
  protected void initialiseParse(Reader input, String baseUri, ParseErrorList errors, ParseSettings settings)
  {
    super.initialiseParse(input, baseUri, errors, settings);
    

    state = HtmlTreeBuilderState.Initial;
    originalState = null;
    baseUriSetFromDoc = false;
    headElement = null;
    formElement = null;
    contextElement = null;
    formattingElements = new ArrayList();
    pendingTableCharacters = new ArrayList();
    emptyEnd = new Token.EndTag();
    framesetOk = true;
    fosterInserts = false;
    fragmentParsing = false;
  }
  
  List<Node> parseFragment(String inputFragment, Element context, String baseUri, ParseErrorList errors, ParseSettings settings)
  {
    state = HtmlTreeBuilderState.Initial;
    initialiseParse(new StringReader(inputFragment), baseUri, errors, settings);
    contextElement = context;
    fragmentParsing = true;
    Element root = null;
    
    if (context != null) {
      if (context.ownerDocument() != null) {
        doc.quirksMode(context.ownerDocument().quirksMode());
      }
      
      String contextTag = context.tagName();
      if (StringUtil.in(contextTag, new String[] { "title", "textarea" })) {
        tokeniser.transition(TokeniserState.Rcdata);
      } else if (StringUtil.in(contextTag, new String[] { "iframe", "noembed", "noframes", "style", "xmp" })) {
        tokeniser.transition(TokeniserState.Rawtext);
      } else if (contextTag.equals("script")) {
        tokeniser.transition(TokeniserState.ScriptData);
      } else if (contextTag.equals("noscript")) {
        tokeniser.transition(TokeniserState.Data);
      } else if (contextTag.equals("plaintext")) {
        tokeniser.transition(TokeniserState.Data);
      } else {
        tokeniser.transition(TokeniserState.Data);
      }
      root = new Element(Tag.valueOf("html", settings), baseUri);
      doc.appendChild(root);
      stack.add(root);
      resetInsertionMode();
      


      Elements contextChain = context.parents();
      contextChain.add(0, context);
      for (Element parent : contextChain) {
        if ((parent instanceof FormElement)) {
          formElement = ((FormElement)parent);
          break;
        }
      }
    }
    
    runParser();
    if (context != null) {
      return root.childNodes();
    }
    return doc.childNodes();
  }
  
  protected boolean process(Token token)
  {
    currentToken = token;
    return state.process(token, this);
  }
  
  boolean process(Token token, HtmlTreeBuilderState state) {
    currentToken = token;
    return state.process(token, this);
  }
  
  void transition(HtmlTreeBuilderState state) {
    this.state = state;
  }
  
  HtmlTreeBuilderState state() {
    return state;
  }
  
  void markInsertionMode() {
    originalState = state;
  }
  
  HtmlTreeBuilderState originalState() {
    return originalState;
  }
  
  void framesetOk(boolean framesetOk) {
    this.framesetOk = framesetOk;
  }
  
  boolean framesetOk() {
    return framesetOk;
  }
  
  Document getDocument() {
    return doc;
  }
  
  String getBaseUri() {
    return baseUri;
  }
  
  void maybeSetBaseUri(Element base) {
    if (baseUriSetFromDoc) {
      return;
    }
    String href = base.absUrl("href");
    if (href.length() != 0) {
      baseUri = href;
      baseUriSetFromDoc = true;
      doc.setBaseUri(href);
    }
  }
  
  boolean isFragmentParsing() {
    return fragmentParsing;
  }
  
  void error(HtmlTreeBuilderState state) {
    if (errors.canAddError()) {
      errors.add(new ParseError(reader.pos(), "Unexpected token [%s] when in state [%s]", new Object[] { currentToken.tokenType(), state }));
    }
  }
  
  Element insert(Token.StartTag startTag)
  {
    if (startTag.isSelfClosing()) {
      Element el = insertEmpty(startTag);
      stack.add(el);
      tokeniser.transition(TokeniserState.Data);
      tokeniser.emit(emptyEnd.reset().name(el.tagName()));
      return el;
    }
    
    Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri, settings.normalizeAttributes(attributes));
    insert(el);
    return el;
  }
  
  Element insertStartTag(String startTagName) {
    Element el = new Element(Tag.valueOf(startTagName, settings), baseUri);
    insert(el);
    return el;
  }
  
  void insert(Element el) {
    insertNode(el);
    stack.add(el);
  }
  
  Element insertEmpty(Token.StartTag startTag) {
    Tag tag = Tag.valueOf(startTag.name(), settings);
    Element el = new Element(tag, baseUri, attributes);
    insertNode(el);
    if (startTag.isSelfClosing()) {
      if (tag.isKnownTag()) {
        if (!tag.isEmpty()) {
          tokeniser.error("Tag cannot be self closing; not a void tag");
        }
      } else
        tag.setSelfClosing();
    }
    return el;
  }
  
  FormElement insertForm(Token.StartTag startTag, boolean onStack) {
    Tag tag = Tag.valueOf(startTag.name(), settings);
    FormElement el = new FormElement(tag, baseUri, attributes);
    setFormElement(el);
    insertNode(el);
    if (onStack)
      stack.add(el);
    return el;
  }
  
  void insert(Token.Comment commentToken) {
    Comment comment = new Comment(commentToken.getData());
    insertNode(comment);
  }
  

  void insert(Token.Character characterToken)
  {
    String tagName = currentElement().tagName();
    String data = characterToken.getData();
    Node node;
    Node node; if (characterToken.isCData()) {
      node = new CDataNode(data); } else { Node node;
      if ((tagName.equals("script")) || (tagName.equals("style"))) {
        node = new DataNode(data);
      } else
        node = new TextNode(data); }
    currentElement().appendChild(node);
  }
  
  private void insertNode(Node node)
  {
    if (stack.size() == 0) {
      doc.appendChild(node);
    } else if (isFosterInserts()) {
      insertInFosterParent(node);
    } else {
      currentElement().appendChild(node);
    }
    
    if (((node instanceof Element)) && (((Element)node).tag().isFormListed()) && 
      (formElement != null)) {
      formElement.addElement((Element)node);
    }
  }
  
  Element pop() {
    int size = stack.size();
    return (Element)stack.remove(size - 1);
  }
  
  void push(Element element) {
    stack.add(element);
  }
  
  ArrayList<Element> getStack() {
    return stack;
  }
  
  boolean onStack(Element el) {
    return isElementInQueue(stack, el);
  }
  
  private boolean isElementInQueue(ArrayList<Element> queue, Element element) {
    for (int pos = queue.size() - 1; pos >= 0; pos--) {
      Element next = (Element)queue.get(pos);
      if (next == element) {
        return true;
      }
    }
    return false;
  }
  
  Element getFromStack(String elName) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      if (next.nodeName().equals(elName)) {
        return next;
      }
    }
    return null;
  }
  
  boolean removeFromStack(Element el) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      if (next == el) {
        stack.remove(pos);
        return true;
      }
    }
    return false;
  }
  
  void popStackToClose(String elName) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      stack.remove(pos);
      if (next.nodeName().equals(elName)) {
        break;
      }
    }
  }
  
  void popStackToClose(String... elNames) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      stack.remove(pos);
      if (StringUtil.inSorted(next.nodeName(), elNames))
        break;
    }
  }
  
  void popStackToBefore(String elName) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      if (next.nodeName().equals(elName)) {
        break;
      }
      stack.remove(pos);
    }
  }
  
  void clearStackToTableContext()
  {
    clearStackToContext(new String[] { "table" });
  }
  
  void clearStackToTableBodyContext() {
    clearStackToContext(new String[] { "tbody", "tfoot", "thead", "template" });
  }
  
  void clearStackToTableRowContext() {
    clearStackToContext(new String[] { "tr", "template" });
  }
  
  private void clearStackToContext(String... nodeNames) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      if ((StringUtil.in(next.nodeName(), nodeNames)) || (next.nodeName().equals("html"))) {
        break;
      }
      stack.remove(pos);
    }
  }
  
  Element aboveOnStack(Element el) {
    assert (onStack(el));
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element next = (Element)stack.get(pos);
      if (next == el) {
        return (Element)stack.get(pos - 1);
      }
    }
    return null;
  }
  
  void insertOnStackAfter(Element after, Element in) {
    int i = stack.lastIndexOf(after);
    Validate.isTrue(i != -1);
    stack.add(i + 1, in);
  }
  
  void replaceOnStack(Element out, Element in) {
    replaceInQueue(stack, out, in);
  }
  
  private void replaceInQueue(ArrayList<Element> queue, Element out, Element in) {
    int i = queue.lastIndexOf(out);
    Validate.isTrue(i != -1);
    queue.set(i, in);
  }
  
  void resetInsertionMode() {
    boolean last = false;
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element node = (Element)stack.get(pos);
      if (pos == 0) {
        last = true;
        node = contextElement;
      }
      String name = node.nodeName();
      if ("select".equals(name)) {
        transition(HtmlTreeBuilderState.InSelect);
        break; }
      if (("td".equals(name)) || (("th".equals(name)) && (!last))) {
        transition(HtmlTreeBuilderState.InCell);
        break; }
      if ("tr".equals(name)) {
        transition(HtmlTreeBuilderState.InRow);
        break; }
      if (("tbody".equals(name)) || ("thead".equals(name)) || ("tfoot".equals(name))) {
        transition(HtmlTreeBuilderState.InTableBody);
        break; }
      if ("caption".equals(name)) {
        transition(HtmlTreeBuilderState.InCaption);
        break; }
      if ("colgroup".equals(name)) {
        transition(HtmlTreeBuilderState.InColumnGroup);
        break; }
      if ("table".equals(name)) {
        transition(HtmlTreeBuilderState.InTable);
        break; }
      if ("head".equals(name)) {
        transition(HtmlTreeBuilderState.InBody);
        break; }
      if ("body".equals(name)) {
        transition(HtmlTreeBuilderState.InBody);
        break; }
      if ("frameset".equals(name)) {
        transition(HtmlTreeBuilderState.InFrameset);
        break; }
      if ("html".equals(name)) {
        transition(HtmlTreeBuilderState.BeforeHead);
        break; }
      if (last) {
        transition(HtmlTreeBuilderState.InBody);
        break;
      }
    }
  }
  

  private String[] specificScopeTarget = { null };
  
  private boolean inSpecificScope(String targetName, String[] baseTypes, String[] extraTypes) {
    specificScopeTarget[0] = targetName;
    return inSpecificScope(specificScopeTarget, baseTypes, extraTypes);
  }
  
  private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes)
  {
    int bottom = stack.size() - 1;
    int top = bottom > 100 ? bottom - 100 : 0;
    

    for (int pos = bottom; pos >= top; pos--) {
      String elName = ((Element)stack.get(pos)).nodeName();
      if (StringUtil.inSorted(elName, targetNames))
        return true;
      if (StringUtil.inSorted(elName, baseTypes))
        return false;
      if ((extraTypes != null) && (StringUtil.inSorted(elName, extraTypes))) {
        return false;
      }
    }
    return false;
  }
  
  boolean inScope(String[] targetNames) {
    return inSpecificScope(targetNames, TagsSearchInScope, null);
  }
  
  boolean inScope(String targetName) {
    return inScope(targetName, null);
  }
  
  boolean inScope(String targetName, String[] extras) {
    return inSpecificScope(targetName, TagsSearchInScope, extras);
  }
  

  boolean inListItemScope(String targetName)
  {
    return inScope(targetName, TagSearchList);
  }
  
  boolean inButtonScope(String targetName) {
    return inScope(targetName, TagSearchButton);
  }
  
  boolean inTableScope(String targetName) {
    return inSpecificScope(targetName, TagSearchTableScope, null);
  }
  
  boolean inSelectScope(String targetName) {
    for (int pos = stack.size() - 1; pos >= 0; pos--) {
      Element el = (Element)stack.get(pos);
      String elName = el.nodeName();
      if (elName.equals(targetName))
        return true;
      if (!StringUtil.inSorted(elName, TagSearchSelectScope))
        return false;
    }
    Validate.fail("Should not be reachable");
    return false;
  }
  
  void setHeadElement(Element headElement) {
    this.headElement = headElement;
  }
  
  Element getHeadElement() {
    return headElement;
  }
  
  boolean isFosterInserts() {
    return fosterInserts;
  }
  
  void setFosterInserts(boolean fosterInserts) {
    this.fosterInserts = fosterInserts;
  }
  
  FormElement getFormElement() {
    return formElement;
  }
  
  void setFormElement(FormElement formElement) {
    this.formElement = formElement;
  }
  
  void newPendingTableCharacters() {
    pendingTableCharacters = new ArrayList();
  }
  
  List<String> getPendingTableCharacters() {
    return pendingTableCharacters;
  }
  
  void setPendingTableCharacters(List<String> pendingTableCharacters) {
    this.pendingTableCharacters = pendingTableCharacters;
  }
  








  void generateImpliedEndTags(String excludeTag)
  {
    while ((excludeTag != null) && (!currentElement().nodeName().equals(excludeTag)) && 
      (StringUtil.inSorted(currentElement().nodeName(), TagSearchEndTags)))
      pop();
  }
  
  void generateImpliedEndTags() {
    generateImpliedEndTags(null);
  }
  

  boolean isSpecial(Element el)
  {
    String name = el.nodeName();
    return StringUtil.inSorted(name, TagSearchSpecial);
  }
  
  Element lastFormattingElement() {
    return formattingElements.size() > 0 ? (Element)formattingElements.get(formattingElements.size() - 1) : null;
  }
  
  Element removeLastFormattingElement() {
    int size = formattingElements.size();
    if (size > 0) {
      return (Element)formattingElements.remove(size - 1);
    }
    return null;
  }
  
  void pushActiveFormattingElements(Element in)
  {
    int numSeen = 0;
    for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
      Element el = (Element)formattingElements.get(pos);
      if (el == null) {
        break;
      }
      if (isSameFormattingElement(in, el)) {
        numSeen++;
      }
      if (numSeen == 3) {
        formattingElements.remove(pos);
        break;
      }
    }
    formattingElements.add(in);
  }
  
  private boolean isSameFormattingElement(Element a, Element b)
  {
    if (a.nodeName().equals(b.nodeName())) {} return 
    
      a.attributes().equals(b.attributes());
  }
  
  void reconstructFormattingElements()
  {
    Element last = lastFormattingElement();
    if ((last == null) || (onStack(last))) {
      return;
    }
    Element entry = last;
    int size = formattingElements.size();
    int pos = size - 1;
    boolean skip = false;
    for (;;) {
      if (pos == 0) {
        skip = true;
      }
      else {
        entry = (Element)formattingElements.get(--pos);
        if (entry != null) if (onStack(entry))
            break;
      }
    }
    for (;;) { if (!skip)
        entry = (Element)formattingElements.get(++pos);
      Validate.notNull(entry);
      

      skip = false;
      Element newEl = insertStartTag(entry.nodeName());
      
      newEl.attributes().addAll(entry.attributes());
      

      formattingElements.set(pos, newEl);
      

      if (pos == size - 1)
        break;
    }
  }
  
  void clearFormattingElementsToLastMarker() {
    while (!formattingElements.isEmpty()) {
      Element el = removeLastFormattingElement();
      if (el == null)
        break;
    }
  }
  
  void removeFromActiveFormattingElements(Element el) {
    for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
      Element next = (Element)formattingElements.get(pos);
      if (next == el) {
        formattingElements.remove(pos);
        break;
      }
    }
  }
  
  boolean isInActiveFormattingElements(Element el) {
    return isElementInQueue(formattingElements, el);
  }
  
  Element getActiveFormattingElement(String nodeName) {
    for (int pos = formattingElements.size() - 1; pos >= 0; pos--) {
      Element next = (Element)formattingElements.get(pos);
      if (next == null)
        break;
      if (next.nodeName().equals(nodeName))
        return next;
    }
    return null;
  }
  
  void replaceActiveFormattingElement(Element out, Element in) {
    replaceInQueue(formattingElements, out, in);
  }
  
  void insertMarkerToFormattingElements() {
    formattingElements.add(null);
  }
  
  void insertInFosterParent(Node in)
  {
    Element lastTable = getFromStack("table");
    boolean isLastTableParent = false;
    Element fosterParent; Element fosterParent; if (lastTable != null) {
      if (lastTable.parent() != null) {
        Element fosterParent = lastTable.parent();
        isLastTableParent = true;
      } else {
        fosterParent = aboveOnStack(lastTable);
      }
    } else { fosterParent = (Element)stack.get(0);
    }
    
    if (isLastTableParent) {
      Validate.notNull(lastTable);
      lastTable.before(in);
    }
    else {
      fosterParent.appendChild(in);
    }
  }
  
  public String toString() {
    return 
    

      "TreeBuilder{currentToken=" + currentToken + ", state=" + state + ", currentElement=" + currentElement() + '}';
  }
}

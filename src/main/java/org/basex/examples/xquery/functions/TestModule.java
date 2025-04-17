package org.basex.examples.xquery.functions;

import java.util.HashMap;
import java.util.Map;

import org.basex.query.CompileContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.QueryString;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.query.func.java.JavaCall;
import org.basex.query.util.list.AnnList;
import org.basex.query.value.Value;
import org.basex.query.value.item.FuncItem;
import org.basex.query.value.item.QNm;
import org.basex.query.value.node.ANode;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.FuncType;
import org.basex.query.value.type.SeqType;
import org.basex.query.var.Var;
import org.basex.query.var.VarRef;
import org.basex.query.var.VarScope;
import org.basex.util.hash.IntObjMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public class TestModule extends QueryModule
{

  // == Simple identity function.


  @Requires(Permission.NONE)
  @Deterministic
  @ContextDependent
  public Value function(Value inputValue) throws QueryException
  {
    Element inputElement = null;
    if (inputValue.seqType().instanceOf(SeqType.ELEMENT_O)) {
      FElem inputElem = (FElem) inputValue;
      inputElement = (Element) inputElem.toJava();
      // Type of inputElement is org.basex.api.dom.BXElem
    } else {
      throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
    }
    // Convert the input element to a BaseX element.
    Value bxResult = JavaCall.toValue(inputElement, queryContext, null);
    return bxResult;
  }


  // == Identity function with an additional DOM transformation.


  @Requires(Permission.NONE)
  @Deterministic
  @ContextDependent
  public Value domfunction(Value inputValue) throws QueryException
  {
    Element inputElement = null;
    if (inputValue.seqType().instanceOf(SeqType.ELEMENT_O)) {
      FElem inputElem = (FElem) inputValue;
      inputElement = (Element) inputElem.toJava();
      // Type of inputElement is org.basex.api.dom.BXElem
    } else {
      throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
    }
    // inputElement.getFirstChild().getAttributes().item(0).getNamespaceURI() == "http://test"
    // Transform the BXElem inputElement into an identical ElementImpl outputElement.
    Element outputElement = transformDOM(inputElement);
    // Type of outputElement is com.sun.org.apache.xerces.internal.dom.ElementImpl
    // outputElement.getFirstChild().getAttributes().item(0).getNamespaceURI() == "http://test"
    // Convert the output element to a BaseX element.
    Value bxResult = JavaCall.toValue(outputElement, queryContext, null);
    // ((FElem)bxResult).childIter().next() == <p xmlns:test="http://test" xmlns="" test:attr="p">TEST</p>
    // ((FElem)bxResult).childIter().next().attributeIter().next().qname() == "test:attr"
    return bxResult;
  }


  // == Higher order function that generates an identity function.


  @Requires(Permission.NONE)
  @Deterministic
  @ContextDependent
  public FuncItem hofunction() throws QueryException {
    // Names and types of the arguments of the generated function.
    final Var[] generatedFunctionParameters = { new VarScope().addNew(new QNm("input"), SeqType.ITEM_O, queryContext, null) };
    final Expr[] generatedFunctionParameterExprs = { new VarRef(null, generatedFunctionParameters[0]) };
    // Result type of the generated function.
    final SeqType generatedFunctionResultType = SeqType.NODE_ZM;
    // Type of the generated function.
    final FuncType generatedFunctionType = FuncType.get(generatedFunctionResultType, generatedFunctionParameters[0].declType);
    // The generated function.
    HOFunction resultFunction = new HOFunction(generatedFunctionResultType, generatedFunctionParameterExprs, queryContext);
    // Return a function item.
    return new FuncItem(null, resultFunction, generatedFunctionParameters, AnnList.EMPTY, generatedFunctionType, generatedFunctionParameters.length, null);
  }

  /**
   * The generated function.
   */
  private static final class HOFunction extends Arr {

    protected HOFunction(SeqType generatedFunctionResultType, Expr[] generatedFunctionParameterExprs, QueryContext queryContext)
    {
      super(null, generatedFunctionResultType, generatedFunctionParameterExprs);
    }

    /**
     * Evaluate the generated function.
     */
    @Override
    public Value value(final QueryContext qc)
    throws QueryException
    {
      Value inputValue = arg(0).value(qc);
      Element inputElement = null;
      if (inputValue.seqType().instanceOf(SeqType.ELEMENT_O)) {
        FElem inputElem = (FElem) inputValue;
        inputElement = (Element) inputElem.toJava();
        // Type of inputElement is org.basex.api.dom.BXElem
      } else {
        throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
      }
      // Convert the input element to a BaseX element.
      Value bxResult = JavaCall.toValue(inputElement, qc, null);
      return bxResult;
    }

    @Override
    public Expr copy(CompileContext cc, IntObjMap<Var> vm)
    {
      Expr[] functionParameterExprs = copyAll(cc, vm, this.args());
      return copyType(new HOFunction(this.seqType(), functionParameterExprs, null));
    }

    @Override
    public void toString(QueryString qs)
    {
      qs.token("generated-higher-order-function").params(exprs);
    }

  }


  // == Higher order function that generates an identity function with an additional DOM transformation.


  @Requires(Permission.NONE)
  @Deterministic
  @ContextDependent
  public FuncItem domhofunction() throws QueryException {
    // Names and types of the arguments of the generated function.
    final Var[] generatedFunctionParameters = { new VarScope().addNew(new QNm("input"), SeqType.ITEM_O, queryContext, null) };
    final Expr[] generatedFunctionParameterExprs = { new VarRef(null, generatedFunctionParameters[0]) };
    // Result type of the generated function.
    final SeqType generatedFunctionResultType = SeqType.NODE_ZM;
    // Type of the generated function.
    final FuncType generatedFunctionType = FuncType.get(generatedFunctionResultType, generatedFunctionParameters[0].declType);
    // The generated function.
    DomFunction resultFunction = new DomFunction(generatedFunctionResultType, generatedFunctionParameterExprs, queryContext);
    // Return a function item.
    return new FuncItem(null, resultFunction, generatedFunctionParameters, AnnList.EMPTY, generatedFunctionType, generatedFunctionParameters.length, null);
  }


  /**
   * The generated function.
   */
  private static final class DomFunction extends Arr {

    protected DomFunction(SeqType generatedFunctionResultType, Expr[] generatedFunctionParameterExprs, QueryContext queryContext)
    {
      super(null, generatedFunctionResultType, generatedFunctionParameterExprs);
    }

    /**
     * Evaluate the generated function.
     */
    @Override
    public Value value(final QueryContext qc)
    throws QueryException
    {
      Value inputValue = arg(0).value(qc);
      Element inputElement = null;
      if (inputValue.seqType().instanceOf(SeqType.ELEMENT_O)) {
        FElem inputElem = (FElem) inputValue;
        inputElement = (Element) inputElem.toJava();
        // Type of inputElement is org.basex.api.dom.BXElem
      } else {
        throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
      }
      // inputElement.getFirstChild().getAttributes().item(0).getNamespaceURI() == "http://test"
      // Transform the BXElem inputElement into an identical ElementImpl outputElement.
      Element outputElement = transformDOM(inputElement);
      // Type of outputElement is com.sun.org.apache.xerces.internal.dom.ElementImpl
      // outputElement.getFirstChild().getAttributes().item(0).getNamespaceURI() == "http://test"
      // Convert the output element to a BaseX element.
      Value bxResult = JavaCall.toValue(outputElement, qc, null);
      // ((FElem)bxResult).childIter().next() == <p xmlns:test="http://test" xmlns="" test:attr="p">TEST</p>
      // ((FElem)bxResult).childIter().next().attributeIter().next().qname() == "test:attr"
      return bxResult;
    }

    @Override
    public Expr copy(CompileContext cc, IntObjMap<Var> vm)
    {
      Expr[] functionParameterExprs = copyAll(cc, vm, this.args());
      return copyType(new DomFunction(this.seqType(), functionParameterExprs, null));
    }

    @Override
    public void toString(QueryString qs)
    {
      qs.token("generated-domfunction").params(exprs);
    }

  }


  // == The DOM transformation function


  /**
   * Identity transformation on a DOM Element.
   * Normally, we would use a more interesting transformation.
   */
  private static Element transformDOM(Element input) throws QueryException
  {
    try
    {
      Document domDocument = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 3.0").createDocument(null, null, null);
      Element rootElement = domToDom(domDocument, input);
      return rootElement;
    }
    catch (Exception e)
    {
      throw new QueryException(e);
    }
  }

  /**
   * Recursive helper function for the identity transformation.
   */
  private static Element domToDom(Document domDocument, Element input)
  {
    // Create a DOM element.
    String elementNamespace = input.getNamespaceURI();
    Element domElement = (elementNamespace == null || "".equals(elementNamespace))
      ? domDocument.createElement(input.getLocalName())
      : domDocument.createElementNS(elementNamespace, input.getNodeName());
    // Set the attributes.
    NamedNodeMap attributes = input.getAttributes();
    Map<String, String> attributeNamespaces = new HashMap<String, String>(); // prefix -> URI
    for (int i = 0, nrAttrs = attributes.getLength(); i < nrAttrs; ++i) {
      Attr attr = (Attr) attributes.item(i);
      String attributeNamespace = attr.getNamespaceURI();
      String attributeLocalName = attr.getLocalName();
      String attributeName = attr.getNodeName();
      int prefixColonIndex = attributeName.indexOf(':');
      String attributePrefix = (prefixColonIndex >= 0) ? attributeName.substring(0, prefixColonIndex) : "";
      if (attributeNamespace == null || "".equals(attributeNamespace)) {
        domElement.setAttribute(attributeLocalName, attr.getValue());
      } else {
        domElement.setAttributeNS(attributeNamespace, attributeName, attr.getValue());
        attributeNamespaces.put(attributePrefix, attributeNamespace);
      }
    }
    // Add namespace declarations for attributes.
    // Setting these before the attributes makes no difference.
    if (attributeNamespaces.size() > 0) {
      attributeNamespaces.entrySet().stream().
        forEach(entry -> domElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+entry.getKey(), entry.getValue()));
    }
    // Add the content.
    NodeList children = input.getChildNodes();
    for (int i = 0, nrChildren = children.getLength(); i < nrChildren; ++i) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        domElement.appendChild(domDocument.createTextNode(child.getNodeValue()));
      } else if (child.getNodeType() == Node.ELEMENT_NODE) {
        domElement.appendChild(domToDom(domDocument, (Element)child));
      }
    }
    // Return the new DOM element.
    return domElement;
  }

}

package org.basex.examples.xquery.functions;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.func.java.JavaCall;
import org.basex.query.value.Value;
import org.basex.query.value.node.FElem;
import org.basex.query.value.type.SeqType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
      // inputElement.getAttributes().item(0).getNamespaceURI() == "http://test"
    } else {
      throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
    }
    // Convert the input element to a BaseX element.
    Value bxResult = JavaCall.toValue(inputElement, queryContext, null);
    // ((FElem)bxResult).attributeIter().next().qname() == Q{http://test}attr
    return bxResult;
    // It is simpler to just return an Element, which gives the same result, but is more difficult to inspect when debugging.
    //return outputElement;
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
      // inputElement.getAttributes().item(0).getNamespaceURI() == "http://test"
    } else {
      throw new QueryException("The generated function accepts one element, but not a "+inputValue.seqType().typeString());
    }
    // Transform the BXElem inputElement into an identical ElementImpl outputElement.
    Element outputElement = transformDOM(inputElement);
    // Type of outputElement is com.sun.org.apache.xerces.internal.dom.ElementImpl
    // outputElement.getAttributes().item(0).getNamespaceURI() == "http://test"
    // outputElement.getAttributes().item(0).getNodeName() = "test:attr"
    // outputElement.getAttributes().item(1) == xmlns:test="http://test"
    // Convert the output element to a BaseX element.
    Value bxResult = JavaCall.toValue(outputElement, queryContext, null);
    // ((FElem)bxResult).namespaces == Atts[test="http://test", =""]
    // ((FElem)bxResult).attributeIter().next() == test:attr="p"
    return bxResult;
    // It is simpler to just return an Element, which gives the same result, but is more difficult to inspect when debugging.
    //return outputElement;
  }


  // == The DOM transformation function.


  /**
   * Identity transformation on a DOM Element.
   * Normally, we would use a more interesting transformation.
   * That is why this identity function constructs its copy instead of just using `cloneNode()`.
   */
  private static Element transformDOM(Element input) throws QueryException
  {
    try
    {
      // Document domDocument = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 3.0").createDocument(null, null, null);
      // Instead of the line above, we can use the following, giving the same result.
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document domDocument = builder.newDocument();
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
    // Collect namespace declarations in a map from prefix to URI.
    Map<String, String> attributeNamespaces = new HashMap<String, String>();
    // Set the attributes.
    NamedNodeMap attributes = input.getAttributes();
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
        // Simple way to set the attribute.
        domElement.setAttributeNS(attributeNamespace, attributeName, attr.getValue());
        // More complicated way to set the attribute. Same result.
        //Attr newAttr = domDocument.createAttributeNS(attributeNamespace, attributeName);
        //newAttr.setValue(attr.getValue());
        //domElement.setAttributeNodeNS(newAttr);
        // Collect namespaces for making xmlns: attributes below.
        attributeNamespaces.put(attributePrefix, attributeNamespace);
      }
    }
    // Add namespace declarations for attributes.
    // Setting these _before_ the attributes made no difference, and it is easier to do it afterwards.
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

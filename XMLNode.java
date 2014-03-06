//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.xml;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class wraps accessing the nodes of the DOM tree of an XML document
 * If an XML document is loaded for full access to its content, a DOM
 * tree is generated consisting of nodes with attributes. This class
 * implements the interface between libxml2 data storage and C++ types,
 * featuring lazy evaluation. That means instances are not created before
 * they are used.
 */
public class XMLNode {

    /** wrapped Node */
    private Element node;

    /** Document that node comes from */
    private Document doc;

    /** The ctor of XMLNode
     *
     * This ctor is declared private and thus can only be called from other instances
     * of XMLNode or friends like XMLDocument.
     *
     * @exception XMLException is thrown if the given libxml2 element is not a node
     *
     * @param doc    Document that node comes from
     * @param node   The libxml2 node that is wrapped by the new object
     */
    XMLNode(Document doc, Element node) {
        this.doc = doc;
        this.node = node;
    }

    /**
     * Get the name of that node
     *
     * Each XML element has an unique name within its document type. This
     * method provides access to the name of this node.
     *
     * @return A reference to the node's name
     */
    public String getName() {
        return node.getTagName();
    }

//    /**
//     * Get the children of this node
//     *
//     * Within the DOM tree, each node can have a list of children. Access
//     * to this list is provided by this method. The internal vector the
//     * method returns a reference to is not created before the first call
//     * to this method (lazy evaluation)
//     *
//     * @return A reference to the node's vector containing its children
//     * (hint: wrap in simple list to use in Java and C++)
//     */
//    public @PassByValue List<XMLNode> getChildren() {
//        ArrayList<XMLNode> result = new ArrayList<XMLNode>();
//        NodeList nl = node.getChildNodes();
//        for (int i = 0; i < nl.getLength(); i++) {
//            Node n = nl.item(i);
//            if (n.getNodeType() == Node.ELEMENT_NODE) {
//                result.add(new XMLNode(doc, (Element)n));
//            }
//        }
//        return result;
//    }

    /**
     * Add a child to this node
     *
     * In XML DOM trees a node can have several child nodes which are XML
     * nodes themselves. This method add such a node with a given name to
     * the structure which then can be extended by further children or
     * attributes.
     *
     * \note Each node can either have structural child nodes or text content,
     * but not both at the same time.
     *
     * @exception XMLException is thrown if the node already contains text content
     *
     * @param name   The name of the new node
     *
     * @return A reference to the newly created node
     */
    public XMLNode addChildNode(String name) {
        Element e = doc.createElement(name);
        node.appendChild(e);
        return new XMLNode(doc, e);
    }

    /**
     * Add an existing node as child to this node
     *
     * This methods adds an existing node to \a this children. By default,
     * \a node is moved with its complete subtree to its new place. It is
     * not possible to move a node into its own subtree.
     *
     * If \a copy is set to true the node and its complete subtree is copied
     * to its new place and the old version remains at its origin.
     *
     * @exception tXML2WrapperException is thrown if \this is contained in the subtree of \a node and \a copy is false
     *
     * @param node   The node to be added
     * @param copy   Set to true if a copy of \a node should be added instead of \a node itself
     *
     * @return A reference to the new child
     */
    public XMLNode addChildNode(XMLNode n, boolean copy) {
        XMLNode result = new XMLNode(doc, (Element)doc.importNode(n.node, copy));
        node.appendChild(result.node);
        return result;
    }

    /**
     * Remove a structural child node
     *
     * Removes a given node from the children list of this node.
     *
     * @exception XMLException is thrown if the given node is not a child node
     *
     * @param node   The node to remove from the list
     */
    public void removeChildNode(XMLNode node) {
        this.node.removeChild(node.node);
    }

    /**
     * Get whether this node has text content or not
     *
     * Instead of structural child nodes each node can have plain text content.
     * This method determines the existence of text content and creates an
     * internal representation for fast access (lazy evaluation). Furthermore,
     * calling this method befor accessing the text content can be used to
     * avoid runtim errors in form of instances of XML2WrapperException.
     *
     * @return Whether this node has plain text content or not
     */
    public boolean hasTextContent() {
        try {
            getTextContent();
            return true;
        } catch (XMLException e) {
            return false;
        }
    }

    /**
     * Get the plain text content of this node
     *
     * If the node contains plain text content this method grants access via
     * a String reference.
     *
     * @return A reference to the plain text content
     */
    public String getTextContent() throws XMLException {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                return n.getTextContent();
            }
        }
        return "";
    }

    /**
     * Set the plain text content of this node
     *
     * @exception XMLException is thrown if the node already has structural children
     *
     * @param content   The new plain text content of this node
     */
    public void setContent(String content) throws XMLException {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                throw new XMLException("Tried to set text content in a node that already has structural children!");
            }
        }
        removeTextContent();
        node.appendChild(doc.createTextNode(content));
    }

    /**
     * Remove the plain text content of this node
     */
    public void removeTextContent() {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                node.removeChild(n);
                removeTextContent();
                break;
            }
        }
    }

    /**
     * Get whether this node has the given attribute or not
     *
     * Each XML node can have several attributes. Calling this method
     * before accessing an attribute using its name gives information
     * about its availability and thus can be used to avoid runtime
     * errors in form of instances of XML2WrapperException.
     *
     * @return Whether this node has the given attribute or not
     */
    public boolean hasAttribute(String name) {
        return node.hasAttribute(name);
    }

    /**
     * Get an XML attribute as String
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as string.
     *
     * @exception XMLException is thrown if the requested attribute is not available
     *
     * @param name   The name of the attribute
     *
     * @return The attribute as string
     */
    public String getStringAttribute(String name) throws XMLException {
        String s = node.getAttribute(name);
        if (s == null || s.length() == 0) {
            throw new XMLException("Requested attribute `" + name + "' does not exist in this node!");
        }
        return s;
    }

    /**
     * Get an XML attribute as int
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as int.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     * @param base   The base that should be used for number interpretation
     *
     * @return The attribute as int
     */
    public int getIntAttribute(String name) throws XMLException {
        return getIntAttribute(name, 10);
    }


    /**
     * Get an XML attribute as int
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as int.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     * @param base   The base that should be used for number interpretation
     *
     * @return The attribute as int
     */
    public int getIntAttribute(String name, int base) throws XMLException {
        return Integer.parseInt(getStringAttribute(name), base);
    }

    /**
     * Get an XML attribute as long long int
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as long long
     * int.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     *
     * @return The attribute as long long int
     */
    public long getLongLongIntAttribute(String name) throws XMLException {
        return getLongLongIntAttribute(name, 10);
    }

    /**
     * Get an XML attribute as long long int
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as long long
     * int.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     * @param base   The base that should be used for number interpretation
     *
     * @return The attribute as long long int
     */
    public long getLongLongIntAttribute(String name, int base) throws XMLException {
        return Long.parseLong(getStringAttribute(name), base);
    }

    /**
     * Get an XML attribute as float
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as float.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     *
     * @return The attribute as float
     */
    public float getFloatAttribute(String name) throws XMLException {
        return Float.parseFloat(getStringAttribute(name));
    }

    /**
     * Get an XML attribute as double
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method as double.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not a number
     *
     * @param name   The name of the attribute
     *
     * @return The attribute as double
     */
    public double getDoubleAttribute(String name) throws XMLException {
        return Double.parseDouble(getStringAttribute(name));
    }

    //  /**
    //   * Get an XML attribute as enum
    //   *
    //   * If the XML node wrapped by this instance has an attribute with
    //   * the given name, its value is interpreted as name of an element
    //   * in an enumeration. Therefore, a vector with the enum's names must
    //   * be provided. The method then returns the index of the name that
    //   * was found in the attribute.
    //   *
    //   * @exception XML2WrapperException is thrown if the requested attribute's value is available or not a member of given vector
    //   *
    //   * @param name         The name of the attribute
    //   * @param enum_names   The names of the enumeration elements
    //   *
    //   * @return The index of the matching element name
    //   */
    //  template <typename TEnum>
    //  public inline const TEnum GetEnumAttribute(String name, const std::vector<std::string> &enum_names) const
    //  {
    //    const std::string value = this->GetStringAttribute(name);
    //    std::vector<std::string>::const_iterator it = std::find(enum_names.begin(), enum_names.end(), value);
    //    if (it == enum_names.end())
    //    {
    //      throw XML2WrapperException("Invalid value for " + this->GetName() + "." + name + ": `" + value + "'");
    //    }
    //    return static_cast<TEnum>(std::distance(enum_names.begin(), it));
    //  }



    /**
     * Get an XML attribute as bool
     *
     * If the XML node wrapped by this instance has an attribute with
     * the given name, its value is returned by this method interpreted
     * as bool.
     *
     * @exception XMLException is thrown if the requested attribute's value is available or not true/false
     *
     * @param name   The name of the attribute
     *
     * @return Whether the attribute's value was "true" or "false"
     */
    public boolean getBoolAttribute(String name) throws XMLException {
        String s = getStringAttribute(name).toLowerCase().trim();
        if (s.equals("true")) {
            return true;
        } else if (s.equals("false")) {
            return false;
        }
        throw new XMLException("Invalid boolean value: " + s);
    }

    /**
     * @return Parent node
     */
    public XMLNode getParent() {
        if (this.node.getParentNode() instanceof Element) {
            return new XMLNode(doc, (Element)this.node.getParentNode());
        }
        return null;
    }

    /**
     * Set an XML attribute of this node
     *
     * This methods sets the attribute with the given name to the given value.
     * If the node does not have the specified attribute yet, it will be created
     * depending on the given parameter.
     *
     * \note The value type must support streaming to be serialized
     *
     * @exception XMLException is thrown if the requested attribute does not exist and should not be created
     *
     * @param name     The name of the attribute
     * @param value    The new value
     */
    public void setAttribute(String name, Object o) {
        setStringAttribute(name, o.toString());
    }

    /**
     * Set a bool XML attribute of this node
     *
     * This is a method for special handling of bool values which should be serialized
     * into "true" and "false" instead of int representation.
     *
     * @exception XMLException is thrown if the requested attribute does not exist and should not be created
     *
     * @param name     The name of the attribute
     * @param value    The new value
     */
    public void setAttribute(String name, boolean value) {
        setStringAttribute(name, value ? "true" : "false");
    }

    /**
     * Set a string XML attribute of this node
     *
     * This is a method for special handling of string values which do not have to be
     * serialized via stringstream.
     *
     * @exception XMLException is thrown if the requested attribute does not exist and should not be created
     *
     * @param name     The name of the attribute
     * @param value    The new value
     */
    public void setAttribute(String name, String value) {
        setStringAttribute(name, value);
    }

    /**
     * Set a string XML attribute of this node
     *
     * @param name  The name of the attribute
     * @param value  The new value
     */
    private void setStringAttribute(String name, String value) {
        node.setAttribute(name, value);
    }

    /**
     * Remove an attribute from this node
     *
     * @param name     The name of the attribute
     */
    public void removeAttribute(String name) {
        node.removeAttribute(name);
    }

    /**
     * Use this way:
     *
     *  for (XMLNode child : root.children()) {
     *    ...
     *  }
     *
     * @return Object to iterate over XML node's children using foreach loop
     */
    public Iterable<XMLNode> children() {
        return new ConstChildIterator();
    }

    /**
     * ChildIterator.
     *
     * Iteration should look like this:
     *  for (ConstChildIterator it = node.getChildrenBegin(); it.get() != node.getChildrenEnd(); it.next())
     */
    private class ConstChildIterator implements Iterator<XMLNode>, Iterable<XMLNode> {

        private XMLNode next = getNextNode(node.getFirstChild());

        private XMLNode getNextNode(Node nextNode) {
            while (nextNode != null) {
                if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                    return new XMLNode(doc, (Element)nextNode);
                }
                nextNode = nextNode.getNextSibling();
            }
            return null;
        }

        /**
         * Advance to next node
         *
         * @return New current node (the one that we just moved to
         */
        public XMLNode next() {
            XMLNode current = next;
            next = getNextNode(next.node.getNextSibling());
            return current;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public void remove() {
            throw new RuntimeException("Remove operation not supported");
        }

        @Override
        public Iterator<XMLNode> iterator() {
            return this;
        }
    }

    /**
     * @return Number of children
     */
    public int childCount() {
        int i = 0;
        for (@SuppressWarnings("unused") XMLNode child : children()) {
            i++;
        }
        return i;
    }

    /**
     * Get a dump in form of xml code of the subtree starting at \a this
     *
     * @param format   Set to true if the dumped text should be indented
     */
    public String getXMLDump(boolean format) {
        try {
            StringWriter sw = new StringWriter();
            XMLDocument.writeToStream(new StreamResult(sw), format, new DOMSource(node));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed!");
        }
    }

    /**
     * @param other Node to compare with
     * @return True, if nodes are equal
     */
    public boolean nodeEquals(XMLNode other) {
        return node.isEqualNode(other.node);
    }
};

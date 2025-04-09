package ru.vsu.ppa.simplecode.util;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Helper class for working with XML nodes.
 */
@RequiredArgsConstructor
public class XmlNodeHelper {

    /**
     * The source node to perform XPath operations on.
     */
    private final Node sourceNode;

    /**
     * The XPath instance to evaluate expressions.
     */
    private final XPath xPath;

    /**
     * Evaluates an XPath expression and returns the result as an Optional Double.
     *
     * @param path the XPath expression to evaluate
     * @return an Optional containing the result of the evaluation, or an empty Optional if the result is not finite
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public XmlValue<Double> getDouble(String path) throws XPathExpressionException {
        return XmlValue.ofNode((Double) xPath.evaluate(path, sourceNode, XPathConstants.NUMBER), path)
                .filter(Double::isFinite);
    }

    /**
     * Evaluates an XPath expression and returns the result as a NodeList.
     *
     * @param path the XPath expression to evaluate
     * @return the NodeList resulting from the evaluation
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public List<Node> getNodeList(String path) throws XPathExpressionException {
        val list = (NodeList) xPath.evaluate(path, sourceNode, XPathConstants.NODESET);
        return IntStream
                .range(0, list.getLength())
                .mapToObj(list::item)
                .toList();
    }

    /**
     * Evaluates an XPath expression and returns the result as an Optional String.
     *
     * @param path the XPath expression to evaluate
     * @return an Optional containing the result of the evaluation, or an empty Optional if the result is null
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public XmlValue<String> getString(String path) throws XPathExpressionException {
        return XmlValue.ofNode((String) xPath.evaluate(path, sourceNode, XPathConstants.STRING), path);
    }

    /**
     * Evaluates an XPath expression and returns the value of a specified attribute as an Optional String.
     *
     * @param path          the XPath expression to evaluate
     * @param attributeName the name of the attribute to retrieve the value of
     * @return an Optional containing the attribute value, or an empty Optional if the attribute is not found
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public XmlValue<String> getAttributeValue(String path, String attributeName) throws XPathExpressionException {
        var node = (Node) xPath.evaluate(path, sourceNode, XPathConstants.NODE);
        if (node == null) {
            return XmlValue.empty();
        }
        return XmlValue.ofAttribute(node.getAttributes().getNamedItem(attributeName), path, attributeName)
                .map(Node::getNodeValue);
    }

    /**
     * Evaluates an XPath expression and returns the result as an Optional Node.
     *
     * @param path the XPath expression to evaluate
     * @return an Optional containing the result of the evaluation, or an empty Optional if the result is null
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public XmlValue<Node> getNode(String path) throws XPathExpressionException {
        return XmlValue.ofNode((Node) xPath.evaluate(path, sourceNode, XPathConstants.NODE), path);
    }

    /**
     * Returns the value of a specified attribute of the source node as an Optional String.
     *
     * @param attributeName the name of the attribute to retrieve the value of
     * @return an Optional containing the attribute value, or an empty Optional if the attribute is not found
     */
    public XmlValue<String> getAttributeValue(String attributeName) {
        return XmlValue.ofAttribute(
                        sourceNode.getAttributes().getNamedItem(attributeName),
                        sourceNode.getNodeName(), attributeName)
                .map(Node::getNodeValue);
    }

}

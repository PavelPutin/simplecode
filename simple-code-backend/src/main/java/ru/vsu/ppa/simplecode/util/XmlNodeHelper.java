package ru.vsu.ppa.simplecode.util;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public Optional<Double> getDouble(String path) throws XPathExpressionException {
        return Optional.of((Double) xPath.evaluate(path, sourceNode, XPathConstants.NUMBER))
                .filter(Double::isFinite);
    }

    /**
     * Evaluates an XPath expression and returns the result as a NodeList.
     *
     * @param path the XPath expression to evaluate
     * @return the NodeList resulting from the evaluation
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public NodeList getNodeList(String path) throws XPathExpressionException {
        return (NodeList) xPath.evaluate(path, sourceNode, XPathConstants.NODESET);
    }

    /**
     * Evaluates an XPath expression and returns the result as an Optional String.
     *
     * @param path the XPath expression to evaluate
     * @return an Optional containing the result of the evaluation, or an empty Optional if the result is null
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public Optional<String> getString(String path) throws XPathExpressionException {
        return Optional.ofNullable((String) xPath.evaluate(path, sourceNode, XPathConstants.STRING));
    }

    /**
     * Evaluates an XPath expression and returns the value of a specified attribute as an Optional String.
     *
     * @param path the XPath expression to evaluate
     * @param attributeName the name of the attribute to retrieve the value of
     * @return an Optional containing the attribute value, or an empty Optional if the attribute is not found
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public Optional<String> getAttributeValue(String path, String attributeName) throws XPathExpressionException {
        return this.getNode(path)
                .map(element -> element.getAttributes().getNamedItem(attributeName))
                .map(Node::getNodeValue);
    }

    /**
     * Evaluates an XPath expression and returns the result as an Optional Node.
     *
     * @param path the XPath expression to evaluate
     * @return an Optional containing the result of the evaluation, or an empty Optional if the result is null
     * @throws XPathExpressionException if the XPath expression is invalid
     */
    public Optional<Node> getNode(String path) throws XPathExpressionException {
        return Optional.ofNullable((Node) xPath.evaluate(path, sourceNode, XPathConstants.NODE));
    }

    /**
     * Returns the value of a specified attribute of the source node as an Optional String.
     *
     * @param attributeName the name of the attribute to retrieve the value of
     * @return an Optional containing the attribute value, or an empty Optional if the attribute is not found
     */
    public Optional<String> getAttributeValue(String attributeName) {
        return Optional.ofNullable(sourceNode.getAttributes().getNamedItem(attributeName))
                .map(Node::getNodeValue);
    }

}

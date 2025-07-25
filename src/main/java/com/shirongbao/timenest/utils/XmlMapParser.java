package com.shirongbao.timenest.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * XML解析工具类，用于将XML文档解析为简单的Map<String, String>结构
 * 可以正确处理CDATA部分
 */
public final class XmlMapParser {

    private XmlMapParser() {}

    /**
     * 将XML字符串解析为Map<String, String>
     * @param xmlString XML字符串
     * @return 解析后的Map
     * @throws Exception 解析过程中可能发生的异常
     */
    public static Map<String, String> parseXmlToMap(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlString)));
        document.getDocumentElement().normalize();

        Map<String, String> resultMap = new HashMap<>();
        Element rootElement = document.getDocumentElement();

        // 解析子元素（不包括根元素名称在键中）
        parseChildElements(rootElement, resultMap);

        return resultMap;
    }

    /**
     * 从文件中解析XML为Map<String, String>
     * @param xmlFile XML文件
     * @return 解析后的Map
     * @throws Exception 解析过程中可能发生的异常
     */
    public static Map<String, String> parseXmlFileToMap(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        document.getDocumentElement().normalize();

        Map<String, String> resultMap = new HashMap<>();
        Element rootElement = document.getDocumentElement();

        // 解析子元素（不包括根元素名称在键中）
        parseChildElements(rootElement, resultMap);

        return resultMap;
    }

    /**
     * 解析元素的直接子元素
     * @param parentElement 父元素
     * @param map 存储解析结果的Map
     */
    private static void parseChildElements(Element parentElement, Map<String, String> map) {
        NodeList childNodes = parentElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String nodeName = element.getNodeName();
                String textContent = element.getTextContent().trim();

                // 将元素名和内容添加到Map中
                map.put(nodeName, textContent);
            }
        }
    }

}
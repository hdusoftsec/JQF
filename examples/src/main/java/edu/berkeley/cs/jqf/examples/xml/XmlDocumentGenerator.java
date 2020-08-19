/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.examples.xml;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.internal.GeometricDistribution;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.examples.common.AlphaStringGenerator;
import edu.berkeley.cs.jqf.examples.common.Dictionary;
import edu.berkeley.cs.jqf.examples.common.DictionaryBackedStringGenerator;
import org.junit.Assume;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A generator for XML documents.
 *
 * @author Rohan Padhye
 */
public class XmlDocumentGenerator extends Generator<Document> {

    private static DocumentBuilderFactory documentBuilderFactory =
            DocumentBuilderFactory.newInstance();

    private static GeometricDistribution geometricDistribution =
            new GeometricDistribution();

    /** Mean number of child nodes for each XML element. */
    private static final double MEAN_NUM_CHILDREN = 4;

    /** Mean number of attributes for each XML element. */
    private static final double MEAN_NUM_ATTRIBUTES = 2;

    /**
     * Minimum size of XML tree.
     * @see {@link #configure(Size)}
     */
    private int minDepth = 0;

    /**
     * Maximum size of XML tree.
     * @see {@link #configure(Size)}
     */
    private int maxDepth = 4;

    private Generator<String> stringGenerator = new AlphaStringGenerator();

    public XmlDocumentGenerator() {
        super(Document.class);

        //zyp
        String using = System.getProperty("jqf.ei.usingSnippet");
        if(using != null && using.equalsIgnoreCase("true"))
        {
            usingSnippet = true;
        }
        if(usingSnippet)
        {
            // Windows "src\\test\\resources\\seeddir"
            // Linux "src/test/resources/seeddir"
            File f = new File("src/test/resources/seeddir");
            System.out.println("Use snippet file dir:"+ f.getAbsolutePath());
            parseSeedFiles(f);
            dumpSnippets();
        }
        System.out.println("usingSnippet= "+ usingSnippet +"\n\n");
    }

    /**
     * Configures the minimum/maximum size of the XML document.
     *
     * This method is not usually invoked directly. Instead, use
     * the `@Size` annotation on fuzzed parameters to configure
     * automatically.
     *
     * @param size the min/max size of the XML document
     */
    public void configure(Size size) {
        minDepth = size.min();
        maxDepth = size.max();
    }


    /**
     * Configures the string generator used by this generator to use
     * a dictionary. This is useful for overriding the default
     * arbitrary string generator with something that pulls tag names
     * from a predefined list.
     *
     * @param dict the dictionary file
     * @throws IOException if the dictionary file cannot be read
     */
    public void configure(Dictionary dict) throws IOException {
        stringGenerator = new DictionaryBackedStringGenerator(dict.value(), stringGenerator);
    }

    static int count_t = 0; //zyp
    static int err_count_t = 0; // zyp
    /**
     * Generators a random XML document.
     * @param random a source of pseudo-random values
     * @param status generation state
     * @return a randomly-generated XML document
     */
    @Override
    public Document generate(SourceOfRandomness random, GenerationStatus status) {
        DocumentBuilder builder;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        if (stringGenerator == null) {
            stringGenerator = gen().type(String.class);
        }

        Document document = builder.newDocument();
        try {
            populateDocument(document, random, status);
        } catch (DOMException e) {
            err_count_t ++;
            //System.out.println("eeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +e + " errCount:" + err_count_t);
            //e.printStackTrace();
            Assume.assumeNoException(e);
        }

        // zyp added
        count_t ++;
        //if(count_t %100 == 0) {
        //    System.out.println("XML IN String format is: \n" + XMLDocumentUtils.documentToString(document));
        //}

        return document;

    }

    private String makeString(SourceOfRandomness random, GenerationStatus status) {

        String a = stringGenerator.generate(random, status);
        //System.out.println("makeString "+a);
        return a;
    }

    private Document populateDocument(Document document, SourceOfRandomness random, GenerationStatus status) {
        //zyp
        if(usingSnippet && random.nextInt(100) < USE_SNIPPET_DOCUMENT_ROOT__RATIO &&
                hasSnippet(DOCUMENT_ROOT_NODE))
        {
            Element root = (Element)getSnippet(document, DOCUMENT_ROOT_NODE, random);
            populateElement(document, root, random, status, 0);
            document.appendChild(root);
        }else {
            Element root = document.createElement(makeString(random, status));
            populateElement(document, root, random, status, 0);
            document.appendChild(root);
        }
        return document;
    }

    private void populateElement(Document document, Element elem, SourceOfRandomness random, GenerationStatus status, int depth) {
        // Add attributes
        int numAttributes = Math.max(0, geometricDistribution.sampleWithMean(MEAN_NUM_ATTRIBUTES, random)-1);
        for (int i = 0; i < numAttributes; i++) {
            if(usingSnippet && random.nextInt(100) < USE_SNIPPET_ATTR_RATIO &&
                    hasSnippet(Node.ATTRIBUTE_NODE))
            {
                elem.setAttributeNode((Attr)getSnippet(document,Node.ATTRIBUTE_NODE, random));
            }else {
                elem.setAttribute(makeString(random, status), makeString(random, status));
            }
        }
        // Make children
        if (depth < minDepth || (depth < maxDepth && random.nextBoolean())) {
            int numChildren = Math.max(0, geometricDistribution.sampleWithMean(MEAN_NUM_CHILDREN, random)-1);
            for (int i = 0; i < numChildren; i++) {
                //zyp
                if(usingSnippet && random.nextInt(100) < USE_SNIPPET_CHILD_ELEMENT_RATIO &&
                        hasSnippet(Node.ELEMENT_NODE))
                {
                    Element child = (Element)getSnippet(document, Node.ELEMENT_NODE, random);
                    populateElement(document, child, random, status, depth + 1);
                    elem.appendChild(child);
                }else {
                    Element child = document.createElement(makeString(random, status));
                    populateElement(document, child, random, status, depth + 1);
                    elem.appendChild(child);
                }
            }
        } else if (random.nextBoolean()) {
            // Add text
            Text text = document.createTextNode(makeString(random, status));
            elem.appendChild(text);
        } else if (random.nextBoolean()) {
            // Add text as CDATA
            Text text = document.createCDATASection(makeString(random, status));
            elem.appendChild(text);
        }
    }


    /**是否采用snippet方法，为启用开关*/
    public static boolean usingSnippet = false;
    /**采用snippet element做子 root elememnt的百分比，100以内值*/
    public static int USE_SNIPPET_DOCUMENT_ROOT__RATIO = 50;
    /**采用snippet element做子 elememnt的百分比，100以内值*/
    public static int USE_SNIPPET_CHILD_ELEMENT_RATIO = 50;
    /**采用snippet Attr做Attr的百分比，100以内值*/
    public static int USE_SNIPPET_ATTR_RATIO = 50;
    /**采用snipper时对snippet进行深度复制的百分比，100以内值*/
    public static int USE_SNIPPET_DEEP_RATIO = 10;

    /**保存解析好的snippets， 为<SnippetType, SnippetArray>形式*/
    public static HashMap<Integer, ArrayList<Node>> snippetMap = new HashMap <>();

    /**引入一个常量，做为Document顶层Root的Element节点类型*/
    public static final short DOCUMENT_ROOT_NODE = 80;

    /**
     * 加入到对应类型的snippet数组如果数组中还没有相同的内容。
     * @param type
     * @param node
     * @return true如果加入，false如果有相同值所以未加入
     */
    public static boolean addNodeIfUnique(int type, Node node)
    {
        ArrayList<Node> arr = snippetMap.get(type);
        if(arr == null)
        {
            arr = new ArrayList<>();
            snippetMap.put(type, arr);
        }
        boolean found = false;
        for(Node i: arr)
        {
            if(i.isEqualNode(node))
            {
                found = true;
                break;
            }
        }
        if(!found)
        {
            arr.add(node);
        }
        return !found;
    }

    public static void obtainSnippetsFromElement(Element element, boolean isRoot)
    {
        // 首先把Element本身加入
        if(!isRoot) {
            addNodeIfUnique(Node.ELEMENT_NODE, element);
        }

        // 处理Attribute
        if(!isRoot) { // root中可能包括xsi:schemaLocation 这种属性，跳过
            NamedNodeMap attrmap = element.getAttributes();
            for (int i = 0; i < attrmap.getLength(); ++i) {
                Node attr = attrmap.item(i);
                addNodeIfUnique(Node.ATTRIBUTE_NODE, attr);
            }
        }

        // 处理 子项
        NodeList childs =  element.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node node = childs.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                obtainSnippetsFromElement((Element)node, false);
            }
            //TODO: else, for other nodetype
        }

    }

    public static void parseASeedFile(File file)
    {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            Element rootElement = doc.getDocumentElement();
            rootElement.normalize();
            addNodeIfUnique(DOCUMENT_ROOT_NODE, rootElement);
            obtainSnippetsFromElement(rootElement, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理种子目录或单个种子文件
     * @param file
     */
    public static void parseSeedFiles(File file)
    {
        if(file == null || !file.exists())
        {
            System.err.println("Input seed file/directory does not exist!, file: " + file.getAbsolutePath());
            return;
        }
        if(file.isDirectory())
        {
            for(File f :  file.listFiles())
            {
                parseASeedFile(f);
            }
        }else{
            parseASeedFile(file);
        }
    }


    public static String documentToString(Document document)
    {
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static void printNode(Node node)
    {
        if(node != null)
        {
            if(node.getNodeType() == Node.ELEMENT_NODE){
                printElement((Element)node);
            }else if (node.getNodeType() == Node.ATTRIBUTE_NODE)
            {
                printAttr((Attr)node);
            }else if(node.getNodeType() == Node.DOCUMENT_NODE)
            {
                System.out.println("!<XML document>!");
                Element rootElement = ((Document)node).getDocumentElement();
                printElement(rootElement);
            }else{
                System.out.println("Cannot print the node");
            }
        }
    }

    public static void printAttr(Attr attr) {
        System.out.print(" " + attr.getName() + "=\"" + attr.getValue() + "\"");
    }

    /** 改正了不打印TextNode的错*/
    //https://blog.csdn.net/p812438109/java/article/details/81807440
    public static void printElement(Element element) {
        System.out.print("<" + element.getTagName());
        NamedNodeMap attris = element.getAttributes();
        for (int i = 0; i < attris.getLength(); i++) {
            Attr attr = (Attr) attris.item(i);
            System.out.print(" " + attr.getName() + "=\"" + attr.getValue() + "\"");
        }
        System.out.println(">");

        NodeList nodeList = element.getChildNodes();
        Node childNode;
        for (int temp = 0; temp < nodeList.getLength(); temp++) {
            childNode = nodeList.item(temp);

            // 判断是否属于节点
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                // 判断是否还有子节点
                if(childNode.hasChildNodes()){
                    printElement((Element) childNode);
                } else if (childNode.getNodeType() != Node.COMMENT_NODE) {
                    System.out.print(childNode.getTextContent());
                }
            }else if (childNode.getNodeType() == Node.TEXT_NODE) {
                System.out.print(childNode.getTextContent());
            }
        }
        System.out.println("</" + element.getTagName() + ">");
    }

    public static void dumpSnippets()
    {
        for(Map.Entry<Integer, ArrayList<Node>> entry : snippetMap.entrySet())
        {
            Integer type = entry.getKey();
            System.out.println("\nType: "+ type + " snippetNum: "+ entry.getValue().size());
            for(Node node : entry.getValue())
            {
                System.out.println("\nsnippet: ");
                if(node.getNodeType() == Node.ATTRIBUTE_NODE)
                {
                    printAttr((Attr)node);
                }else if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    printElement((Element)node);
                }else{
                    System.out.println("Unsupported print type");
                }
            }
        }
    }

    public static Node getSnippet(Document document, int type, SourceOfRandomness random)
    {
        ArrayList<Node> array = snippetMap.get(type);
        if(array.size()>0)
        {
            Node node = array.get(random.nextInt(array.size()));
            boolean deep = random.nextInt(100)<USE_SNIPPET_DEEP_RATIO?true:false;// random.nextBoolean()
            if(type == DOCUMENT_ROOT_NODE){deep = false;}
            Node newNode =  document.importNode(node, deep);
            /*NodeList childs = newNode.getChildNodes();
            int orgLen = childs.getLength();
            if(orgLen>0)
            {
                int childNumToReserve = random.nextInt(1); // 0 or 1
                for(int i = 0; i < orgLen - childNumToReserve; i++)
                {
                    childs = newNode.getChildNodes();
                    if(childs.getLength()>0)
                    {
                        int rm = random.nextInt(childs.getLength());
                        newNode.removeChild(childs.item(rm));
                    }
                }
            };*/

            return newNode;
            //return node.cloneNode(true);
        }else{
            return null;
        }
    }

    public static boolean hasSnippet(int type)
    {
        return snippetMap.get(type)!= null && snippetMap.get(type).size() > 0;
    }

}

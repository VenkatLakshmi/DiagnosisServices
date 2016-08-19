package com.apigee.diagnosis.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by amar on 16/08/16.
 */
public class XMLResponseParser {
    // List and Item tag names
    public final static String LIST_TAG_NAME = "List";
    public final static String ITEM_TAG_NAME = "Item";

    // LOG instance
    private static Logger LOG = LoggerFactory.getLogger(XMLResponseParser.class);

    /*
     * Parse the XML response obtained from an API call and return it in an array
     *
     * Format of the API call XML response would be
     * <List>
     *     <Item>item 1</Item>
     *     <Item>item 2</Item>
     *     <Item>item 3</Item>
     *     ...
     *     <Item>item n</Item>
     * </List>
     */
    public static ArrayList<String> getItemListFromXMLResponse (String xmlResponse)
        throws Exception {

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlResponse));
        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName(LIST_TAG_NAME);
        LOG.info("getItemListFromXMLResponse number of Item nodes = " + nodes.getLength());

        if (nodes.getLength() == 0) {
            LOG.info("getItemListFromXMLResponse returning null");
            return null;
        }
        ArrayList<String> itemsList = new ArrayList<String>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            NodeList items  = element.getElementsByTagName(ITEM_TAG_NAME);

            for (int j = 0; j < items.getLength(); j++) {
                Element line = (Element) items.item(j);
                itemsList.add(getCharacterDataFromElement(line));
            }
        }
        LOG.info("getItemListFromXMLResponse returning " + itemsList);
        return itemsList;
    }

    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }
}

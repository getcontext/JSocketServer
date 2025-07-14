package server.config;


import server.utils.FileUtils;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class ServerConfig {
    private final Map<String, String> parameters = new HashMap<String, String>();

    public ServerConfig(String file) {
        read(file);
    }

    protected void read(String file) {
        try {
            String rootDir = System.getProperty("user.dir");

            file = rootDir + FileUtils.FILE_SEPARATOR + file;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(file));
            doc.getDocumentElement().normalize();
            NodeList childNodes = doc.getChildNodes();

            parameters.clear();
            for (int s = 0; s < childNodes.getLength(); s++) {
                Node item = childNodes.item(s);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    setParameter((Element) item, "port");
                    setParameter((Element) item, "websocketPort");
                    setParameter((Element) item, "webPort");
                }
            }
        } catch (SAXParseException err) {
            System.out.println("parsing error");
        } catch (ParserConfigurationException e) {
            System.out.println("configuration error");
        } catch (SAXException e) {
            System.out.println("general sax parser error");
        } catch (IOException e) {
            System.out.println("config file read error");
        } catch (Exception e) {
            System.out.println("general parser error");
        }
    }

    private void setParameter(Element item, String name) {
        Element element = item;
        NodeList tagName = element.getElementsByTagName(name);
        parameters.put(name, tagName.item(0).getChildNodes().item(0).getNodeValue().trim());
    }

    public String get(String key) {
        return parameters.get(key);
    }
}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.*;

// Download and add this library to the build path.
import org.apache.commons.codec.binary.Base64;

/**
 * This class is in charge of taking a query and returning a list of 10
 * results obtained from Bing with the given account key.
 */
public class Extractor {
    private String bingAccountKey;


    public Extractor() {
        // Use default bing account key.
        this("ghTYY7wD6LpyxUO9VRR7e1f98WFhHWYERMcw87aQTqQ");
    }


    public Extractor(String bingAccountKey) {
        this.bingAccountKey = bingAccountKey;
    }


    public List<Result> getTop10BingResults(Query query) {
        String bingUrl = getQueryURL(query);
        String accountKey = bingAccountKey;

        byte[] accountKeyBytes
            = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);

        try {
            URL url = new URL(bingUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization",
                                             "Basic " + accountKeyEnc);
            InputStream inputStream = (InputStream) urlConnection.getContent();
            byte[] contentRaw = new byte[urlConnection.getContentLength()];
            inputStream.read(contentRaw);
            String content = new String(contentRaw);
            //System.out.println(content);
            try {
                File file = new File("Results.xml");
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(content);
                //fileWriter.write("a test");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println(e);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                //System.out.println("here");
                //Using factory get an instance of document builder
                DocumentBuilder db = dbf.newDocumentBuilder();

                //parse using builder to get DOM representation of the XML file
                Document dom = db.parse("Results.xml");
                Element docEle = (Element) dom.getDocumentElement();

                //get a nodelist of elements
                NodeList n1 = docEle.getElementsByTagName("d:Title");
                NodeList n2 = docEle.getElementsByTagName("d:Url");
                NodeList n3 = docEle.getElementsByTagName("d:Description");

                ArrayList<Result> resultList = new ArrayList<Result>();
                for (int i = 0; i < 10; i++) {
                    Element e1 = (Element)n1.item(i);
                    Element e2 = (Element)n2.item(i);
                    Element e3 = (Element)n3.item(i);
                    resultList.add(new Result(
                                       e1.getTextContent(),
                                       e2.getTextContent(),
                                       e3.getTextContent()
                                   ));
                }
                return resultList;

            } catch (SAXException se) {
                se.printStackTrace();
            } catch (ParserConfigurationException pe) {
                pe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        return null;
    }

    public String getQueryURL(Query query) {
        return "https://api.datamarket.azure.com/Bing/Search/Web?"
               + "$top=10"
               + "&$format=Atom"
               + "&Query=%27" + query.serialize() + "%27";
    }
}
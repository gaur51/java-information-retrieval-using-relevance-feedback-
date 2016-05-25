import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
//import javax.lang.model.element.Element;
//import javax.naming;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.util.*;

//Download and add this library to the build path.
import org.apache.commons.codec.binary.Base64;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hp
 */
public class OldExtractor {
	private String bingAccountKey;

	public OldExtractor() {
		// Use default bing account key.
		this("ghTYY7wD6LpyxUO9VRR7e1f98WFhHWYERMcw87aQTqQ");
	}
	public OldExtractor(String bingAccountKey) {
		this.bingAccountKey = bingAccountKey;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		String bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&$format=Atom&Query=%27gates%27";
		//Provide your account key here.
		String accountKey = "ghTYY7wD6LpyxUO9VRR7e1f98WFhHWYERMcw87aQTqQ";
		//  String accountKey = "xqbCjT87/MQz25JWdRzgMHdPkGYnOz77IYmP5FUIgC8";

		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);


		try {
			URL url = new URL(bingUrl);
			URLConnection urlConnection = url.openConnection();

			urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
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

				NodeList nl = docEle.getElementsByTagName("d:Url");
				if (nl != null && nl.getLength() > 0) {
					for (int i = 0 ; i < nl.getLength(); i++) {

						//get the employee element
						Element el = (Element)nl.item(i);
						// System.out.println("here");
						System.out.println(el.getTextContent());

						//get the Employee object
						//Employee e = getEmployee(el);

						//add it to list
						//myEmpls.add(e);
					}
				}
				NodeList n2 = docEle.getElementsByTagName("d:Title");
				if (n2 != null && n2.getLength() > 0) {
					for (int i = 0 ; i < n2.getLength(); i++) {

						//get the employee element
						Element e2 = (Element)n2.item(i);
						// System.out.println("here");
						System.out.println(e2.getTextContent());

						//get the Employee object
						//Employee e = getEmployee(el);

						//add it to list
						//myEmpls.add(e);
					}
				}

				NodeList n3 = docEle.getElementsByTagName("d:Description");
				if (n3 != null && n3.getLength() > 0) {
					for (int i = 0 ; i < n3.getLength(); i++) {

						//get the employee element
						Element e3 = (Element)n3.item(i);
						// System.out.println("here");
						System.out.println(e3.getTextContent());

						//get the Employee object
						//Employee e = getEmployee(el);

						//add it to list
						//myEmpls.add(e);
					}
				}



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

		//The content string is the xml/json output from Bing.

	}

	public List<Result> getTop10BingResults(Query query) {
		// TODO: Implement this method
		ArrayList<Result> resultList = new ArrayList<Result>();
		for (int i = 0; i < 10; i++) {
			resultList.add(new Result(
			                   "Lorem Ipsum",
			                   "https://www.google.com/",
			                   "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
			               ));
		}
		return resultList;
	}
	public String getQueryURL(Query query) {
		return "https://api.datamarket.azure.com/Bing/Search/Web?$top=10&$format=Atom&Query=%27" + query.serialize() + "%27";
	}
}


package com.statoil.reinvent.importers.stocks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockXMLParser {
	
	private final Logger LOGGER = LoggerFactory.getLogger(StockXMLParser.class);
	
	public List<StockData> loadAndCreateList(InputStream dataStream) throws ParserConfigurationException, SAXException, IOException, ParseException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(dataStream);
		Element docEle = dom.getDocumentElement();
		
		List<StockData> mainList= new ArrayList<StockData>();
		
		NodeList euroStockDataElements = docEle.getElementsByTagName("data");
		if (euroStockDataElements != null && euroStockDataElements.getLength() > 0) {
			for (int i = 0; i < euroStockDataElements.getLength(); i++) {
				Element el = (Element) euroStockDataElements.item(i);
				mainList.add(getMainNumberData(el));				
			}
		}
		return mainList;
	}

	public StockData getMainNumberData(Element numberElement) throws ParseException {
		String pre = "STL";
		String post = "NOK";
		String sub = "OSLO STOCK EXCHANGE (OSE)";
		String footerText = " (GMT +1)";

		String stockExchange = getTextValue(numberElement, "RicCode");
		if (stockExchange != null && "STO".equals(stockExchange)) {
			pre = "STO";
			post = "USD";
			sub = "NEW YORK STOCK EXCHANGE (NYSE)";
			footerText = " CET <span class='delay'>at least 20mins delayed</span>";
		}
		String number = getFormattedValue(numberElement, "Quote");
		String date = getTextValue(numberElement, "Date");
		footerText = formatDateTime(date) + footerText;

		StockData mainNumberData = new StockData(stockExchange, pre, post, sub, number, footerText);
		return mainNumberData;
	}

	public String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal;
	}

	public String getFormattedValue(Element ele, String tagName) {
		DecimalFormat df = new DecimalFormat("###.##");
		return df.format(Float.parseFloat(getTextValue(ele, tagName)));
	}

	public String formatDateTime(String date) throws ParseException{
		String formattedDate = "";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
		String[] dateValues = date.split("T");
		for (String value : dateValues) {
			if (value.indexOf(":") > 0) {
				String[] timeValues = value.split(":");
				if(timeValues.length > 2){
					formattedDate = formattedDate + " " + timeValues[0] + ":" + timeValues[1];
				}
			} else {
					Date newDate = formatter.parse(value);
					formatter = new SimpleDateFormat("dd/mm/yyyy");
					formattedDate = formatter.format(newDate);
			}
		}
		return formattedDate;
	}

}

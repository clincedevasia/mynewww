package com.statoil.reinvent.importers.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.polling.importer.ImportException;
import com.day.cq.polling.importer.Importer;
import com.statoil.reinvent.services.UserMapperService;

@Service(value = Importer.class)
@Component
@Property(name = Importer.SCHEME_PROPERTY, value = "stockholder")
public class StockImporter implements Importer {

	private static final String STOCKDATA_PATH = "/stockdata";
	private static final Logger LOG = LoggerFactory.getLogger(StockImporter.class);

	@Reference
	private UserMapperService userMapperService;

	private ResourceResolver resourceResolver;

	private Map<String, Object> defaultProperties = new HashMap<>();
	
	@Override
	public void importData(String scheme, String dataSource, Resource resource, String login, String password)
			throws ImportException {
		importData(scheme, dataSource, resource);

	}

	public void importData(final String scheme, final String dataSource, final Resource resource)
			throws ImportException {
		try {
		
			LOG.debug("Inside stockholder importer");
			defaultProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
			resourceResolver = userMapperService.getJCRWriterService();
			String stockDataPath = resource.getPath() + STOCKDATA_PATH;
			Resource stockDataResource = ResourceUtil.getOrCreateResource(resourceResolver, stockDataPath,
					defaultProperties, "", true);
			InputStream in = new URL(dataSource).openStream();
			List<StockData> stockList = new StockXMLParser().loadAndCreateList(in);
			for (StockData stockData : stockList) {
				writeToRepo(stockDataResource.getPath(), stockData);
			}

		} catch (LoginException | IOException | ParserConfigurationException | SAXException | ParseException e) {
			LOG.error("Exception due to ", e);
		} finally {
			if (resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}

	}

	private void writeToRepo(String stockDataPath, StockData stockData) throws PersistenceException {

		String stockName = stockData.getRicCode();
        // As a result of changes to the XML feed. The node names in the XML feed are used as node names in CRX.
        // We map the new XML feed values to the old XML feed values.
		switch (stockName) {
		case "STL":
			stockName = "OSE";
			break;
		case "STO":
			stockName = "NYSE";
			break;
		default:
			break;
		}
		Resource stockResource = ResourceUtil.getOrCreateResource(resourceResolver, stockDataPath + "/" + stockName, defaultProperties,
				"", true);
		ModifiableValueMap stockProperties = stockResource.adaptTo(ModifiableValueMap.class);
		stockProperties.put("pre", stockData.getPre());
		stockProperties.put("post", stockData.getPost());
		stockProperties.put("sub", stockData.getSub());
		stockProperties.put("number", stockData.getNumber());
		stockProperties.put("footerText", stockData.getFooterText());
		resourceResolver.commit();
	}

}

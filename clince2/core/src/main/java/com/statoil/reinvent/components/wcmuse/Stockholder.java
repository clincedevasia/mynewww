package com.statoil.reinvent.components.wcmuse;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.statoil.reinvent.editor.fields.CheckBox;
import com.statoil.reinvent.importers.stocks.StockData;

public class Stockholder extends BaseComponent {
	
	private static final Logger LOG = LoggerFactory.getLogger(Stockholder.class);
    private static final String NODE_PATH = "/content/statoil/en/stockdata/";

    private StockData stockData;
    private String feed;
    private boolean feedSelected;

    private String id="";
    private boolean countupCheckbox;
    private String countup="";

    @Override
	protected void activated() throws Exception {
    	LOG.debug("Inside stockholder component wcmuse class");
    	this.feed = getProperties().get("feed", String.class);
        this.feedSelected = feed != null;
        this.stockData = getNodeData(this.feed);

        javax.jcr.Node node = getResource().adaptTo(javax.jcr.Node.class);
        try {
            this.id = node.getName();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        countupCheckbox = CheckBox.getValue(getProperties(), "countup");
        if(countupCheckbox){
            countup="countup ";
        }
	}

    private StockData getNodeData(String feed) throws RepositoryException {
        Resource resource = getResourceResolver().getResource(NODE_PATH + feed);
        Node dataNode = resource.adaptTo(Node.class);
        return new StockData(dataNode);
    }

    public StockData getData() {
        return stockData;
    }

    public boolean isFeedSelected() {
        return feedSelected;
    }

    public String getCountup(){
        return countup;
    }
    public String getId() {
        return id;
    }

}

package com.statoil.reinvent.importers.news;

import com.day.cq.polling.importer.HttpImporter;
import com.day.cq.polling.importer.ImportException;
import com.day.cq.polling.importer.Importer;
import com.statoil.reinvent.importers.builder.PageBuilder;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service(value = Importer.class)
@Component
@Property(name=Importer.SCHEME_PROPERTY, value="news", propertyPrivate = true)
public class NewsImporter extends HttpImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsImporter.class);
    private static final String FOLDER_NODE_TYPE = "sling:Folder";

    @Override
    protected void importData(String scheme, InputStream dataSource, String characterEncoding, Resource targetResource) throws IOException {
        Node target = targetResource.adaptTo(Node.class);
        List<NewsData> newsDataList = new NewsXmlParser().listData(dataSource);
        try {
            for (NewsData newsData : newsDataList) {
                PageBuilder pageBuilder = new PageBuilder(target);
                pageBuilder.createNode(newsData.language, FOLDER_NODE_TYPE).createNode("news", FOLDER_NODE_TYPE).createNode("archive", FOLDER_NODE_TYPE);
                pageBuilder.createNode(newsData.year, FOLDER_NODE_TYPE).createNode(newsData.month, FOLDER_NODE_TYPE).createNode(newsData.day, FOLDER_NODE_TYPE);
                NodeIterator nodeIterator = pageBuilder.getCurrentNode().getNodes();
                boolean exists = false;
                while (nodeIterator.hasNext()) {
                    Node nextNode = nodeIterator.nextNode();
                    if (nextNode.hasNode("jcr:content") && nextNode.getNode("jcr:content").getProperty("oldUrl").getString().equals(newsData.oldUrl)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    continue;
                }
                newsData.writeImagesToNode(pageBuilder.getCurrentNode());
                pageBuilder.createPage(newsData.nodeName);
                newsData.writeDownloadsToNode(pageBuilder.getCurrentNode());
                pageBuilder.createJcrContent("newsarchive");
                newsData.writeToNode(pageBuilder.getCurrentNode());
                pageBuilder.createParsys().createTextComponent(newsData.body);
            }
            target.getSession().save();
        } catch (RepositoryException e) {
            LOGGER.error("Could not write data", e);
            throw new ImportException(e);
        }

    }
}

package com.statoil.reinvent.importers.builder;

import com.statoil.reinvent.utils.ImporterUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreas on 12/08/15.
 */
public class PageBuilder {
    private List<Node> nodeList;
    private Node node;

    public PageBuilder(Node node) {
        this.nodeList = new ArrayList<>();
        nodeList.add(node);
        this.node = node;
    }

    public PageBuilder createNode(String name, String nodeType) throws RepositoryException {
        if (!node.hasNode(name)) {
            node = node.addNode(name, nodeType);
        } else {
            node = node.getNode(name);
        }
        return this;
    }

    public PageBuilder createPage(String name) throws RepositoryException {
        this.node = ImporterUtil.createPage(this.node, name);
        nodeList.add(node);
        return this;
    }

    public PageBuilder createJcrContent(String pageType) throws RepositoryException {
        this.node = ImporterUtil.createJcrContent(this.node, pageType);
        nodeList.add(node);
        return this;
    }

    public PageBuilder createParsys() throws RepositoryException {
        this.node = ImporterUtil.createParsys(this.node);
        nodeList.add(node);
        return this;
    }

    public PageBuilder createTextComponent(String text) throws RepositoryException {
        this.node = ImporterUtil.createTextComponent(this.node, text);
        nodeList.add(node);
        return this;
    }

    public Node getCurrentNode() {
        return this.node;
    }

    public PageBuilder back() {
        node = nodeList.get(nodeList.lastIndexOf(node)-1);
        return this;
    }
}

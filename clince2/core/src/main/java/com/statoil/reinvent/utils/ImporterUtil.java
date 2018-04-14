package com.statoil.reinvent.utils;

import com.day.cq.search.result.Hit;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImporterUtil {
    private ImporterUtil() {

    }

    public static Node createJcrContent(Node parent, String pageType) throws RepositoryException {
        Node jcrContent = parent.addNode("jcr:content", "cq:PageContent");
        jcrContent.setProperty("cq:allowedTemplates", new String[] {"/apps/statoil/templates/"+pageType});
        jcrContent.setProperty("cq:designPath", "/etc/designs/statoil");
        jcrContent.setProperty("cq:template", "/apps/statoil/templates/"+pageType);
        jcrContent.setProperty("sling:resourceType", "statoil/components/page/"+pageType);
        return jcrContent;
    }

    public static Node createParsys(Node parent) throws RepositoryException {
        Node par = parent.addNode(getComponentIndex(parent, "par"));
        par.setProperty("sling:resourceType", "foundation/components/parsys");
        return par;
    }

    public static Node createPage(Node root, String name) throws RepositoryException {
        String indexedName = getComponentIndex(root, name);
        return root.addNode(indexedName, "cq:Page");
    }

    public static Node createTextComponent(Node parNode, String text) throws RepositoryException {
        Node textNode = parNode.addNode(getComponentIndex(parNode, "text"));
        textNode.setProperty("sling:resourceType", "statoil/components/content/text");
        textNode.setProperty("textIsRich", "true");
        textNode.setProperty("text", text);
        return textNode;
    }

    private static String getComponentIndex(Node parent, String nodeName) throws RepositoryException {
        if (!parent.hasNode(nodeName)) {
            return nodeName;
        } else {
            int i = 0;
            String name = nodeName + "_";
            while (parent.hasNode(name + i)) {
                i++;
            }
            return name + i;
        }
    }
}

package com.statoil.reinvent.utils;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Helper methods for retrieving properties in AEM.
 */
public class PropertiesUtil {

    private PropertiesUtil() {

    }

    /**
     * Enter one or more strings into the method, and it will return the first one that is not empty.
     * Useful when selecting from properties that should be prioritized.
     *
     * Ie. "pageTitle", "navTitle", "jcr:title"
     *
     * @param options VarArg of strings to select from. Comma separated or array
     * @return
     */
    public static String getFirstUnemptyString(String... options) {
        for(String string : options) {
            if(string != null && !string.isEmpty()) {
                return string;
            }
        }
        return null;
    }

    /**
     * Fetch a String property from a node with null-check. Will return null if propery doesn't exist.
     * Will throw an exception if the path to the node doesn't exist.
     *
     * @param node
     * @param relPath
     * @return
     * @throws RepositoryException
     */
    public static String getString(Node node, String relPath) throws RepositoryException {
        Property property = node.getProperty(relPath);
        if (property != null) {
            return property.getString();
        }
        return null;
    }
}

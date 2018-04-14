package com.statoil.reinvent.importers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

public class ImportData {

    private Map<String, Object> map;

    public ImportData() {
        map = new HashMap<>();
    }

    public void put(String key, String value) {
        map.put(key, value);
    }

    public void put(String key, List<String> value) {
        String[] array = new String[value.size()];
        value.toArray(array);
        map.put(key, array);
    }

    public void put(String key, Date value) {
        map.put(key, convertDateToCalendar(value));
    }

    public void put(String key, Long value) {
        map.put(key, value);
    }

    public void add(String key, Map<String, String> map) {
        List<Map<String, String>> list = (List)this.map.get(key);
        if (list == null) {
            this.map.put(key, new ArrayList<Map<String, String>>());
        }
        ((List)this.map.get(key)).add(map);
    }

    private Calendar convertDateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public Object get(String key) {
        return map.get(key);
    }

    public String toString() {
        return map.toString();
    }

    public Node writeToNode(Node node) throws RepositoryException {
        iterateMap(node, map);
        return node;
    }

    private void setProperty(Node node, String key, Object value) throws RepositoryException {
        if (value instanceof String) {
            node.setProperty(key, (String)value);
        } else if (value instanceof String[]) {
            node.setProperty(key, (String[])value);
        } else if (value instanceof Long) {
            node.setProperty(key, (Long)value);
        } else if (value instanceof Calendar) {
            node.setProperty(key, (Calendar)value);
        } else if (value instanceof List) {
            int i = 0;
            if (node.hasNode(key)) {
                node.getNode(key).remove();
            }
            Node newNode = node.addNode(key);
            for (Map<String, Object> map : (List<Map<String, Object>>)value) {
                StringBuilder buildKey = new StringBuilder().append(key).append("-").append(i);
                iterateMap(newNode.addNode(buildKey.toString()), map);
                i++;
            }

        }
    }

    private void iterateMap(Node node, Map<String, Object> map) throws RepositoryException {
        for (String key : map.keySet()) {
            setProperty(node, key, map.get(key));
        }
    }
}

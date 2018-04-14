package com.statoil.reinvent.editor.fields;

import org.apache.sling.api.resource.ValueMap;

/**
 * Created by andreas on 15/10/15.
 */
public class CheckBox {

    private static final String TRUE = "true";

    private CheckBox() {}

    public static boolean getValue(ValueMap properties, String propertyName) {
        String propertyValue = properties.get(propertyName, String.class);
        return TRUE.equals(propertyValue);
    }
}

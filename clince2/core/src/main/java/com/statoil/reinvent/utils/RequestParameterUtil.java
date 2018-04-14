package com.statoil.reinvent.utils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

/**
 * Created by andreas on 29/09/15.
 */
public class RequestParameterUtil {

    private SlingHttpServletRequest request;

    public RequestParameterUtil(SlingHttpServletRequest request) {
        this.request = request;
    }

    public String getString(String name) {
        return getString(name, null);
    }

    public String getString(String name, String defult) {
        RequestParameter requestParameter = request.getRequestParameter(name);
        if(requestParameter != null) {
            return requestParameter.getString();
        } else {
            return defult;
        }
    }

    public Long getLong(String name) {
        return getLong(name, null);
    }

    public Long getLong(String name, Long defult) {
        RequestParameter requestParameter = request.getRequestParameter(name);
        if(requestParameter != null) {
            return Long.parseLong(requestParameter.getString());
        } else {
            return defult;
        }
    }

    public Integer getInteger(String name) {
        return getLong(name).intValue();
    }

    public Integer getInteger(String name, Integer defult) {
        return getLong(name, defult.longValue()).intValue();
    }

}

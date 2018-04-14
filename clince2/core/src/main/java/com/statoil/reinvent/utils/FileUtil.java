package com.statoil.reinvent.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.Resource;

/**
 * Created by kristoffer on 11/11/15.
 * As of this date unused, just refactored out
 */
public class FileUtil {

    public static String getExtension(Resource resource){
        String ext = getExtension(resource.getPath());
        return ext;
    }

    public static String getExtension(String resourcePath){

        String ext = FilenameUtils.getExtension(resourcePath);
        return ext;
    }
}

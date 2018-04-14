package com.statoil.reinvent.components.table;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.Page;
import com.statoil.reinvent.validators.ExcelValidator;
import com.statoil.reinvent.validators.VideoValidator;
import org.apache.commons.io.FileUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableItem {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String FILE = "file";
    public static final String LINK = "link";
    public static final String TEXT = "text";

    private String type;
    private String text;
    private String link;

    private String filesize;
    private String extension;
    private String icon;

    private boolean external;


    public TableItem(String title, String url) {
        type = LINK;
        text = title;
        link = url;
        external = url.startsWith("http");
    }

    public TableItem(String text) {
        this.type = TEXT;
        this.text = text;
        this.link = null;
    }

    public TableItem(String title, Resource resource) {
        if(resource.isResourceType("cq:Page")) {
            Page page = resource.adaptTo(Page.class);
            type = LINK;
            text = page.getTitle();
            link = page.getPath() + ".html";
        } else {
            Asset asset = resource.adaptTo(Asset.class);
            setFileInfo(asset);
        }

        if (title != null) {
            text = title;
        }
    }

    public TableItem(Resource resource) {
        this(null, resource);
    }

    private void setFileInfo(Asset asset) {
        filesize = FileUtils.byteCountToDisplaySize(asset.getOriginal().getSize());
        type = FILE;
        link = asset.getPath();
        extension = findExtension(link);
        icon = findFileIcon(link);
        external = true;
    }

    private String findExtension(String path) {
        String extension = path.substring(path.lastIndexOf(".")+1, path.length());
        return extension;
    }

    private String findFileIcon(String link) {
        VideoValidator videoValidator = new VideoValidator();
        ExcelValidator excelValidator = new ExcelValidator();
        String ext = findExtension(link);
        if (ext.equalsIgnoreCase("mp3")) {
            return "audio";
        } else if (ext.equalsIgnoreCase("pdf")) {
            return "pdf";
        } else if (excelValidator.validate(link)) {
            return "xls";
        } else if(videoValidator.validate(link)){
            return "video";
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public String getFilesize() {
        return filesize;
    }

    public String getExtension() {
        return extension;
    }

    public String getIcon() {
        return icon;
    }

    public boolean isExternal() {
        return external;
    }
}
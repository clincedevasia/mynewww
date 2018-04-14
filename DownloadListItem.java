package com.statoil.reinvent.components.list;

import com.day.cq.dam.api.Asset;

public class DownloadListItem extends ListItem {

    private String image;

    private static final String ASSET_THUMBNAIL_EXT = "/_jcr_content/renditions/cq5dam.thumbnail.620.620.png.transform/download/image.png";

    public DownloadListItem(String title, Asset asstet, boolean showImage) {
        super(title, asstet, showImage);
        if (showImage) {
            this.image = asstet.getPath() + ASSET_THUMBNAIL_EXT;
        }
    }

    @Override
    public String getImage() {
        return image;
    }
}

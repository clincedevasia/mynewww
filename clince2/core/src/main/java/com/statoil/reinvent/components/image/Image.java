package com.statoil.reinvent.components.image;

import com.day.cq.dam.api.Asset;
import com.statoil.reinvent.utils.ImageUtil;
import org.apache.commons.io.FilenameUtils;

import java.util.Locale;

public class Image {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private final Asset imageAsset;
    private final String uuid;
    private final String photographer;
    private String ariaDescriptionId;
    private Locale locale;

    public Image(Asset imageAsset) {
        this(imageAsset, null);
    }

    public Image(Asset imageAsset, Locale locale) {
        this.imageAsset = imageAsset;
        this.uuid = imageAsset.getMetadataValue("dam:sha1");
        this.photographer = findPhotographers(imageAsset);
        this.locale = locale;
    }

    private String findPhotographers(Asset imageAsset) {
        return imageAsset.getMetadataValue("dam:Credit");
    }

    public String getAlt() {
        return ImageUtil.getAlternateText(imageAsset, locale);
    }

    public String getTitle() {
        return imageAsset.getMetadataValue("jcr:title");
    }


    public String getEmptyText() {
        if (imageAsset == null) {
            return "Image";
        } else {
            return null;
        }
    }

    private String languagePostfix() {
        if (locale != null) {
            return locale.getLanguage();
        }
        return DEFAULT_LOCALE.getLanguage();
    }

    public String getSrc(String size, String... transforms) {
        return ImageUtil.getSrc(imageAsset.getPath(), size, true, transforms);
    }

    public String getSrc_large () {
        return ImageUtil.getSrc(imageAsset.getPath(), ImageUtil.RENDITION_LARGE);
    }

    public String getSrc_extralarge (){
        return ImageUtil.getSrc(imageAsset.getPath(), ImageUtil.RENDITION_EXTRA_LARGE);
    }

    public String getSrc_medium () {
        return ImageUtil.getSrc(imageAsset.getPath(), ImageUtil.RENDITION_MEDIUM);
    }

    public String getSrc_small () {
        return ImageUtil.getSrc(imageAsset.getPath(), ImageUtil.RENDITION_SMALL);
    }

    public String getSrc_extrasmall () {
        return ImageUtil.getSrc(imageAsset.getPath(), ImageUtil.RENDITION_EXTRA_SMALL);
    }


    public void setAriaDescriptionId(String ariaDescriptionId) {
        this.ariaDescriptionId = ariaDescriptionId;
    }

    public String getAriaDescriptionId() {
        return ariaDescriptionId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPhotographer() {
        return photographer;
    }

    public String getExtension() {
        return FilenameUtils.getExtension(imageAsset.getPath());
    }

    public String getPath() {
        return imageAsset.getPath();
    }
}

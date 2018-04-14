"use strict";
use(function () {

    // TODO: change currentStyle to wcm.currentStyle

    var CONST = {
        PROP_TITLE: "jcr:title",
        PROP_PAGE_TITLE: "pageTitle",
        PROP_TYPE: "type",
        PROP_DEFAULT_TYPE: "defaultType"
    }

    var title = {};

    var align = granite.resource.properties["align"];

    if(align)
        title.cssClass = align;

    var marginBottom = granite.resource.properties["space"];

    if(marginBottom && align)
        title.cssClass += " " + marginBottom;
    if(marginBottom && !align)
        title.cssClass = marginBottom;
    // The actual title content
    title.text = granite.resource.properties[CONST.PROP_TITLE]
            || wcm.currentPage.properties[CONST.PROP_PAGE_TITLE]
            || wcm.currentPage.properties[CONST.PROP_TITLE]
            || wcm.currentPage.name;

    // The HTML element name
    title.element = granite.resource.properties[CONST.PROP_TYPE]
            || currentStyle.get(CONST.PROP_DEFAULT_TYPE, "h1");

    // Adding the constants to the exposed API
    title.CONST = CONST;

    return title;

});

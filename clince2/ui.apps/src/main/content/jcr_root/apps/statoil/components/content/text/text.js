"use strict";
use(["../utils/AuthoringUtils.js"], function (AuthoringUtils) {
    
    var CONST = {
        PROP_TEXT: "text",
        PROP_RICH_FORMAT: "textIsRich",
        CONTEXT_TEXT: "text",
        CONTEXT_HTML: "html"
    };
    
    var text = {};
    var textcolumns= granite.resource.properties["textcolumns"];
    // The actual text content
    text.text = granite.resource.properties[CONST.PROP_TEXT]
            ||  granite.resource.properties["jcr:description"]
            ||  wcm.currentPage.properties["jcr:description"]
            || "";
    
    // Wether the text contains HTML or not
    text.context = (granite.resource.properties[CONST.PROP_RICH_FORMAT] || granite.resource.properties[CONST.PROP_RICH_FORMAT] == 'true')
            ? CONST.CONTEXT_HTML : CONST.CONTEXT_TEXT;


    var type= granite.resource.properties["type"];
    // Set up placeholder if empty
    if (!text.text) {
        text.cssClass = AuthoringUtils.isTouch
                ? "cq-placeholder"
                : "cq-text-placeholder-ipe";
        text.context = CONST.CONTEXT_TEXT;
        text.text = AuthoringUtils.isTouch
                ? ""
                : "Edit text";
    }



    if(!text.cssClass && type){
        text.cssClass=type
    }
    if(textcolumns)
        text.cssClass=text.cssClass + " " + textcolumns;
    // Adding the constants to the exposed API
    text.CONST = CONST;

    var reduceWidth = granite.resource.properties["reduceWidth"];
    if(reduceWidth) {
        text.cssClass += " reduce-width";
    }
    var marginBottom = granite.resource.properties["space"];

    if(marginBottom) {
        text.cssClass += " " + marginBottom;
    }

    return text;
    
});

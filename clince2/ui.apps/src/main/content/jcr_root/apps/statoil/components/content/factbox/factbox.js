"use strict";
use(["../utils/AuthoringUtils.js"], function (AuthoringUtils) {
    
    var CONST = {
        PROP_TEXT: "text",
        PROP_TITLE: "title",
        PROP_HIGHLIGHT: "highlight",
        PROP_RICH_FORMAT: "textIsRich",
        CONTEXT_TEXT: "text",
        CONTEXT_HTML: "html"
    };
    
    var text = {};

    // The actual text content

    text.title = granite.resource.properties[CONST.PROP_TITLE]
                || "";

    text.text = granite.resource.properties[CONST.PROP_TEXT]
            || "";
    // Whether the text contains HTML or not
    text.context = granite.resource.properties[CONST.PROP_RICH_FORMAT]
            ? CONST.CONTEXT_HTML : CONST.CONTEXT_TEXT

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

    var highlight = granite.resource.properties[CONST.PROP_HIGHLIGHT] || "";

    if(highlight)
        text.highlight= highlight;

    if(text.cssClass){
        text.cssClass+=" factbox-content";
    }
    else{
        text.cssClass = "factbox-content";
    }


    var textcolumns= granite.resource.properties["textcolumns"];
    if(textcolumns)
        text.cssClass+= " " + textcolumns;

    text.CONST = CONST;

    var reduceWidth = granite.resource.properties["reduceWidth"];


    if(reduceWidth)
        text.cssClass += " reduce-width";


    var marginBottom = granite.resource.properties["space"];

    if(marginBottom)
        text.cssClass += " " + marginBottom;


    return text;
    
});

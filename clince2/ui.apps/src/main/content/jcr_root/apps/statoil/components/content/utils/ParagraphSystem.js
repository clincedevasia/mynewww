"use strict";

/**
 * Paragraph system helper
 * Builds the structure that needs to be rendered starting from a root paragraph resource
 *
 * @constructor
 */
use(["/libs/sightly/js/3rd-party/q.js"], function (Q) {

    var ParagraphSystem = function (rootResource, newParagraphResourceType, inheritedMode) {
        this.rootResource = rootResource;
        this.newParagraphResourceType = newParagraphResourceType;
        this.inheritedMode = inheritedMode;
    };

    ParagraphSystem.CONST = {
        COL_CONTROL_TYPE_PROPERTY: "controlType",
        COL_CONTROL_LAYOUT_PROPERTY: "layout",
        COL_CONTROL_DEFAULT_LAYOUT: "1;cq-colctrl-default",

        COL_CONTROL_PAGEAREA_COLOR_PROPERTY: "color",
        COL_CONTROL_DEFAULT_PAGEAREA_CLASS: "page-area",


        START_PAR_COLUMN_TYPE: "start",
        BREAK_PAR_COLUMN_TYPE: "break",
        END_PAR_COLUMN_TYPE: "end",
        NORMAL_PAR_TYPE: "normal",

        COL_CONTROL_SUFFIX: "/colctrl",
        IPARSYS_SUFFIX: "/iparsys/par"
    };

    ParagraphSystem.prototype.isColumnControl = function (resource) {
        return this.isOfType(resource, ParagraphSystem.CONST.COL_CONTROL_SUFFIX);
    }

    ParagraphSystem.prototype.isInheritanceParagraph = function (resource) {
        return this.isOfType(resource, ParagraphSystem.CONST.IPARSYS_SUFFIX);
    }

    /**
     * Determines if a resource's type has a given suffix
     */
    ParagraphSystem.prototype.isOfType = function (resource, typeSuffix) {
        var resourceType = resource.resourceType;
        return resourceType.indexOf(typeSuffix) == resourceType.length - typeSuffix.length;
    }

    /**
     * Builds and returns the paragraph system
     */
    ParagraphSystem.prototype.getParagraphs = function () {

        var paragraphSystemPromise = Q.defer();
        var that = this;

        this.rootResource.getChildren().then(function (paragraphs) {
            var colNum = 0;
            var totalColumns = 0;
            var layout = "";
            var pagearea_class = "";

            // used when in iParsys mode
            var hasFakeInherited = false;

            // container for the paragraph render information
            var paragraphSystem = [];

            // used as container when in "column" mode
            var columnsContainer = undefined;

            log.debug("Found " + paragraphs.length + " paragraphs in the paragraph system ");

            /*
             * Iterate though the paragraph's children and create a structure so we can render them
             */
            try {
                for (var paragraphIndex = 0 ; paragraphIndex < paragraphs.length ; paragraphIndex++) {
                    var currentPar = paragraphs[paragraphIndex];
                    var parResourceType = currentPar.resourceType;

                    // if no column marker was found yet, append to the main paragraphSystem object
                    // otherwise add to current found column
                    var destination = paragraphSystem;
                    if (columnsContainer) {
                        destination = columnsContainer.currentColumn.children;
                    }

                    if (that.isInheritanceParagraph(currentPar) && that.inheritedMode) {
                        // found inheritance paragraph, need to include the inherited part
                        destination.push({
                            columns: false,
                            resourcePath: currentPar.path,
                            resourceType: currentPar.resourceType,
                            cssClasses: "iparys_inherited"
                        });

                        hasFakeInherited = true;
                    } else if (that.isColumnControl(currentPar)) {

                        var colControlType = currentPar.properties[ParagraphSystem.CONST.COL_CONTROL_TYPE_PROPERTY];
                        if (!colControlType) {
                            colControlType = ParagraphSystem.CONST.START_PAR_COLUMN_TYPE;
                        }

                        if (colControlType == ParagraphSystem.CONST.START_PAR_COLUMN_TYPE) {
                            // found start column marker

                            // reset column information
                            colNum = 0;
                            totalColumns = 1;

                            // read and compute the layout information
                            layout = currentPar.properties[ParagraphSystem.CONST.COL_CONTROL_LAYOUT_PROPERTY];
                            pagearea_class = currentPar.properties[ParagraphSystem.CONST.COL_CONTROL_PAGEAREA_COLOR_PROPERTY];
                            if (!layout) {
                                layout = ParagraphSystem.CONST.COL_CONTROL_DEFAULT_LAYOUT;
                            }
                            if(!pagearea_class){
                                pagearea_class = ParagraphSystem.CONST.COL_CONTROL_DEFAULT_PAGEAREA_CLASS;
                            }
                            var layoutInfo = layout.split(";");
                            if (layoutInfo.length > 1) {
                                totalColumns = layoutInfo[0];
                                layout = layoutInfo[1];
                            }

                            if (typeof wcmmode != "undefined" && wcmmode.isEdit()) {
                                // append start column control resource to main paragraphSystem object
                                paragraphSystem.push({
                                    columns: false,
                                    resourcePath: currentPar.path,
                                    resourceType: parResourceType,
                                    cssClasses: "section colctrl-start colctrl"
                                });

                            }

                            // create a new column container holder with no items and initialized current column container
                            columnsContainer = {
                                columns: true,
                                columnsContainerCss: "parsys-column " + layout + " " + pagearea_class,
                                currentColumn: {
                                    cssClasses:"parsys_column " + (layout + "-c" + colNum),
                                    children: []
                                },
                                items: []
                            };

                        } else if (colControlType == ParagraphSystem.CONST.BREAK_PAR_COLUMN_TYPE) {
                            // found column break marker

                            // initialize column index
                            colNum++;
                            if (typeof wcmmode != "undefined" && wcmmode.isEdit() && that.newParagraphResourceType != undefined) {

                                // add "new paragraph" resource when in edit mode
                                destination.push({
                                    columns: false,
                                    resourcePath: currentPar.path,
                                    resourceType: that.newParagraphResourceType,
                                    cssClasses: "new section"
                                });
                            }

                            if (colNum <= totalColumns) {
                                // push current built column to items array and initialize a new empty column holder
                                if (columnsContainer) {
                                    columnsContainer.items.push(columnsContainer.currentColumn);

                                    columnsContainer.currentColumn = {
                                        cssClasses:"parsys_column " + (layout + "-c" + colNum),
                                        children: []
                                    }
                                }
                            }

                        } else if (colControlType == ParagraphSystem.CONST.END_PAR_COLUMN_TYPE) {
                            // found column end marker

                            if (typeof wcmmode != "undefined" && wcmmode.isEdit() && that.newParagraphResourceType != undefined) {
                                destination.push({
                                    columns: false,
                                    resourcePath: currentPar.path,
                                    resourceType: that.newParagraphResourceType,
                                    cssClasses: "new section"
                                });
                            }

                            if (colNum > 0) {
                                // save the current build column and push the whole column container object to the main paragraphSystem object
                                if (columnsContainer) {
                                    columnsContainer.items.push(columnsContainer.currentColumn);
                                    columnsContainer.currentColumn = undefined;
                                }

                                paragraphSystem.push(columnsContainer);

                                // undefine the columns container until a new start column marker is found
                                columnsContainer = undefined;
                            }

                            // reset column information
                            colNum = 0;
                            totalColumns = 0;

                            if (typeof wcmmode != "undefined" && wcmmode.isEdit()) {
                                paragraphSystem.push({
                                        columns: false,
                                        resourcePath: currentPar.path,
                                        resourceType: parResourceType,
                                        cssClasses: "section colctrl"
                                });
                            }
                        }

                    } else {
                        destination.push({
                            columns: false,
                            resourcePath: currentPar.path,
                            resourceType: parResourceType,
                            cssClasses: ""
                        });
                    }
                }
                if (that.inheritedMode && !hasFakeInherited) {

                    if (that.newParagraphResourceType != undefined) {
                        paragraphSystem.push({
                            columns: false,
                            resourcePath: "*",
                            resourceType: that.newParagraphResourceType,
                            cssClasses: "new section"
                        });
                    }

                    paragraphSystem.push({
                        columns: false,
                        resourcePath: that.rootResource.path + "/iparsys_fake_par",
                        resourceType: that.rootResource.resourceType + "/par",
                        cssClasses: "iparys_inherited"
                    });

                } else if (typeof wcmmode != "undefined" && wcmmode.isEdit() && that.newParagraphResourceType != undefined) {
                    paragraphSystem.push({
                            columns: false,
                            resourcePath: "*",
                            resourceType: that.newParagraphResourceType,
                            cssClasses: "new section"
                    });
                }

                log.debug("Dumping Parsys structure:");
                for (var parIdx = 0 ; parIdx < paragraphSystem.length ; parIdx++) {
                    if (paragraphSystem[parIdx].columns) {
                        log.debug("Found column container!");
                        for (var colIdx = 0 ; colIdx < paragraphSystem[parIdx].items.length ; colIdx++) {
                            log.debug("Column " + colIdx + " Items:");
                            var column = paragraphSystem[parIdx].items[colIdx];
                            for (var colChildIdx = 0 ; colChildIdx < column.children.length ; colChildIdx++) {
                                var parItem = column.children[colChildIdx];
                                log.debug("Paragraph info: resourcePath=" + parItem.resourcePath
                                        + " resourceType=" + parItem.resourceType
                                        + " cssClasses=" + parItem.cssClasses);
                            }
                        }
                        log.debug("End column container");
                    } else {
                        var parItem = paragraphSystem[parIdx];
                        log.debug("Paragraph info: resourcePath=" + parItem.resourcePath
                                + " resourceType=" + parItem.resourceType
                                + " cssClasses=" + parItem.cssClasses);
                    }
                }
                log.debug("Parsys structure Finished");

                paragraphSystemPromise.resolve(paragraphSystem);
            } catch (e) {
                log.error("Error building paragraph system " + e);
            }
        });

        return paragraphSystemPromise.promise;
    };

    return ParagraphSystem;
});
"use strict";

var global = this;

use(["ResourceUtils.js", "/libs/sightly/js/3rd-party/q.js"], function (ResourceUtils, Q) {
    var DateUtils = {};

    DateUtils.getModifiedDate = function(pageResource) {
        var modifDatePromise = Q.defer();

        var parseDate = function (dateProperty, promise) {
            try {
                var month = dateProperty.get(global.Packages.java.util.Calendar.MONTH) + 1;
                var day = dateProperty.get(global.Packages.java.util.Calendar.DAY_OF_MONTH);
                var year = dateProperty.get(global.Packages.java.util.Calendar.YEAR);
                var hourOfDay = dateProperty.get(global.Packages.java.util.Calendar.HOUR_OF_DAY);
                var minutes = dateProperty.get(global.Packages.java.util.Calendar.MINUTE);

                var modifDate = month + "/" + day + "/" + year + " " + hourOfDay + ":" + minutes;

                log.debug("Page " + pageResource.path + " modification date is " + modifDate);

                promise.resolve(modifDate);
            } catch (e) {
                log.error("Can't determine page " + pageResource.path + " modification date" + e);

                promise.resolve(undefined);
            }
        };

        var propValue = pageResource.properties["date"];
        if (!propValue) {
            ResourceUtils.getResource(pageResource.path + "/jcr:content")
                .then(function (pageContentResource) {
                    propValue = pageContentResource.properties["cq:lastModified"]
                        || pageContentResource.properties["jcr:lastModified"];

                    parseDate(propValue, modifDatePromise);
                });
        } else {
            parseDate(propValue, modifDatePromise);
        }

        return modifDatePromise.promise;
    }

    return DateUtils;
});
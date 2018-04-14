"use strict";

use(function () {
    var StringUtils = {};

    StringUtils.truncate = function(string, length) {
        var substrings = string.split(" ");

        if (substrings.length > length)
            return substrings.slice(0, length).join(" ").concat("...");

        return string;
    }

    return StringUtils;
});
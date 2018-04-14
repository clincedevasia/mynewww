(function ($, $document) {
    "use strict";

    $(document).on("click", ".cq-dialog-submit", function (e) {
        if ($("input[name='./sling:resourceType']").val() == "statoil/components/content/colctrl") {
            var layout = $("select[name='./layout_placeholder']").val().split(";");
            var backgroundcolor = $("input[name='./backgroundcolor']").val();
            var align_buttons = $("input[name='./align-buttons']").val();
            var spacing = $("input[name='./spacing']").val();
            var build_layout = layout[0] + ";";
            if (backgroundcolor) {
                build_layout += backgroundcolor + " ";
            }
            if (align_buttons) {
                build_layout += align_buttons + " ";
            }
            if (spacing) {
                build_layout += spacing + " ";
            }
            build_layout += layout[1];
            $("input[name='./layout']").val(build_layout);
        }
    });
})($, $(document));

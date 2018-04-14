<%--
  ADOBE CONFIDENTIAL

  Copyright 2012 Adobe Systems Incorporated
  All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and may be covered by U.S. and Foreign Patents,
  patents in process, and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%
%><%@include file="/libs/granite/ui/global.jsp" %><%
%><%@page session="false"
          import="java.net.URI,
                  java.net.URISyntaxException,
                  org.apache.sling.api.SlingHttpServletRequest,
                  org.slf4j.Logger,
                  com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.ComponentHelper,
                  com.adobe.granite.ui.components.Config,
                  com.adobe.granite.ui.components.Tag" %>
<%@ page import="javax.jcr.Session" %>
<%@ page import="javax.jcr.Node" %><%

/**
 * Hyperlink is a component to represent a standard HTML hyperlink (&lt;a&gt;).
 *
 * @component
 * @name Hyperlink
 * @location /libs/granite/ui/components/foundation/hyperlink
 *
 * @property {String} id id attr
 * @property {String} rel class attr (this is to indicate the semantic relationship of the element)
 * @property {String} class class attr
 * @property {String} title title attr
 * @property {Boolean} [hidden] hidden attr
 * @property {String} text the body text of the element
 * @property {StringEL} href href attr
 * @property {StringEL} href_i18n The same as href but this property would be translated. This is usually used to produce different href based on locale.
 * @property {String} target target attr
 * @property {String} icon icon class
 * @property {Boolean} hideText visually hide the text
 * @property {String} x-cq-linkchecker x-cq-linkchecker attr
 * @property {String} &lt;other&gt; data-&lt;other&gt; attr
 *
 * @example
 * + mybutton
 *   - jcr:primaryType = "nt:unstructured"
 *   - sling:resourceType = "granite/ui/components/foundation/hyperlink"
 *   - href = "mylink.html"
 *   - icon = "icon-info-sign"
 *   - text = "Properties"
 */

if (!cmp.getRenderCondition().check()) {
    return;
}

Config cfg = cmp.getConfig();
String icon = cfg.get("icon", String.class);
String iconSize = cfg.get("iconSize", "S");

Tag tag = cmp.consumeTag();

AttrBuilder attrs = tag.getAttrs();
cmp.populateCommonAttrs(attrs);

attrs.add("target", cfg.get("target", String.class));

attrs.add("x-cq-linkchecker", cfg.get("x-cq-linkchecker", String.class));

String href;
if (cfg.get("href_i18n", String.class) != null) {
    href = i18n.getVar(cmp.getExpressionHelper().getString(cfg.get("href_i18n", "")));
} else {
    href = cmp.getExpressionHelper().getString(cfg.get("href", ""));
}

attrs.addClass("coral-Link");

// only enable folder creation within folders
/* boolean enabled = false;
String path = slingRequest.getRequestPathInfo().getSuffix();
Session session = resourceResolver.adaptTo(Session.class);
if (path != null && session.nodeExists(path)) {
    Node node = session.getNode(path);
    if (node.isNodeType("sling:Folder") || node.isNodeType("sling:OrderedFolder")) {
        enabled = true;
    }
}*/

// enable folder creation everywhere
boolean enabled = true;

if (enabled) {
    attrs.addHref("href", href);
} else {
    // disable the link:
    // set the class attribute manually (add 'is-disabled',
    // remove 'endor-List-item--interactive')
    attrs.set("class", "endor-List-item coral-Link is-disabled");
}

%><a <%= attrs.build() %>><%
    if (icon != null) {
        %><i class="coral-Icon <%= cmp.getIconClass(icon) %> coral-Icon--size<%= iconSize %>"></i> <%
    }
    if (!cfg.get("hideText", false)) {
        %><%= outVar(xssAPI, i18n, cfg.get("text", "")) %><%
    }
%></a>

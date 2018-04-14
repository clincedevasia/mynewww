<%--
  ADOBE CONFIDENTIAL

  Copyright 2014 Adobe Systems Incorporated
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
%><%@include file="/libs/granite/ui/global.jsp"%><%
%><%@page session="false"%><%
%><%@page import="java.util.ArrayList,
				  java.util.Calendar,
				  java.util.Iterator,
				  java.util.List,
				  java.util.UUID,
				  javax.jcr.RepositoryException,
				  javax.jcr.Session,
				  javax.jcr.security.AccessControlManager,
				  javax.jcr.security.Privilege,
				  org.apache.commons.lang.StringUtils,
				  org.apache.jackrabbit.util.Text,
				  org.apache.sling.api.resource.ResourceResolver,
                  org.apache.sling.api.resource.ResourceUtil,
                  org.apache.sling.api.resource.ValueMap,
                  com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.Tag,
                  com.day.cq.wcm.api.Page,
                  com.day.cq.wcm.api.PageManager,
                  com.day.cq.wcm.msm.api.LiveRelationshipManager" %><%

AccessControlManager acm = null;
try {
    acm = resourceResolver.adaptTo(Session.class).getAccessControlManager();
} catch (RepositoryException e) {
    log.error("Unable to get access manager", e);
}

LiveRelationshipManager liveRelationshipManager = resourceResolver.adaptTo(LiveRelationshipManager.class);
boolean isLiveCopy = liveRelationshipManager.hasLiveRelationship(resource);

Page cqPage = resource.adaptTo(Page.class);
String type;
String title;
String thumbnailUrl;
String actionRels = StringUtils.join(getActionRels(resource, acm), " ");

if (cqPage != null) {
    type = "page";
    title = cqPage.getTitle();
    thumbnailUrl = request.getContextPath() + getThumbnailUrl(cqPage, 60, 60);
} else {
    type = "directory";
    title = getFolderTitle(resource);
    thumbnailUrl = request.getContextPath() + Text.escapePath(resource.getPath()) + ".folderthumbnail.jpg";
}

Tag tag = cmp.consumeTag();
AttrBuilder attrs = tag.getAttrs();

attrs.add("itemscope", "itemscope");
attrs.add("data-path", resource.getPath()); // for compatibility
attrs.add("data-resourcetype", resource.getResourceType());
attrs.add("data-timeline", true);
attrs.add("data-item-title", title);
attrs.add("data-item-type", type);
attrs.add("data-livecopy", isLiveCopy);
        
attrs.addClass("coral-ColumnView-item");
attrs.add("data-href", "#" + UUID.randomUUID());

if (hasChildren(resource)) {
    attrs.add("data-src", request.getContextPath() + "/libs/wcm/core/content/sites/column{.offset,limit}.html" + Text.escapePath(resource.getPath()));
    attrs.addClass("coral-ColumnView-item--hasChildren");
} else {
    attrs.add("data-src", request.getContextPath() + "/libs/wcm/core/content/sites/previewcolumn.html" + Text.escapePath(resource.getPath()));
}

%><a <%= attrs.build() %>>
    <div class="coral-ColumnView-icon"><%
        if (cqPage != null) {
            %><img class="coral-ColumnView-thumbnail" src="<%= xssAPI.getValidHref(thumbnailUrl) %>" alt="" itemprop="thumbnail"><%
        } else {
            %><i class="coral-Icon coral-Icon--folder coral-Icon--sizeS"></i><%
        }
    %></div>
    <div class="coral-ColumnView-label foundation-collection-item-title" itemprop="title" title="<%= xssAPI.encodeForHTMLAttr(title) %>"><%= xssAPI.encodeForHTML(title) %></div>
    <div class="foundation-collection-quickactions" hidden data-foundation-collection-quickactions-rel="<%= xssAPI.encodeForHTMLAttr(actionRels) %>"></div>
</a><%!
    private String getThumbnailUrl(Page page, int width, int height) {
        String ck = "";
        
        ValueMap metadata = page.getProperties("image/file/jcr:content");
        if (metadata != null) {
            Calendar cal = metadata.get("jcr:lastModified", Calendar.class);
            if (cal != null) {
                ck = "" + (cal.getTimeInMillis() / 1000);
            }
        }

        return Text.escapePath(page.getPath()) + ".thumb." + width + "." + height + ".png?ck=" + ck;
    }

    private List<String> getActionRels(Resource resource, AccessControlManager acm) {
        List<String> actionRels = new ArrayList<String>();

        actionRels.add("cq-siteadmin-admin-actions-create-activator");
        actionRels.add("cq-siteadmin-admin-actions-copy-activator");
        actionRels.add("cq-siteadmin-admin-createfolder-at-activator");

        Page page = resource.adaptTo(Page.class);

        if (page != null && !page.isLocked()) {
            actionRels.add("cq-siteadmin-admin-actions-lockpage-activator");
        }

        if (page != null && page.isLocked() && page.canUnlock()) {
            actionRels.add("cq-siteadmin-admin-actions-unlockpage-activator");
        }

        if (hasPermission(acm, resource, Privilege.JCR_READ)) {
            if (page != null) {
                actionRels.add("cq-siteadmin-admin-actions-edit-activator");
                actionRels.add("cq-siteadmin-admin-actions-properties-activator");
            } else {
                // for nt:folder there are no properties to edit
                if (!resource.isResourceType("nt:folder")) {
                    actionRels.add("cq-siteadmin-admin-actions-folderproperties-activator");
                }
            }
        }

        if (hasPermission(acm, resource, Privilege.JCR_REMOVE_NODE)) {
            actionRels.add("cq-siteadmin-admin-actions-move-activator");
            actionRels.add("cq-siteadmin-admin-actions-delete-activator");
        }

        if (hasPermission(acm, resource, "crx:replicate")) {
            actionRels.add("cq-siteadmin-admin-actions-publish-activator");
            actionRels.add("cq-siteadmin-admin-actions-unpublish-activator");
        }
        
        if (hasPermission(acm, resource, "jcr:addChildNodes")) {
            ResourceResolver resourceResolver = resource.getResourceResolver();
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
            
            if (!pageManager.getTemplates(resource.getPath()).isEmpty()) {
                actionRels.add("cq-siteadmin-admin-createpage-at-activator");
            }
            
            actionRels.add("cq-siteadmin-admin-createsite-at-activator");
            actionRels.add("cq-siteadmin-admin-createcatalog-at-activator");
            
            ValueMap vm = ResourceUtil.getValueMap(resource);
            String primaryType = vm.get("jcr:primaryType", String.class);
            
            if ("sling:Folder".equals(primaryType) ||
                    "sling:OrderedFolder".equals(primaryType) ||
                    "nt:folder".equals(primaryType)) {
                actionRels.add("cq-siteadmin-admin-createfolder-at-activator");
            }
        }

        return actionRels;
    }

    private boolean hasPermission(AccessControlManager acm, Resource resource, String privilege) {
        if (acm != null) {
            try {
                Privilege p = acm.privilegeFromName(privilege);
                return acm.hasPrivileges(resource.getPath(), new Privilege[]{p});
            } catch (RepositoryException ignore) {
            }
        }
        return false;
    }

    private String getFolderTitle(Resource folder) {
        ValueMap vm = ResourceUtil.getValueMap(folder);
        return vm.get("jcr:content/jcr:title", vm.get("jcr:title", folder.getName()));
    }

    private boolean hasChildren(Resource resource) {
	    for (Iterator<Resource> it = resource.listChildren(); it.hasNext(); ) {
	        Resource r = it.next();
	        
	        if (r.getName().startsWith("rep:") || r.getName().equals("jcr:content")) {
	            continue;
	        }
	        return true;
	    }
	    
	    return false;
	}

%>


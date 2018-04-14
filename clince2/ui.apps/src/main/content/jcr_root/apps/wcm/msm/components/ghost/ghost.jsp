<%@page session="false"%><%--
  Copyright 1997-2011 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Display a placeholder for deleted components in a LiveCopy

--%><%@ page contentType="text/html" pageEncoding="utf-8"%><%
%><%@ page import="com.day.cq.i18n.I18n,
                 com.day.cq.wcm.foundation.Placeholder" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
    if(editContext!=null) {
        editContext.getEditConfig().setEmpty(true);
    } //else WCM mode is "publish, so no need to display anything.
%><%
    String text = I18n.get(slingRequest, "Placeholder for a deleted inherited Component");
%>

 <% String pageName = currentPage.getName(); %>
 <%
      if(pageName.equals("no")){%>

<%}
else {%>
<%= Placeholder.getDefaultPlaceholder(slingRequest, text, "", "") %>
<%} %>
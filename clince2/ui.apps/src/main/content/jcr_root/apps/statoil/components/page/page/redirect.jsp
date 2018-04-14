<%@page session="false"
            contentType="text/html; charset=utf-8"
            import="com.day.cq.commons.Doctype,
                    com.day.cq.wcm.api.WCMMode,
					java.util.Arrays,
					org.apache.sling.commons.json.JSONObject,
	   				org.apache.sling.commons.json.JSONArray,
                    com.day.cq.wcm.foundation.ELEvaluator" %><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %><%
%><cq:defineObjects/>
<%
    String location = properties.get("redirectTarget", "");
	String isClientContextDisabled = properties.get("disableClientContext", "");
	String[] strRedirectArray = properties.get("redirects", String[].class);
    location = ELEvaluator.evaluate(location, slingRequest, pageContext);
    boolean wcmModeIsDisabled = WCMMode.fromRequest(request) == WCMMode.DISABLED;
    boolean wcmModeIsPreview = WCMMode.fromRequest(request) == WCMMode.PREVIEW;

	//Enabled page level targeting based on country code
	if(isClientContextDisabled != null && "false".equals(isClientContextDisabled) && strRedirectArray != null && ((wcmModeIsDisabled) || (wcmModeIsPreview))){
		String redirectPath;
		JSONArray allRedirects = new JSONArray(Arrays.asList(strRedirectArray));
        if(allRedirects != null){
            for(int i=0; i<allRedirects.length(); i++ ){
                JSONObject redirect = allRedirects.getJSONObject(i);
                %>
                <script type="text/javascript">
                       if('<%=redirect.getString("code")%>' === 'CQ_Analytics.ClientContext.get("statoilgeoloc/countryCode")'){
                            <%
                                redirectPath = redirect.getString("redirectpage");
                                if(!redirectPath.equals(currentPage.getPath())){
                                    redirectPath = redirect.getString("redirectpage") + ".html";
                                	response.sendRedirect(redirectPath);
                                }
                            %>
                        }
                </script>
          <%}
        }
    } else if ((location.length() > 0) && ((wcmModeIsDisabled) || (wcmModeIsPreview))) {		
        // check for recursion
        if (currentPage != null && !location.equals(currentPage.getPath()) && location.length() > 0) {
            // check for absolute path
            final int protocolIndex = location.indexOf(":/");
            final int queryIndex = location.indexOf('?');
            final String redirectPath;
            if ( protocolIndex > -1 && (queryIndex == -1 || queryIndex > protocolIndex) ) {
                redirectPath = location;
            } else {
                redirectPath = slingRequest.getResourceResolver().map(request, location) + ".html";
            }
            response.sendRedirect(redirectPath);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        return;
    }
%>

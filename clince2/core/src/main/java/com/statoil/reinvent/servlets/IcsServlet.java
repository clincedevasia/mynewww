package com.statoil.reinvent.servlets;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

@SlingServlet(paths = { "/services/ics" }, methods = { "GET" })
public class IcsServlet extends SlingAllMethodsServlet {

    private static final Logger logger = LoggerFactory.getLogger(LanguageServlet.class);

    @Override
    protected final void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            String summary = URLDecoder.decode(request.getParameter("summary"), "UTF-8");
            String description = URLDecoder.decode(request.getParameter("description"), "UTF-8");
            String location =  URLDecoder.decode(request.getParameter("location"), "UTF-8");
            String timeZone = URLDecoder.decode(request.getParameter("timeZone"), "UTF-8");
            String start =  URLDecoder.decode(request.getParameter("start"), "UTF-8");
            String end =  URLDecoder.decode(request.getParameter("end"), "UTF-8");      
            String fileName=summary.replaceAll("[, ]","_" ) + ".ics";
            response.setContentType("text/calendar; charset=utf-8");
            response.setHeader("Content-Disposition","attachment; filename=" + fileName);
            String newline = System.getProperty("line.separator");
            
            int  timeStartIndex = start.indexOf("T")+1;
            String startTime = start.substring(timeStartIndex);
            String endTime = end.substring(timeStartIndex);
            String[] icsArray;
            if(startTime.equals("000000")&&endTime.equals("000000")){
	             icsArray = new String[] {
	                "BEGIN:VCALENDAR",
	                "VERSION:2.0",
	                "BEGIN:VEVENT",
	                "DESCRIPTION:" + description,
	                "DTSTART:" + start,
	                "LOCATION:" + location,
	                "SUMMARY:" + summary,
	                "END:VEVENT",
	                "END:VCALENDAR"
	            };
            }else {
            	icsArray = new String[] {
                        "BEGIN:VCALENDAR",
                        "VERSION:2.0",
                        "BEGIN:VEVENT",
                        "DESCRIPTION:" + description,
                        "DTSTART:" + start,
                        "DTEND:"+end,
                        "LOCATION:" + location,
                        "SUMMARY:" + summary,
                        "END:VEVENT",
                        "END:VCALENDAR"
                    };
			}

            String ics = StringUtils.join(icsArray, newline);

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(ics.getBytes());
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }
}
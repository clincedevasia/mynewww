package com.statoil.reinvent.servlets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * The Resource Resolver in AEM has shortcomings when it comes to resolving URLs, so implemented mapping in this class.
 *
 * - Resource Resolver only works with http.
 * - Using URLs when using Resource Resolver in code does not work, but does (strangely) work on the Resource Resolver
 *   test page, e.g. http://[server]/system/console/jcrresolver.
 */
public class LanguageResourceResolver {

    static class SiteMeta {
        private String defaultLanguage;
        private String path;

        public SiteMeta(String defaultLanguage, String path) {
            this.defaultLanguage = defaultLanguage;
            this.path = path;
        }
    }

    private static HashMap<String, SiteMeta> sites = new HashMap<String, SiteMeta>()
    {
     
		private static final long serialVersionUID = 2387467551277400712L;
		
		{ put("beta.statoil.com",            new SiteMeta("en", "/content/statoil/")); }
        { put("www.statoil.com",             new SiteMeta("en", "/content/statoil/")); }
        { put("www.statoil.com.br",          new SiteMeta("pt", "/content/statoil-br/")); }
        { put("www.statoil.de",              new SiteMeta("de", "/content/statoil-de/")); }
        { put("www.statoil.nl",              new SiteMeta("nl", "/content/statoil-nl/")); }
        { put("www.statoilfondene.no",       new SiteMeta("no", "/content/Statoilfondene/")); }
        { put("www.statoilstorage.de",       new SiteMeta("de", "/content/statoilstorage-de/")); }
        { put("www.empirewind.com",          new SiteMeta("en", "/content/empirewind/")); }
        { put("beta-stage.statoil.com",      new SiteMeta("en", "/content/statoil/")); }
        { put("www-stage.statoil.com",       new SiteMeta("en", "/content/statoil/")); }
        { put("www-stage.statoil.com.br",    new SiteMeta("pt", "/content/statoil-br/")); }
        { put("www-stage.statoil.de",        new SiteMeta("de", "/content/statoil-de/")); }
        { put("www-stage.statoil.nl",        new SiteMeta("nl", "/content/statoil-nl/")); }
        { put("www-stage.statoilfondene.no", new SiteMeta("no", "/content/Statoilfondene/")); }
        { put("www-stage.statoilstorage.de", new SiteMeta("de", "/content/statoilstorage-de/")); }
        { put("www-stage.empirewind.com",    new SiteMeta("en", "/content/empirewind/")); }
        { put("localhost",                   new SiteMeta("en", "/content/statoil/")); }
    };

    public static String resolve(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String host = resolveHost(uri.getHost());
        String path = resolvePath(uri.getHost(), uri.getPath());

        return host + path;
    }

    /**
     * Maps a content path to a URL. Which URL to map to is dependent on the host (environment).
     *
     * @param host
     * @param path
     * @return
     */
    public static String map(String host, String path)
    {
        String[] values = StringUtils.split(path, '/');
        String sitePath = "/content/" + values[1] + "/";

        String page = StringUtils.substring(path, sitePath.length());

        switch (sitePath) {
            case "/content/statoil/":
                return formatAddress(host, "statoil.com", page);
            case "/content/statoil-br/":
                return formatAddress(host, "statoil.com.br", page);
            case "/content/statoil-de/":
                return formatAddress(host, "statoil.de", page);
            case "/content/statoilstorage-de/":
                return formatAddress(host, "statoilstorage.de", page);
            case "/content/statoil-nl/":
                return formatAddress(host, "statoil.nl", page);
            case "/content/Statoilfondene/":
                return formatAddress(host, "statoilfondene.no", page);
            case "/content/empirewind/":
                return formatAddress(host, "empirewind.com", page);
            default:
                throw new IllegalArgumentException("path: " + path);
        }
    }

    private static String resolvePath(String host, String path) {
        if (StringUtils.isNotBlank(path) && !path.equals("/"))
            return FilenameUtils.removeExtension(path.substring(1));

        return sites.get(host).defaultLanguage;
    }

    private static String resolveHost(String host) {
        return sites.get(host).path;
    }

    private static String formatAddress(String host, String domainName, String page)
    {
        String subDomain = host.startsWith("www-stage") ? "www-stage" : "www";

        return String.format("%s.%s/%s.html", subDomain, domainName, page);
    }
}


package org.vaadin.artur;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.server.VaadinServlet;

public class StaticFilesServingVaadinServlet extends VaadinServlet {
    /*
     * Stuff below copy pasted and slightly modified to get the servlet to serve
     * stuff from VAADIN/ when the servlet is mapped the way it is
     */

    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
//        System.out.println(request.getPathInfo() + " vs "
//                + request.getContextPath() + "/VAADIN/");
        if (isStaticResourceRequest(request)) {
            serveStaticResource(request, response);
            return;
        }
        super.service(request, response);
    }

    private URL findResourceURL(String filename, ServletContext sc)
            throws MalformedURLException {
        URL resourceUrl = sc.getResource(filename);
        if (resourceUrl == null) {
            // try if requested file is found from classloader

            // strip leading "/" otherwise stream from JAR wont work
            if (filename.startsWith("/")) {
                filename = filename.substring(1);
            }

            resourceUrl = getService().getClassLoader().getResource(filename);
        }
        return resourceUrl;
    }

    private static Logger getLogger() {
        return Logger.getLogger(VaadinConsoleServlet.class.getName());
    }

    private boolean browserHasNewestVersion(HttpServletRequest request,
            long resourceLastModifiedTimestamp) {
        if (resourceLastModifiedTimestamp < 1) {
            // We do not know when it was modified so the browser cannot have an
            // up-to-date version
            return false;
        }
        /*
         * The browser can request the resource conditionally using an
         * If-Modified-Since header. Check this against the last modification
         * time.
         */
        try {
            // If-Modified-Since represents the timestamp of the version cached
            // in the browser
            long headerIfModifiedSince = request
                    .getDateHeader("If-Modified-Since");

            if (headerIfModifiedSince >= resourceLastModifiedTimestamp) {
                // Browser has this an up-to-date version of the resource
                return true;
            }
        } catch (Exception e) {
            // Failed to parse header. Fail silently - the browser does not have
            // an up-to-date version in its cache.
        }
        return false;
    }

    private void serveStaticResource(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String filename = request.getPathInfo();
        ServletContext sc = getServletContext();
        URL resourceUrl = findResourceURL(filename, sc);
        if (resourceUrl == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // security check: do not permit navigation out of the VAADIN
        // directory
        if (!isAllowedVAADINResourceUrl(request, resourceUrl)) {
            getLogger()
                    .log(Level.INFO,
                            "Requested resource [{0}] not accessible in the VAADIN directory or access to it is forbidden.",
                            filename);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Find the modification timestamp
        long lastModifiedTime = 0;
        URLConnection connection = null;
        try {
            connection = resourceUrl.openConnection();
            lastModifiedTime = connection.getLastModified();
            // Remove milliseconds to avoid comparison problems (milliseconds
            // are not returned by the browser in the "If-Modified-Since"
            // header).
            lastModifiedTime = lastModifiedTime - lastModifiedTime % 1000;

            if (browserHasNewestVersion(request, lastModifiedTime)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        } catch (Exception e) {
            // Failed to find out last modified timestamp. Continue without it.
            getLogger()
                    .log(Level.FINEST,
                            "Failed to find out last modified timestamp. Continuing without it.",
                            e);
        } finally {
            try {
                // Explicitly close the input stream to prevent it
                // from remaining hanging
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4257700
                InputStream is = connection.getInputStream();
                if (is != null) {
                    is.close();
                }
            } catch (FileNotFoundException e) {
                // Not logging when the file does not exist.
            } catch (IOException e) {
                getLogger().log(Level.INFO,
                        "Error closing URLConnection input stream", e);
            }
        }

        // Set type mime type if we can determine it based on the filename
        final String mimetype = sc.getMimeType(filename);
        if (mimetype != null) {
            response.setContentType(mimetype);
        }

        // Provide modification timestamp to the browser if it is known.
        if (lastModifiedTime > 0) {
            response.setDateHeader("Last-Modified", lastModifiedTime);

            String cacheControl = "public, max-age=0, must-revalidate";
            int resourceCacheTime = getCacheTime(filename);
            if (resourceCacheTime > 0) {
                cacheControl = "max-age=" + String.valueOf(resourceCacheTime);
            }
            response.setHeader("Cache-Control", cacheControl);
        }

        writeStaticResourceResponse(request, response, resourceUrl);
    }

    @Override
    protected boolean isStaticResourceRequest(HttpServletRequest request) {
        String pi = request.getPathInfo();
        if (pi != null && pi.startsWith("/VAADIN/"))
            return true;
        return false;
    }

}

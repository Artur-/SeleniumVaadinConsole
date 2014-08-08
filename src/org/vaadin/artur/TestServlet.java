package org.vaadin.artur;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter w = resp.getWriter();
        writeLine(w,"Request.contextPath: "+req.getContextPath());
        writeLine(w,"Request.pathInfo: "+req.getPathInfo());
        writeLine(w,"Request.pathTranslated: "+req.getPathTranslated());
        writeLine(w,"Request.requestURI: "+req.getRequestURI());
        writeLine(w,"Request.requestURL: "+req.getRequestURL());
        writeLine(w,"Request.servletPath: "+req.getServletPath());
    }

    private void writeLine(PrintWriter w, String string) {
        w.write(string);
        w.write("\n");
        
    }
}

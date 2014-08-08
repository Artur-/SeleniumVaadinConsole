package org.vaadin.artur;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openqa.grid.internal.Registry;

import com.vaadin.server.DefaultDeploymentConfiguration;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

public class VaadinConsoleServlet extends RegistryBasedVaadinServlet {
    public VaadinConsoleServlet() {
        this(null);
    }

    public VaadinConsoleServlet(Registry registry) {
        super(registry);
    }

    @Override
    protected DeploymentConfiguration createDeploymentConfiguration(
            Properties initParameters) {
        initParameters.put("ui", VaadinConsoleUI.class.getName());
        initParameters.put(VaadinServlet.PARAMETER_VAADIN_RESOURCES, ".");
        return new DefaultDeploymentConfiguration(getClass(), initParameters);
    }

}

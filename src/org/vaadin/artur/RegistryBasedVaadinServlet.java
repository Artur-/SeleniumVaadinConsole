package org.vaadin.artur;

import org.openqa.grid.internal.Registry;

import com.vaadin.server.VaadinServlet;

public abstract class RegistryBasedVaadinServlet extends StaticFilesServingVaadinServlet {
    private Registry registry;

    public RegistryBasedVaadinServlet(Registry registry) {
        this.registry = registry;
    }

    protected Registry getRegistry() {
        if (registry == null) {
            registry = (Registry) getServletContext()
                    .getAttribute(Registry.KEY);
        }

        return registry;
    }

    public static Registry findRegistry() {
        if (VaadinServlet.getCurrent() instanceof RegistryBasedVaadinServlet)
            return ((RegistryBasedVaadinServlet) VaadinServlet.getCurrent())
                    .getRegistry();
        else
            return (Registry) VaadinServlet.getCurrent().getServletContext()
                    .getAttribute(Registry.KEY);
    }
}

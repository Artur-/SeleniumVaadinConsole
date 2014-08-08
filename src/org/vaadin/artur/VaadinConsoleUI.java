package org.vaadin.artur;

import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.annotations.Theme;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("seleniumvaadinconsole")
public class VaadinConsoleUI extends UI {

    // @WebServlet(value = "/*", asyncSupported = true)
    // @VaadinServletConfiguration(productionMode = false, ui =
    // SeleniumvaadinconsoleUI.class)

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Selenium Grid Console");

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.addComponent(new Label("Selenium Grid Console running at "
                + getRegistry().getHub().getHost()));
        setContent(layout);

        for (RemoteProxy proxy : getRegistry().getAllProxies()) {
            layout.addComponent(createProxyPanel(proxy));
        }

        Button button = new Button("Click Me");
        button.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                layout.addComponent(new Label("Thank you for clicking"));
            }
        });
        layout.addComponent(button);
    }

    private Component createProxyPanel(RemoteProxy proxy) {
        Panel p = new Panel(proxy.getRemoteHost().toString());
        p.setSizeUndefined();
        if (((DefaultRemoteProxy) proxy).isDown())
            p.setCaption(p.getCaption() + " (UNREACHABLE)");

        GridLayout gl = new GridLayout(2, 1);
        gl.setSpacing(true);
        p.setContent(gl);
        for (TestSlot slot : proxy.getTestSlots()) {
            gl.addComponent(new Image(null, new ThemeResource(getIcon(slot))));
            gl.addComponent(new Label(slot.getCapabilities()
                    .get(CapabilityType.BROWSER_NAME).toString()));
        }
        return p;
    }

    private String getIcon(TestSlot slot) {
        return "icons/"+BrowserNameUtils.consoleIconName(
                new DesiredCapabilities(slot.getCapabilities()), getRegistry())
                + ".png";
    }

    private Registry getRegistry() {
        return RegistryBasedVaadinServlet.findRegistry();
    }

}
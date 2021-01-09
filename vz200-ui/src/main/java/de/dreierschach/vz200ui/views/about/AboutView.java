package de.dreierschach.vz200ui.views.about;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.main.MainView;

@Route(value = "about", layout = MainView.class)
@PageTitle("About")
public class AboutView extends Div {

    public AboutView() {
        setId("about-view");
        ComponentFactory.StyledText text = ComponentFactory.styledText(
                //@formatter:off
                "<h1>VZ200-UI</h1>" +
                "<p>Mit dieser UI kannst du den VZ200-Jemu-Emulator fernsteuern.</p>" +
                "<p>(c) 2021 Christian Wahlmann"
                //@formatter:on
        );
        this.add(text);
    }

}

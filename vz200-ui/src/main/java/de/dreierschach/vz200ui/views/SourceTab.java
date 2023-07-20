package de.dreierschach.vz200ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import de.dreierschach.vz200ui.util.ComponentFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.function.Consumer;

public class SourceTab extends Tab {
    private final Button closeButton;
    private final Button editButton;
    private Span nameLabel;
    private String name;
    private boolean changed;
    private Consumer<SourceTab> nameChangeListener = tab -> {
    };

    public SourceTab() {
        changed = false;
        name = "";
        closeButton = new Button(VaadinIcon.CLOSE_SMALL.create());
        editButton = new Button(VaadinIcon.PENCIL.create());
        editButton.addClickListener(event -> {
            ComponentFactory.singleInput("Edit name", name, "name",
                    newName -> {
                        if (StringUtils.isEmpty(newName)) {
                            return;
                        }
                        withName(newName.toUpperCase(Locale.ROOT));
                    },
                    () -> {
                    });

        });
        nameLabel = new Span("NEW");
        add(nameLabel, getSpacer(), editButton, closeButton);
    }

    public SourceTab withName(String name) {
        this.name = name;
        this.nameLabel.setText(name + (changed ? "*" : ""));
        nameChangeListener.accept(this);
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isChanged() {
        return changed;
    }

    public SourceTab withChanged(boolean changed) {
        this.changed = changed;
        this.nameLabel.setText(name + (changed ? "*" : ""));
        return this;
    }

    public SourceTab withNameChangeListener(Consumer<SourceTab> nameChangeListener) {
        this.nameChangeListener = nameChangeListener;
        return this;
    }

    public Span getSpacer() {
        Span l = new Span();
        l.getElement().setProperty("innerHTML", "&nbsp;&nbsp;");
        return l;
    }

}

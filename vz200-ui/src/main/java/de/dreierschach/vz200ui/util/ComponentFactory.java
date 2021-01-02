package de.dreierschach.vz200ui.util;


import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

public class ComponentFactory {
    private ComponentFactory() {
    }

    public static Label spacer() {
        Label label = new Label();
        label.getElement().setProperty("innerHTML", "&nbsp;");
        return label;
    }

    public static void notification(String message, String style, int duration) {
        Div div = new Div();
        div.addClassNames(style);
        div.setText(message);
        Notification notification = new Notification(div);
        notification.setDuration(duration);
        notification.open();
    }

    public static void info(String message) {
        notification(message, "info", 1500);
    }

    public static void warning(String message) {
        notification(message, "warning", 3000);
    }

    public static void danger(String message) {
        notification(message, "danger", 5000);
    }

    public static void confirm(String message, String confirmLabel, String declineLabel, Runnable onConfirm, Runnable onDecline) {
        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Button confirmButton = new Button(confirmLabel, VaadinIcon.CHECK.create(), event -> {
            dialog.close();
            onConfirm.run();
        });

        Button declineButton = new Button(declineLabel, VaadinIcon.CLOSE.create(), event -> {
            dialog.close();
            onDecline.run();
        });
        dialog.add(new Label(message), spacer(), confirmButton, declineButton);
        dialog.open();
    }

    public static <T extends HasElement> T withTooltip(T hasElement, String tooltip) {
        hasElement.getElement().setProperty("title", tooltip);
        return hasElement;
    }
}

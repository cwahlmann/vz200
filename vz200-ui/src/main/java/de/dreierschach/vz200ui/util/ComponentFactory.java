package de.dreierschach.vz200ui.util;


import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
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

    public static Label ruler() {
        Label label = new Label();
        label.getElement().setProperty("innerHTML", "<hr>");
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

    public static AceEditor aceEditor(String placeholder, AceTheme theme, int minLines, int maxlines, int maxWidth) {
        AceEditor editor = new AceEditor();
        editor.setPlaceholder(placeholder);
        editor.setTheme(theme);
        editor.setMinlines(minLines);
        editor.setMaxlines(maxlines);
        editor.setMaxWidth(maxWidth + "em");

        editor.setWrap(false);
        editor.setMode(AceMode.python);
        editor.setFontSize(15);
        editor.setShowInvisibles(false);
        editor.setShowGutter(false);
        editor.setShowPrintMargin(false);
        editor.setDisplayIndentGuides(false);
        editor.setUseWorker(false);
        editor.setSofttabs(true);
        editor.setTabSize(4);
        editor.setAutoComplete(true);
        editor.setHighlightActiveLine(false);
        editor.setHighlightSelectedWord(false);
        editor.setWidth("100%");
        editor.setHeight("100%");
        editor.setInitialFocus(true);
        editor.setReadOnly(false);
        return editor;
    }
}

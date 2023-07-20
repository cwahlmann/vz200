package de.dreierschach.vz200ui.util;


import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;

import java.util.function.Consumer;

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
        notification(message, "info", 3000);
    }

    public static void warning(String message) {
        notification(message, "warning", 4500);
    }

    public static void danger(String message) {
        notification(message, "danger", 6000);
    }

    public static void confirm(String message, String confirmLabel, String declineLabel, Runnable onConfirm, Runnable onDecline) {
        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Button confirmButton = new Button(confirmLabel, VaadinIcon.CHECK.create(), event -> {
            dialog.close();
            onConfirm.run();
        });
        confirmButton.addClickShortcut(Key.ENTER);

        Button declineButton = new Button(declineLabel, VaadinIcon.CLOSE.create(), event -> {
            dialog.close();
            onDecline.run();
        });
        declineButton.addClickShortcut(Key.ESCAPE);

        dialog.add(new VerticalLayout(new Label(message), new HorizontalLayout(confirmButton, declineButton)));
        dialog.open();
    }

    public static void singleInput(String message, String placeholder, Consumer<String> onOk, Runnable onCancel) {
        singleInput(message, "", placeholder, onOk, onCancel);
    }

    public static void singleInput(String message, String value, String placeholder, Consumer<String> onOk, Runnable onCancel) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        TextField inputField = new TextField("", placeholder);
        inputField.setValue(value);
        inputField.focus();

        Button okButton = new Button("ok", VaadinIcon.CHECK.create(), event -> {
            dialog.close();
            onOk.accept(inputField.getValue());
        });
        okButton.addClickShortcut(Key.ENTER);

        Button cancelButton = new Button("cancel", VaadinIcon.CLOSE.create(), event -> {
            dialog.close();
            onCancel.run();
        });
        cancelButton.addClickShortcut(Key.ESCAPE);

        dialog.add(new VerticalLayout(new Label(message), inputField, new HorizontalLayout(okButton, cancelButton)));
        dialog.open();
    }

    static class PrintDialog extends Dialog {
        private String text;
        private TextArea textArea;

        public PrintDialog(String text) {
            super();
            this.text = text;
            textArea = new TextArea();
            textArea.setSizeFull();
            textArea.addClassNames("print-style");
            textArea.addValueChangeListener(event -> print());
            add(textArea);
            setSizeFull();
        }

        private void print() {
            UI.getCurrent().access(() -> UI.getCurrent().getPage().executeJs("print();"));
        }

        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
            textArea.setValue(text);
        }
    }

    public static Dialog openPrintDialog(String text) {
        Dialog dialog = new PrintDialog(text);
        dialog.open();
        return dialog;
    }

    public static <T extends Component> T withTooltip(T hasElement, String tooltip) {
        hasElement.getElement().setProperty("title", tooltip);
        return hasElement;
    }

    public static AceEditor aceEditor(String placeholder, AceTheme theme, int cols, int rows) {
        AceEditor editor = new AceEditor();
        editor.setPlaceholder(placeholder);
        editor.setTheme(theme);
        editor.setHeight(rows + "em");
        editor.setWidth(cols + "em");
        editor.setWrap(false);
        editor.setMode(AceMode.python);
        editor.setFontSize(15);
        editor.setShowInvisibles(false);
        editor.setShowGutter(false);
        editor.setShowPrintMargin(true);
        editor.setDisplayIndentGuides(false);
        editor.setUseWorker(false);
        editor.setSofttabs(true);
        editor.setTabSize(4);
        editor.setAutoComplete(true);
        editor.setHighlightActiveLine(false);
        editor.setHighlightSelectedWord(false);
        editor.setInitialFocus(true);
        editor.setReadOnly(false);
        return editor;
    }

    public static StyledText styledText(String text) {
        return new StyledText(text);
    }

    /**
     * @author Syam Pillai
     * @link https://vaadin.com/forum/thread/17072019/17508926
     */
    public static class StyledText extends Composite<Span> implements HasText {

        private final Span content = new Span();
        private String text;

        public StyledText(String htmlText) {
            setText(htmlText);
        }

        @Override
        protected Span initContent() {
            return content;
        }

        @Override
        public void setText(String htmlText) {
            if (htmlText == null) {
                htmlText = "";
            }
            if (htmlText.equals(text)) {
                return;
            }
            text = htmlText;
            content.removeAll();
            content.add(new Html("<span>" + htmlText + "</span>"));
        }

        @Override
        public String getText() {
            return text;
        }
    }
}

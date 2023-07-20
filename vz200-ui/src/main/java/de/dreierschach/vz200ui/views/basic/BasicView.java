package de.dreierschach.vz200ui.views.basic;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
import de.dreierschach.vz200ui.util.GraphicCharUtil;
import de.dreierschach.vz200ui.views.View;
import de.dreierschach.vz200ui.views.main.MainView;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceTheme;
import org.vaadin.stefan.LazyDownloadButton;

import java.io.InputStream;
import java.util.function.Supplier;


@Route(value = "basic", layout = MainView.class)
@PageTitle("Basic")
@CssImport("./styles/views/basic/basic-view.css")
@VaadinSessionScope
public class BasicView extends View<BasicPresenter> {

    static final String UPLOAD_ID = "UPLOAD_ID";

    AceEditor sourceEditor;

    Button installButton;
    Button runButton;
    Button downloadButton;
    Button resetButton;
    Checkbox changedCheckbox;
    TextField nameField;

    LazyDownloadButton saveButton;
    Supplier<String> filenameSupplier = () -> "source.bas";
    Supplier<InputStream> inputStreamSupplier = () -> null;
    Runnable onDownload = () -> {
    };

    LazyDownloadButton convertToVzButton;
    Supplier<String> convertFilenameSupplier = () -> "default.vz";
    Supplier<InputStream> convertInputStreamSupplier = () -> null;
    Runnable onDownloadVz = () -> {
    };

    ConfirmedUpload confirmedUpload;

    @Override
    protected String getViewId() {
        return "basic-view";
    }

    protected void createContent() {
        sourceEditor = ComponentFactory.aceEditor("enter your basic code here", AceTheme.ambiance, 44, 25);

        nameField = new TextField("", "", "Name");

        installButton = new Button("Install", VaadinIcon.LAPTOP.create());
        runButton = new Button("Run", VaadinIcon.LAPTOP.create());
        downloadButton = new Button("Download", VaadinIcon.LAPTOP.create());
        resetButton = new Button("Reset", VaadinIcon.LAPTOP.create());
        saveButton = new LazyDownloadButton("Save", VaadinIcon.DOWNLOAD.create(), () -> filenameSupplier.get(),
                () -> inputStreamSupplier.get());
        saveButton.addDownloadStartsListener(event -> this.onDownload.run());
        convertToVzButton = new LazyDownloadButton("Save as VZ", VaadinIcon.DOWNLOAD.create(),
                () -> convertFilenameSupplier.get(), () -> convertInputStreamSupplier.get());
        convertToVzButton.addDownloadStartsListener(event -> this.onDownloadVz.run());
        changedCheckbox = new Checkbox("Changed", false);
        changedCheckbox.setReadOnly(true);


        HorizontalLayout bar = new HorizontalLayout(saveButton, changedCheckbox);
        bar.setAlignItems(Alignment.BASELINE);

        add(nameField, installButton, runButton, downloadButton, resetButton, convertToVzButton, bar);

        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.add(createGraphicButton("q", 0x8e, Key.KEY_Q));
        row.add(createGraphicButton("w", 0x8d, Key.KEY_W));
        row.add(createGraphicButton("e", 0x8b, Key.KEY_E));
        row.add(createGraphicButton("r", 0x87, Key.KEY_R));
        row.add(createGraphicButton("t", 0x8c, Key.KEY_T));
        row.add(createGraphicButton("z", 0x80, Key.KEY_Y));
        row.add(createGraphicButton("u", 0x8a, Key.KEY_U));
        row.add(createGraphicButton("i", 0x85, Key.KEY_I));
        row.add(createGraphicButton("a", 0x81, Key.KEY_A));
        row.add(createGraphicButton("s", 0x82, Key.KEY_S));
        row.add(createGraphicButton("d", 0x84, Key.KEY_D));
        row.add(createGraphicButton("f", 0x88, Key.KEY_F));
        row.add(createGraphicButton("g", 0x89, Key.KEY_G));
        row.add(createGraphicButton("h", 0x86, Key.KEY_H));
        row.add(createGraphicButton("j", 0x8f, Key.KEY_J));
        row.add(createGraphicButton("y", 0x83, Key.KEY_Z));
        add(row);

        addAndExpand(sourceEditor);

        setVerticalComponentAlignment(Alignment.END, nameField, installButton, runButton, downloadButton, resetButton,
                saveButton, changedCheckbox, sourceEditor);

        confirmedUpload = new ConfirmedUpload().withMessage("Overwrite recent changes?").withConfirmCaption("Overwrite")
                .withDeclineCaption("Cancel").withButtonCaption("Load").withDropCaption("Drop file")
                .withIcon(VaadinIcon.FILE);
        addUpload(UPLOAD_ID, bar, 0, confirmedUpload);
    }

    private Component createGraphicButton(String key, int vzCode, Key keycode) {
        Character c = GraphicCharUtil.GRAPHIC_CHARS.get(vzCode).getUnicodeChar();
        var button = new Button("" + c);
        button.getElement().setProperty("title", "[AltGr] + [Shift] + [" + key + "]");
        button.addClassNames("graphic-button-style");
        button.addClickListener(event -> sourceEditor.addTextAtCurrentPosition(String.valueOf(c)));
        button.addClickShortcut(keycode, KeyModifier.ALT_GRAPH, KeyModifier.SHIFT);
        VerticalLayout result = new VerticalLayout();
        result.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        result.add(new Label(key), button);
        result.setPadding(false);
        result.setSpacing(false);
        result.setSizeUndefined();
        return result;
    }

    public void setDownloadSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.filenameSupplier = filenameSupplier;
        this.inputStreamSupplier = inputStreamSupplier;
        this.onDownload = onDownload;
    }

    public void setConvertSupplier(Supplier<String> filenameSupplier, Supplier<InputStream> inputStreamSupplier, Runnable onDownload) {
        this.convertFilenameSupplier = filenameSupplier;
        this.convertInputStreamSupplier = inputStreamSupplier;
        this.onDownloadVz = onDownload;
    }
}

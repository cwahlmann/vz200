package de.dreierschach.vz200ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import de.dreierschach.vz200ui.util.ConfirmedUpload;
import de.dreierschach.vz200ui.util.ConfirmedUploadElement;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

abstract public class View<T extends Presenter<?>> extends HorizontalLayout {

    static class ConfirmedUploadButton {
        private ConfirmedUpload upload = null;
        private ConfirmedUploadElement uploadElement = null;
        private int position = 0;
        private HasComponents container = null;
        private MemoryBuffer buffer = null;

        public ConfirmedUploadElement getUploadElement() {
            return uploadElement;
        }

        public int getPosition() {
            return position;
        }

        public HasComponents getContainer() {
            return container;
        }

        public MemoryBuffer getBuffer() {
            return buffer;
        }

        public ConfirmedUpload getUpload() {
            return upload;
        }

        public ConfirmedUploadButton withUploadElement(ConfirmedUploadElement uploadElement) {
            this.uploadElement = uploadElement;
            return this;
        }

        public ConfirmedUploadButton withPosition(int position) {
            this.position = position;
            return this;
        }

        public ConfirmedUploadButton withContainer(HasComponents container) {
            this.container = container;
            return this;
        }

        public ConfirmedUploadButton withBuffer(MemoryBuffer buffer) {
            this.buffer = buffer;
            return this;
        }

        public ConfirmedUploadButton withUpload(ConfirmedUpload upload) {
            this.upload = upload;
            return this;
        }
    }

    @Autowired
    private T presenter;

    private Map<String, ConfirmedUploadButton> uploadButtons = new HashMap<>();

    public View() {
        setId(getViewId());
        createContent();
    }

    abstract protected String getViewId();

    abstract protected void createContent();

    public void addUpload(String key, HasComponents confirmedUploadContainer, int confirmedUploadPosition, ConfirmedUpload confirmedUpload) {
        uploadButtons.put(key, new ConfirmedUploadButton().withUpload(confirmedUpload)
                                                          .withContainer(confirmedUploadContainer)
                                                          .withPosition(confirmedUploadPosition).withUploadElement(null)
                                                          .withBuffer(new MemoryBuffer()));
    }

    public InputStream getUploadStream(String key) {
        return Optional.ofNullable(uploadButtons.get(key)).map(ConfirmedUploadButton::getBuffer)
                       .map(MemoryBuffer::getInputStream).orElse(null);
    }

    public String getUploadFilename(String key) {
        return Optional.ofNullable(uploadButtons.get(key)).map(ConfirmedUploadButton::getBuffer)
                       .map(MemoryBuffer::getFileName).orElse("");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        uploadButtons.keySet().forEach(this::resetUploadButton);
    }

    @PostConstruct
    public void init() {
        presenter.bind(this);
    }

    public void resetUploadButton(String key) {
        ConfirmedUploadButton uploadButton = uploadButtons.get(key);
        if (uploadButton == null) {
            return;
        }
        if (uploadButton.getUpload() == null) {
            return;
        }
        if (uploadButton.getUploadElement() != null) {
            uploadButton.getContainer().remove(uploadButton.getUploadElement());
        }
        ConfirmedUploadElement element = new ConfirmedUploadElement(uploadButton.getBuffer(), uploadButton.getUpload());
        element.setUploadButton(new Button(uploadButton.getUpload().getButtonCaption()));
        element.setDropLabel(new Label(uploadButton.getUpload().getDropCaption()));
        element.setDropLabelIcon(uploadButton.getUpload().getIcon().create());
        uploadButton.withUploadElement(element);
        uploadButton.getContainer().addComponentAtIndex(uploadButton.getPosition(), uploadButton.getUploadElement());
    }

    public T getPresenter() {
        return presenter;
    }
}

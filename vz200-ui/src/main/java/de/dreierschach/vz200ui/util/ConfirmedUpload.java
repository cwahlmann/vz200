package de.dreierschach.vz200ui.util;

import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.function.Supplier;

public class ConfirmedUpload {
    private Runnable onConfirmed = () -> {
    };
    private Runnable onDeclined = () -> {
    };
    private Supplier<Boolean> needsConfirmation = () -> false;

    private String message = "Sure to upload this file?";
    private String confirmCaption = "OK";
    private String declineCaption = "Cancel";
    private String buttonCaption = "Upload";
    private String dropCaption = "Drop file here";
    private VaadinIcon icon = VaadinIcon.UPLOAD;

    public Runnable getOnConfirmed() {
        return onConfirmed;
    }

    public ConfirmedUpload withOnConfirmed(Runnable onConfirmed) {
        this.onConfirmed = onConfirmed;
        return this;
    }

    public Runnable getOnDeclined() {
        return onDeclined;
    }

    public ConfirmedUpload withOnDeclined(Runnable onDeclined) {
        this.onDeclined = onDeclined;
        return this;
    }

    public Supplier<Boolean> getNeedsConfirmation() {
        return needsConfirmation;
    }

    public ConfirmedUpload withNeedsConfirmation(Supplier<Boolean> needsConfirmation) {
        this.needsConfirmation = needsConfirmation;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ConfirmedUpload withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getConfirmCaption() {
        return confirmCaption;
    }

    public ConfirmedUpload withConfirmCaption(String confirmCaption) {
        this.confirmCaption = confirmCaption;
        return this;
    }

    public String getDeclineCaption() {
        return declineCaption;
    }

    public ConfirmedUpload withDeclineCaption(String declineCaption) {
        this.declineCaption = declineCaption;
        return this;
    }

    public String getButtonCaption() {
        return buttonCaption;
    }

    public ConfirmedUpload withButtonCaption(String buttonCaption) {
        this.buttonCaption = buttonCaption;
        return this;
    }

    public String getDropCaption() {
        return dropCaption;
    }

    public ConfirmedUpload withDropCaption(String dropCaption) {
        this.dropCaption = dropCaption;
        return this;
    }

    public VaadinIcon getIcon() {
        return icon;
    }

    public ConfirmedUpload withIcon(VaadinIcon icon) {
        this.icon = icon;
        return this;
    }
}

package de.dreierschach.vz200ui.views;

abstract public class Presenter<T extends View<?>> {
    protected T view;

    void bind(View<?> view) {
        this.view = (T) view;
        doBind();
    }

    abstract protected void doBind();
}

package org.onexus.ui.website.widgets.tableviewer.decorators.link;

import org.onexus.resource.api.ParameterKey;


public enum LinkDecoratorParameters implements ParameterKey {

    URL("url", "The URL where do you want to link, with fiels values as ${[field_id]}", false),
    TARGET("target", "The link target", true);

    private final String key;
    private final String description;
    private final boolean optional;

    private LinkDecoratorParameters(String key, String description, boolean optional) {
        this.key = key;
        this.description = description;
        this.optional = optional;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }


}


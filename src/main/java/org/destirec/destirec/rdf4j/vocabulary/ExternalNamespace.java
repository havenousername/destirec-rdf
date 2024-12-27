package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.base.AbstractNamespace;

public class ExternalNamespace extends AbstractNamespace {
    private final String prefix;
    private final String name;

    public ExternalNamespace(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getName() {
        return name;
    }
}

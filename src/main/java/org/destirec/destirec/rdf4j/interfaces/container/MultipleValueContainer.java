package org.destirec.destirec.rdf4j.interfaces.container;

import java.util.List;

public class MultipleValueContainer<Containarable> implements Container<Containarable> {
    private final List<Containarable> variables;

    public MultipleValueContainer(List<Containarable> variables) {
        this.variables = variables;
    }

    @Override
    public boolean isSingular() {
        return false;
    }

    @Override
    public Containarable getSingular() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot use getSingular from this class");
    }

    @Override
    public List<Containarable> getMultiple() {
        return variables;
    }
}

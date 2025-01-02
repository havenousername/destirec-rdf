package org.destirec.destirec.rdf4j.interfaces.container;

import java.util.List;

public class SingularValueContainer<Containarable> implements Container<Containarable> {
    private final Containarable variable;

    public SingularValueContainer(Containarable variable) {
        this.variable = variable;
    }

    @Override
    public boolean isSingular() {
        return true;
    }

    @Override
    public Containarable getSingular() {
        return variable;
    }

    @Override
    public List<Containarable> getMultiple() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot use getMultiple from this class");
    }
}

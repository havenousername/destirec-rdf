package org.destirec.destirec.rdf4j.interfaces.container;

import java.util.List;

public interface Container<Containarable> {
    boolean isSingular();
    Containarable getSingular();

    List<Containarable> getMultiple();
}

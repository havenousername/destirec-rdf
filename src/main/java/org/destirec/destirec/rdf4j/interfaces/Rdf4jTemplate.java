package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Rdf4jTemplate {
     <T> T applyToConnection(final Function<RepositoryConnection, T> fun);
    void consumeConnection(final Consumer<RepositoryConnection> fun);
}

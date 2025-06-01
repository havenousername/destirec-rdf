package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.interfaces.Rdf4jTemplate;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class ThreadSafeRdf4JTemplateService implements Rdf4jTemplate {
    private final RDF4JTemplate rdf4JTemplate;
    public final Lock writeLock;

    public ThreadSafeRdf4JTemplateService(RDF4JTemplate rdf4JTemplate) {
        this.rdf4JTemplate = rdf4JTemplate;
        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    public <T> T applyToConnection(Function<RepositoryConnection, T> fun) {
        try {
            writeLock.lock();
            return rdf4JTemplate.applyToConnection(fun);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void consumeConnection(Consumer<RepositoryConnection> fun) {
        try {
            writeLock.lock();
            rdf4JTemplate.consumeConnection(fun);
        } finally {
            writeLock.unlock();
        }
    }
}

package org.destirec.destirec.utils;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;

public record ShortRepositoryInfo(boolean isRemote, Repository repository) {
    public String getLocation() {
        if (repository instanceof HTTPRepository) {
            return ((HTTPRepository) repository).getRepositoryURL();
        }
        return repository.getDataDir().getAbsolutePath();
    }

}

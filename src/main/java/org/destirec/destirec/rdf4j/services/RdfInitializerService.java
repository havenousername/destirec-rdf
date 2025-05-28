package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.version.VersionDao;
import org.destirec.destirec.rdf4j.version.VersionDto;
import org.destirec.destirec.rdf4j.version.VersionConfig;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class RdfInitializerService {
    private final VersionDao versionDao;

    public RdfInitializerService(
            VersionDao versionDao
    ) {
        this.versionDao = versionDao;
    }


    public IRI initializeRdfVersion() {
        VersionDto versionDto = versionDao
                .getDtoCreator()
                .create(Map.of(VersionConfig.Fields.VERSION, "1"));
        return versionDao.saveAndReturnId(versionDto);
    }

    public boolean hasVersion() {
        var versions = versionDao.list();

        return !versions.isEmpty();
    }

    public VersionDto version() {
        var versions = versionDao.list();

        return versions.getFirst();
    }
}

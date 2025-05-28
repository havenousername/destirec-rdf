package org.destirec.destirec.rdf4j.poi;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Getter
@Repository
public class POIDao extends GenericDao<POIConfig.Fields, POIDto> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final FeatureDao featureDao;

    public POIDao(
            RDF4JTemplate rdf4JTemplate,
            POIConfig configFields,
            POIMigration migration,
            POIDtoCreator dtoCreator,
            FeatureDao featureDao,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
        this.featureDao = featureDao;
    }


    @Override
    public POIDtoCreator getDtoCreator() {
        return (POIDtoCreator)super.getDtoCreator();
    }
}

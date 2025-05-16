package org.destirec.destirec.rdf4j.region.feature;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FeatureDao extends GenericDao<FeatureConfig.Fields, FeatureDto> {
    public FeatureDao(
            RDF4JTemplate rdf4JTemplate,
            FeatureConfig configFields,
            FeatureMigration migration,
            FeatureDtoCreator dtoCreator,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
    }

    @Override
    public FeatureDtoCreator getDtoCreator() {
        return (FeatureDtoCreator) super.getDtoCreator();
    }
}

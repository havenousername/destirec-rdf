package org.destirec.destirec.rdf4j.region.cost;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CostDao extends GenericDao<CostConfig.Fields, CostDto> {
    public CostDao(
            RDF4JTemplate rdf4JTemplate,
            CostConfig configFields,
            CostMigration migration,
            CostDtoCreator dtoCreator,
            DestiRecOntology ontology
            ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
    }

    @Override
    public CostDtoCreator getDtoCreator() {
        return (CostDtoCreator) super.getDtoCreator();
    }

    @Override
    public String getReadQuery() {
        return super.getReadQuery();
    }

    public final CostDto saveNoId(CostDto input) {
        IRI id = this.getInputId(input);
        this.saveAndReturnId(input, id);
        return new CostDto(id, input.getHasScore(), input.isActive(), input.getCostPerWeek(), input.getBudgetLevel());
    }
}

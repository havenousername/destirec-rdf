package org.destirec.destirec.rdf4j.preferences;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.utils.rdfDictionary.PreferenceNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Getter
@Repository
public class PreferenceDao extends GenericDao<PreferenceConfig.Fields, PreferenceDto> {
    private final CostDao costDao;
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    public PreferenceDao(
            RDF4JTemplate rdf4JTemplate,
            PreferenceConfig modelFields,
            PreferenceMigration migration,
            PreferenceDtoCreator dtoCreator,
            DestiRecOntology ontology, CostDao costDao, FeatureDao featureDao, MonthDao monthDao
    ) {
        super(rdf4JTemplate, modelFields, migration, dtoCreator, ontology);
        this.costDao = costDao;
        this.featureDao = featureDao;
        this.monthDao = monthDao;
    }


    @Override
    public String getReadQuery() {
        return super.getReadQuery();
    }

    @Override
    public PreferenceDtoCreator getDtoCreator() {
        return (PreferenceDtoCreator) super.getDtoCreator();
    }

    public Optional<PreferenceDto> getByAuthor(IRI author) {
        IRI preferenceId = getByAuthorId(author);
        if (preferenceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getById(preferenceId));
    }

    public IRI getByAuthorId(IRI author) {
        String query = getByAuthorQuery(migration.getResource(), author);
        var result = getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_BY_AUTHOR_QUERY", () -> query)
                .evaluateAndConvert()
                .toStream()
                .findFirst();

        return result.map(bindings ->
                (IRI) bindings.getBinding("preferenceId").getValue()).orElse(null);
    }

    protected String getByAuthorQuery(RdfResource graph, IRI author) {
        Variable preferenceId = SparqlBuilder.var("preferenceId");
        return Queries.SELECT(preferenceId)
                .where(GraphPatterns.tp(preferenceId, PreferenceNames.Properties.PREFERENCE_AUTHOR, author))
                .getQueryString();
    }
}

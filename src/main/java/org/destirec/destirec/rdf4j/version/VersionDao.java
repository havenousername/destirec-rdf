package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VersionDao extends GenericDao<VersionConfig.Fields, VersionDto> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public VersionDao(
            RDF4JTemplate rdf4JTemplate,
            VersionConfig model,
            SchemaVersionMigration migration,
            VersionDtoCreator creator,
            DestiRecOntology ontology) {
        super(rdf4JTemplate, model, migration, creator, ontology);
    }

    public void saveRegionVersion(IRI from, int versionNumber) {
        getRdf4JTemplate().applyToConnection(connection -> {
            IRI subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "region");
            TriplePattern version = GraphPatterns.tp(subject, RDF.TYPE, TopOntologyNames.Classes.VERSION.rdfIri());
            TriplePattern fromSource = GraphPatterns.tp(subject, DC.SOURCE, from);
            TriplePattern versionId = GraphPatterns.tp(subject, TopOntologyNames.Properties.HAS_VERSION.rdfIri(), valueFactory.createLiteral(versionNumber));


            connection.begin();
            ModifyQuery insertQuery = Queries.INSERT(version, fromSource, versionId);
            connection.prepareUpdate(insertQuery.getQueryString()).execute();
            connection.commit();

            return getRegionVersion();
        });
    }


    public void savePOIVersion(List<IRI> from, int versionNumber) {
        getRdf4JTemplate().applyToConnection(connection -> {
            IRI subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "poi");
            List<TriplePattern> patterns = new ArrayList<>();

            TriplePattern version = GraphPatterns.tp(subject, RDF.TYPE, TopOntologyNames.Classes.VERSION.rdfIri());
            patterns.add(version);

            for (IRI fromSource : from) {
                patterns.add(GraphPatterns.tp(subject, DC.SOURCE, fromSource));
            }

            version.andHas(TopOntologyNames.Properties.HAS_VERSION.rdfIri(), versionNumber);

            connection.begin();
            ModifyQuery insertQuery = Queries.INSERT(patterns.toArray(TriplePattern[]::new));
            connection.prepareUpdate(insertQuery.getQueryString()).execute();
            connection.commit();

            return getRegionVersion();
        });
    }

    public boolean hasPOIVersion(int version) {
        Resource subject = valueFactory.createIRI(configFields.getResourceLocation() + "poi");

        return checkVersionStatements(version, subject);

    }

    public IRI getRegionVersion() {
        Variable regionVersion = SparqlBuilder.var("regionVersion");
        Variable from = SparqlBuilder.var("from");
        SelectQuery query = Queries.SELECT(regionVersion);
        query.where(
                regionVersion.isA(TopOntologyNames.Classes.VERSION.rdfIri())
                        .andHas(DC.SOURCE, from)
        ).limit(1);

        return getRdf4JTemplate().applyToConnection(connection -> {
            try {
                TupleQueryResult res = connection.prepareTupleQuery(query.getQueryString()).evaluate();
                if (res.hasNext()) {
                    BindingSet binding = res.next();
                    return (IRI)binding.getValue(regionVersion.getVarName());
                }
            } catch (QueryEvaluationException exception) {
                logger.error("Cannot find region version");
            }
            return null;
        });
    }

    public boolean hasRegionVersion(int version) {
        Resource subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "region");
        return checkVersionStatements(version, subject);
    }

    private boolean checkVersionStatements(int version, Resource subject) {
        Statement typeStatement = valueFactory.createStatement(
                subject,
                RDF.TYPE,
                TopOntologyNames.Classes.VERSION.rdfIri()
        );
        Statement versionStatement = valueFactory.createStatement(
                subject,
                TopOntologyNames.Properties.HAS_VERSION.rdfIri(),
                valueFactory.createLiteral(version)
        );
        return getRdf4JTemplate().applyToConnection(connection -> connection.hasStatement(typeStatement, true)
                && connection.hasStatement(versionStatement, true));
    }
}

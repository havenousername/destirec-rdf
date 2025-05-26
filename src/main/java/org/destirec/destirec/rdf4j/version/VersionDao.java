package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.VersionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
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
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VersionDao extends GenericDao<VersionConfig.Fields, VersionDto> {
    private final RDF4JTemplate getRdf4JTemplate;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public VersionDao(
            RDF4JTemplate rdf4JTemplate,
            VersionConfig model,
            SchemaVersionMigration migration,
            VersionDtoCreator creator,
            DestiRecOntology ontology,
            RDF4JTemplate getRdf4JTemplate) {
        super(rdf4JTemplate, model, migration, creator, ontology);
        this.getRdf4JTemplate = getRdf4JTemplate;
    }

    public IRI saveRegionVersionId(IRI from) {
        return getRdf4JTemplate().applyToConnection(connection -> {
            IRI subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "/region");
            TriplePattern version = GraphPatterns.tp(subject, RDF.TYPE, VersionNames.Classes.VERSION.rdfIri());
            TriplePattern fromSource = GraphPatterns.tp(subject, DC.SOURCE, from);


            connection.begin();
            ModifyQuery insertQuery = Queries.INSERT(version, fromSource);
            connection.prepareUpdate(insertQuery.getQueryString()).execute();
            connection.commit();

            return getRegionVersion();
        });
    }

    public void saveRegionVersion(IRI from) {
        getRdf4JTemplate().applyToConnection(connection -> {
            IRI subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "region");
            TriplePattern version = GraphPatterns.tp(subject, RDF.TYPE, VersionNames.Classes.VERSION.rdfIri());
            TriplePattern fromSource = GraphPatterns.tp(subject, DC.SOURCE, from);


            connection.begin();
            ModifyQuery insertQuery = Queries.INSERT(version, fromSource);
            connection.prepareUpdate(insertQuery.getQueryString()).execute();
            connection.commit();

            return getRegionVersion();
        });
    }


    public void savePOIVersion(List<IRI> from, int amount) {
        getRdf4JTemplate().applyToConnection(connection -> {
            IRI subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "poi");
            TriplePattern version = GraphPatterns.tp(subject, RDF.TYPE, VersionNames.Classes.VERSION.rdfIri());
            from.forEach(fromSource -> {
                version.andHas((RdfPredicate) DC.SOURCE, fromSource);
            });

            version.andHas(RDFS.LABEL, amount);

            connection.begin();
            ModifyQuery insertQuery = Queries.INSERT(version);
            connection.prepareUpdate(insertQuery.getQueryString()).execute();
            connection.commit();

            return getRegionVersion();
        });
    }

    public boolean hasPOIVersion() {
        Resource subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "poi");
        Statement statement = new Statement() {
            @Override
            public Resource getSubject() {
                return subject;
            }

            @Override
            public IRI getPredicate() {
                return RDF.TYPE;
            }

            @Override
            public Value getObject() {
                return VersionNames.Classes.VERSION.rdfIri();
            }

            @Override
            public Resource getContext() {
                return null;
            }
        };
        return getRdf4JTemplate().applyToConnection(connection -> connection.hasStatement(statement, true));
    }

    public IRI getRegionVersion() {
        Variable regionVersion = SparqlBuilder.var("regionVersion");
        Variable from = SparqlBuilder.var("from");
        SelectQuery query = Queries.SELECT(regionVersion);
        query.where(
                regionVersion.isA(VersionNames.Classes.VERSION.rdfIri())
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

    public boolean hasRegionVersion() {
        Resource subject = valueFactory.createIRI(this.configFields.getResourceLocation() + "region");
        Statement statement = new Statement() {
            @Override
            public Resource getSubject() {
                return subject;
            }

            @Override
            public IRI getPredicate() {
                return RDF.TYPE;
            }

            @Override
            public Value getObject() {
                return VersionNames.Classes.VERSION.rdfIri();
            }

            @Override
            public Resource getContext() {
                return null;
            }
        };
        return getRdf4JTemplate().applyToConnection(connection -> connection.hasStatement(statement, true));
    }
}

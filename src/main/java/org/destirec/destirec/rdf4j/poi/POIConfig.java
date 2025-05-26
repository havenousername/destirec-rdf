package org.destirec.destirec.rdf4j.poi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class POIConfig extends GenericConfig<POIConfig.Fields> {
    public POIConfig() {
        super("poi");
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var values = switch (field) {
            case OSM -> DCTERMS.LOCATION;
            case NAME -> FOAF.NAME;
            case FEATURE -> RegionNames.Properties.HAS_FEATURE.rdfIri();
            case PARENT_REGION -> GEO.sfWithin;
            case SOURCE -> DC.SOURCE;
            case IMAGE -> FOAF.DEPICTION;
            case THUMBNAIL -> FOAF.THUMBNAIL;
            case COORDINATES -> GEO.AS_WKT;
            case OFFICIAL_WEBSITE -> FOAF.HOMEPAGE;
            case IMDB_ID -> DESTIREC.wrap("imdbId").rdfIri();
            case TWITTER_ID -> DESTIREC.wrap("twitterId").rdfIri();
            case QUORA_TOPIC_ID -> DESTIREC.wrap("quoraId").rdfIri();
            case TRIPADVISOR_ID -> DESTIREC.wrap("tripadvisorId").rdfIri();
            case SITE_LINKS_NUMBER -> DESTIREC.wrap("siteLinksNumber").rdfIri();
            case WIKI_STATEMENTS -> DESTIREC.wrap("wikiStatements").rdfIri();
            case FEATURE_SPECIFIC_TYPE -> DESTIREC.wrap("featureSpecificType").rdfIri();
            case OUTGOING_LINKS_NUMBER -> DESTIREC.wrap("outgoingLinksNumber").rdfIri();
            case null -> throw new IllegalArgumentException("Field is not defined");
        };
        return new ValueContainer<>(values);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name()));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        if (field == Fields.IMAGE || field == Fields.THUMBNAIL || field == Fields.OSM) {
            return new ValueContainer<>(CoreDatatype.XSD.STRING);
        } else if (field == Fields.COORDINATES) {
            return new ValueContainer<>(CoreDatatype.GEO.WKT_LITERAL);
        } else if (field == Fields.NAME || field == Fields.OFFICIAL_WEBSITE) {
            return new ValueContainer<>(CoreDatatype.XSD.STRING);
        } else if (field == Fields.IMDB_ID || field == Fields.TWITTER_ID || field == Fields.QUORA_TOPIC_ID || field == Fields.TRIPADVISOR_ID) {
            return new ValueContainer<>(CoreDatatype.XSD.ANYURI);
        } else if (field == Fields.SITE_LINKS_NUMBER || field == Fields.WIKI_STATEMENTS || field == Fields.OUTGOING_LINKS_NUMBER) {
            return new ValueContainer<>(CoreDatatype.XSD.INTEGER);
        }
        return new ValueContainer<>(null);
    }

    @Override
    public Boolean getIsOptional(Fields field) {
        if (field == Fields.SOURCE || field == Fields.NAME || field == Fields.PARENT_REGION || field == Fields.FEATURE || field == Fields.FEATURE_SPECIFIC_TYPE) {
            return false;
        }

        return true;
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        NAME(RegionNames.Properties.NAME.str(), true),
        PARENT_REGION("parentRegion", true),
        FEATURE("feature", true),
        SOURCE("sourceIRI", true),
        IMAGE("image", true),
        OFFICIAL_WEBSITE("website", true),
        COORDINATES("coords", true),
        OSM("osm", true),
        SITE_LINKS_NUMBER("siteLinksNumber", true),
        FEATURE_SPECIFIC_TYPE("featureSpecificType", true),
        OUTGOING_LINKS_NUMBER("outgoingLinksNumber", true),
        WIKI_STATEMENTS("wikiStatements", true),
        QUORA_TOPIC_ID("quaraTopic", true),
        TWITTER_ID("twitterTopic", true),
        IMDB_ID("IMDBKeyword", true),
        TRIPADVISOR_ID("tripadvisorMention", true),
        THUMBNAIL("image", true);

        private final String name;
        private final boolean isRead;
    }
}

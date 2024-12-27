package org.destirec.destirec.rdf4j.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.model.predicates.DomainPredicate;
import org.destirec.destirec.rdf4j.model.predicates.PreferencePredicate;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.ClassIncrement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.HashMap;
import java.util.Map;

public class UserPreferences extends NestedResourceRDF<UserPreferences.Fields> {
    private final IRI userIRI;
    private final PreferencePredicate preferencePredicate;

    @Getter
    private final DomainPredicate pricePredicate;

    @Getter
    private final DomainPredicate popularityPredicate;

    @Getter
    private final DomainPredicate travelMonthsPredicate;


    @Getter
    @AllArgsConstructor
    public enum Fields {
        PRICE_RANGE("priceRange"),
        IS_PRICE_IMPORTANT("priceImportant"),
        POPULARITY_RANGE("popularityRange"),
        IS_POPULARITY_IMPORTANT("popularityImportant"),
        MONTH("month"),
        MONTHS_RANGE("monthsRange"),
        IS_PEAK_SEASON_IMPORTANT("isPeakSeasonImportant");


        private final String displayName;
    }


    public UserPreferences(IRI userIRI) {
        super("Preferences", new HashMap<>(),
                Map.ofEntries(
                        Map.entry(Fields.PRICE_RANGE, CoreDatatype.XSD.FLOAT),
                        Map.entry(Fields.IS_PRICE_IMPORTANT, CoreDatatype.XSD.BOOLEAN),
                        Map.entry(Fields.POPULARITY_RANGE, CoreDatatype.XSD.FLOAT),
                        Map.entry(Fields.IS_POPULARITY_IMPORTANT, CoreDatatype.XSD.BOOLEAN),
                        Map.entry(Fields.MONTH, CoreDatatype.XSD.GMONTH),
                        Map.entry(Fields.IS_PEAK_SEASON_IMPORTANT, CoreDatatype.XSD.BOOLEAN),
                        Map.entry(Fields.MONTHS_RANGE, CoreDatatype.XSD.FLOAT)
                ));
        this.userIRI = userIRI;

        preferencePredicate = new PreferencePredicate("Preference");

        pricePredicate = new DomainPredicate(
                "pricePreference",
                preferencePredicate.get(),
                get(),
                new HashMap<>(Map.ofEntries(
                        Map.entry(DomainPredicate.ChildPredicates.BOOL, "isPriceImportant"),
                        Map.entry(DomainPredicate.ChildPredicates.RANGE, "priceRange")
                )));

        popularityPredicate = new DomainPredicate(
                "popularityPreference",
                preferencePredicate.get(),
                get(),
                new HashMap<>(Map.ofEntries(
                        Map.entry(DomainPredicate.ChildPredicates.BOOL, "isPopularityImportant"),
                        Map.entry(DomainPredicate.ChildPredicates.RANGE, "popularityRange")
                )));
        travelMonthsPredicate = new DomainPredicate(
                "travelMonthsPreference",
                preferencePredicate.get(),
                get(),
                new HashMap<>(Map.ofEntries(
                        Map.entry(DomainPredicate.ChildPredicates.BOOL, "isPeakSeasonImportant"),
                        Map.entry(DomainPredicate.ChildPredicates.MONTHS, "month"),
                        Map.entry(DomainPredicate.ChildPredicates.RANGE, "monthRange")
                )));


        setNestedPredicate(Map.entry(
                preferencePredicate.get(),
                "hasMonthValue"
        ));

        fields.put(Fields.PRICE_RANGE, pricePredicate.getChildIRI(DomainPredicate.ChildPredicates.RANGE));
        fields.put(Fields.IS_PRICE_IMPORTANT, pricePredicate.getChildIRI(DomainPredicate.ChildPredicates.BOOL));
        fields.put(Fields.POPULARITY_RANGE, popularityPredicate.getChildIRI(DomainPredicate.ChildPredicates.RANGE));
        fields.put(Fields.IS_POPULARITY_IMPORTANT, popularityPredicate.getChildIRI(DomainPredicate.ChildPredicates.BOOL));
        fields.put(Fields.MONTH, travelMonthsPredicate.getChildIRI(DomainPredicate.ChildPredicates.MONTHS));
        fields.put(Fields.IS_PEAK_SEASON_IMPORTANT, travelMonthsPredicate.getChildIRI(DomainPredicate.ChildPredicates.BOOL));
        fields.put(Fields.MONTHS_RANGE, travelMonthsPredicate.getChildIRI(DomainPredicate.ChildPredicates.RANGE));

        ClassIncrement.getInstance().addClass(this);
        increment = ClassIncrement.getInstance().getIncrement(this);
    }

    @Override
    public void setup(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, OWL.THING)
                .add(get(), OWL.EQUIVALENTCLASS, WIKIDATA.ELECTRONIC_DICTIONARY)
                .add(get(), RDFS.DOMAIN, userIRI)
                .add(get(), RDFS.COMMENT, "A collection of user preferences");

        getNestedPredicate().setup(builder, graphName);
        preferencePredicate.setup(builder, graphName);
        pricePredicate.setup(builder, graphName);
        travelMonthsPredicate.setup(builder, graphName);
    }


    @Override
    public String getResourceLocation() {
        return "resource/preferences/";
    }
}

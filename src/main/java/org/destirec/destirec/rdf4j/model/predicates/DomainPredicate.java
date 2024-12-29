package org.destirec.destirec.rdf4j.model.predicates;

import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DomainPredicate implements Predicate {
    private final IRI iri;

    private final IRI parent;

    @Nullable
    private final IRI domain;

    @Setter
    private IRI type = OWL.CLASS;


    @Getter
    @Nullable
    private Literal label;

    @Getter
    @Nullable
    private Literal comment;

    public void setLabel(String label) {
        this.label = SimpleValueFactory.getInstance().createLiteral(label);
    }

    public void setComment(String comment) {
        this.comment = SimpleValueFactory.getInstance().createLiteral(comment);
    }

    @Override
    public IRI get() {
        return iri;
    }

    public IRI predicateIRI() {
        return iri;
    }

    @Override
    public void setup(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, type);

        if (parent != null) {
            builder.add(get(), RDFS.SUBCLASSOF, parent);
        }

        if (domain != null) {
            builder.add(get(), RDFS.DOMAIN, domain);
        }

        if (label != null) {
            builder.add(get(), RDFS.LABEL, label);
        }

        if (comment != null) {
            builder.add(get(), RDFS.COMMENT, comment);
        }


        predicates.forEach((key, value) -> value.setup(builder, graphName));
    }

    public enum ChildPredicates {
        RANGE,
        BOOL,
        MONTHS,
        GENERIC
    }

    @Getter
    protected final Map<ChildPredicates, Predicate> predicates = new HashMap<>();

    public IRI getChildIRI(ChildPredicates childPredicate) {
        return getPredicates().get(childPredicate).get();
    }

    public DomainPredicate(String name, IRI parent, @Nullable IRI domain, Map<ChildPredicates, String> childPredicates) {
        iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
        this.domain = domain;
        this.parent = parent;
        assignChildren(childPredicates);
    }

    public DomainPredicate(String name) {
        iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
        assignChildren(new HashMap<>());
        parent = null;
        domain = null;
    }

    public DomainPredicate(String name, @Nullable IRI domain) {
        iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
        assignChildren(new HashMap<>());
        parent = null;
        this.domain = domain;
    }

    public DomainPredicate(String name, IRI parent, @Nullable IRI domain) {
        iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
        this.domain = domain;
        this.parent = parent;
        assignChildren(new HashMap<>());
    }

    protected void assignChildren(Map<ChildPredicates, String> childPredicates) {
        childPredicates.forEach((childKey, childValue) -> {
            if (childKey == ChildPredicates.BOOL) {
                predicates.put(ChildPredicates.BOOL, new BooleanPredicate(childValue));
            } else if (childKey == ChildPredicates.RANGE) {
                predicates.put(ChildPredicates.RANGE, new RangePredicate(childValue));
            } else if (childKey == ChildPredicates.MONTHS) {
                predicates.put(ChildPredicates.MONTHS, new MonthsPredicate(childValue));
            } else {
                predicates.put(ChildPredicates.GENERIC, new GenericPredicate(childValue));
            }
        });
    }

    public abstract static class SimplePredicate implements Predicate {
        private final IRI iri;

        @Getter
        private final String label;

        public SimplePredicate(String name) {
            iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
            label = name;
        }

        @Override
        public IRI get() {
            return iri;
        }

    }


    public class GenericPredicate extends SimplePredicate {
        public GenericPredicate(String name) {
            super(name);
        }

        @Override
        public void setup(ModelBuilder builder, String graphName) {
            builder
                    .namedGraph(graphName)
                    .add(get(), RDF.TYPE, OWL.OBJECTPROPERTY)
                    .add(get(), RDFS.SUBPROPERTYOF, parent)
                    .add(get(), RDFS.DOMAIN, predicateIRI())
                    .add(get(), RDFS.LABEL, getLabel())
                    .add(get(), RDFS.RANGE, predicateIRI());
        }
    }


    public class RangePredicate extends SimplePredicate {

        public RangePredicate(String name) {
            super(name);
        }

        @Override
        public void setup(ModelBuilder builder, String graphName) {
            builder
                    .namedGraph(graphName)
                    .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                    .add(get(), RDFS.SUBPROPERTYOF, parent)
                    .add(get(), RDFS.DOMAIN, predicateIRI())
                    .add(get(), RDFS.RANGE, WIKIDATA.PERCENT);
        }
    }

    public class BooleanPredicate extends SimplePredicate {

        public BooleanPredicate(String name) {
            super(name);
        }

        @Override
        public void setup(ModelBuilder builder, String graphName) {
            builder
                    .namedGraph(graphName)
                    .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                    .add(get(), RDFS.SUBPROPERTYOF, parent)
                    .add(get(), RDFS.DOMAIN, predicateIRI())
                    .add(get(), RDFS.RANGE, XSD.BOOLEAN);
        }
    }


    public class MonthsPredicate extends SimplePredicate {
        public MonthsPredicate(String name) {
            super(name);
        }

        @Override
        public void setup(ModelBuilder builder, String graphName) {
            builder
                    .namedGraph(graphName)
                    .add(get(), RDF.TYPE, OWL.OBJECTPROPERTY)
                    .add(get(), RDFS.SUBPROPERTYOF, parent)
                    .add(get(), RDFS.DOMAIN, predicateIRI())
                    .add(get(), RDFS.RANGE, TIME.MONTH);
        }
    }
}

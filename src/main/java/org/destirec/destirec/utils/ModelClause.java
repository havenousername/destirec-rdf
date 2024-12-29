package org.destirec.destirec.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

@Getter
@Setter
@NoArgsConstructor
public class ModelClause {
    private Model model;

    public String generateModelClause() {
        StringBuilder modelClause = new StringBuilder();
        model.forEach(statement -> {
            modelClause.append("<").append(statement.getSubject()).append("> ")
                    .append("<").append(statement.getPredicate()).append("> ")
                    .append(serializeObject(statement.getObject())).append(". ");
        });

        return modelClause.toString();
    }

    public TriplePattern[] generateInsertPatterns() {
        return model
                .stream()
                .map(statement -> GraphPatterns.tp(
                        statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject())
                )
                .toArray(TriplePattern[]::new);
    }

    private String serializeObject(Value object) {
        if (object.isIRI()) {
            return "<" + object.stringValue() + ">";
        } else if (object.isLiteral()) {
            Literal literal = (Literal) object;
            String value = "\"" + object.stringValue() + "\"";
            if (literal.getDatatype() != null) {
                value += "^^<" + literal.getDatatype().stringValue() + ">";
            } else if (literal.getLanguage().isPresent()) {
                value += "@" + literal.getLanguage().get();
            }
            return value;
        } else {
            throw new IllegalArgumentException("Unsupported RDF object type: " + object.getClass());
        }
    }
}

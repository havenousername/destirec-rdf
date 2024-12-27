package org.destirec.destirec.rdf4j.model.predicates;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DomainMultipleChildPredicate extends DomainPredicate {

    protected final Map<ChildPredicates, List<Predicate>> listPredicates = new HashMap<>();
    public DomainMultipleChildPredicate(String name, IRI parent, IRI domain, Map<ChildPredicates, List<String>> childPredicates) {
        super(name, parent, domain);
        assignChildrenList(childPredicates);
    }


    protected void assignChildrenList(Map<ChildPredicates, List<String>> childPredicates) {

        childPredicates.forEach((childKey, childValue) -> {
            if (childKey == ChildPredicates.BOOL) {
                listPredicates.put(ChildPredicates.BOOL, childValue.stream().map(BooleanPredicate::new).collect(Collectors.toList()));
            } else if (childKey == ChildPredicates.RANGE) {
                listPredicates.put(ChildPredicates.RANGE, childValue.stream().map(RangePredicate::new).collect(Collectors.toList()));
            } else if (childKey == ChildPredicates.MONTHS) {
                listPredicates.put(ChildPredicates.MONTHS, childValue.stream().map(MonthsPredicate::new).collect(Collectors.toList()));
            } else {
                listPredicates.put(ChildPredicates.GENERIC, childValue.stream().map(GenericPredicate::new).collect(Collectors.toList()));
            }
        });
    }
}

package org.destirec.destirec.rdf4j.region.apiDto;

import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;

@Getter
@Setter
public class SimpleRegionDto {
    String id;
    String name;
    IRI source;
    IRI sourceParent;
    RegionTypes type;
}

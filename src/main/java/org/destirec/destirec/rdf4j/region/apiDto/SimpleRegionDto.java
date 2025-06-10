package org.destirec.destirec.rdf4j.region.apiDto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;

@ToString
@Getter
@Setter
public class SimpleRegionDto {
    String id;
    String name;
    IRI source;
    IRI sourceParent;
    RegionTypes type;
    String iso;
    IRI geoShape;
    String osmId;
}

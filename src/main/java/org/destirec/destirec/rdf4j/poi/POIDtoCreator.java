package org.destirec.destirec.rdf4j.poi;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.knowledgeGraph.POIClass;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class POIDtoCreator implements DtoCreator<POIDto, POIConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final FeatureDao featureDao;
    private final POIConfig poiConfig ;

    public POIDtoCreator(FeatureDao featureDao, POIConfig poiConfig) {
        this.featureDao = featureDao;
        this.poiConfig = poiConfig;
    }

    @Override
    public POIDto create(IRI id, Map<POIConfig.Fields, String> map) {
        FeatureDto featureDto = Optional.ofNullable(map.get(POIConfig.Fields.FEATURE))
                .map(valueFactory::createIRI)
                .map(featureDao::getById)
                .orElse(null);
        IRI parent = Optional.ofNullable(map.get(POIConfig.Fields.PARENT_REGION))
                .map(valueFactory::createIRI)
                .orElse(null);

        IRI source = Optional.ofNullable(map.get(POIConfig.Fields.SOURCE))
                .map(valueFactory::createIRI)
                .orElse(null);

        String concreteFeature = map.get(POIConfig.Fields.FEATURE_SPECIFIC_TYPE);
        WIKIDATA.RegionOntology.QTypes type = concreteFeature != null ? WIKIDATA.RegionOntology.QTypes.getQTypeFromIRI(concreteFeature.toUpperCase()) : null;

        String image = map.get(POIConfig.Fields.IMAGE);
        String thumb = map.get(POIConfig.Fields.THUMBNAIL);

        return new POIDto(
                id,
                map.get(POIConfig.Fields.NAME),
                source,
                parent,
                featureDto,
                type,
                map.get(POIConfig.Fields.OSM),
                Integer.parseInt(Optional.ofNullable(map.get(POIConfig.Fields.SITE_LINKS_NUMBER)).orElse("0")),
                image != null || thumb != null,
                map.get(POIConfig.Fields.OFFICIAL_WEBSITE),
                new Pair<>(image, thumb),
                Integer.parseInt(Optional.ofNullable(map.get(POIConfig.Fields.OUTGOING_LINKS_NUMBER)).orElse("0")),
                Integer.parseInt(Optional.ofNullable(map.get(POIConfig.Fields.WIKI_STATEMENTS)).orElse("0")),
                map.get(POIConfig.Fields.QUORA_TOPIC_ID) != null,
                map.get(POIConfig.Fields.TWITTER_ID) != null,
                map.get(POIConfig.Fields.IMDB_ID) != null,
                map.get(POIConfig.Fields.TRIPADVISOR_ID) != null,
                new Quartet<>(
                        map.get(POIConfig.Fields.QUORA_TOPIC_ID),
                        map.get(POIConfig.Fields.TRIPADVISOR_ID),
                        map.get(POIConfig.Fields.TWITTER_ID),
                        map.get(POIConfig.Fields.IMDB_ID)
                ),
                map.get(POIConfig.Fields.COORDINATES),
                featureDto != null ? featureDto.getHasScore() : 0
        );
    }

    @Override
    public POIDto create(Map<POIConfig.Fields, String> map) {
        return create(null, map);
    }

    public IRI createId(String id) {
        if (id == null) {
            return null;
        }
        return valueFactory.createIRI(poiConfig.getResourceLocation() + id);
    }

    public POIDtoWithHops create(POIDto dto, String ancestor, String hopCount) {
        IRI ancestorIri = Optional.ofNullable(ancestor)
                .map(valueFactory::createIRI)
                .orElse(null);
        if (ancestorIri == null) {
            throw new IllegalArgumentException("Ancestor cannot not be null");
        }
        int hopCountInt = Integer.parseInt(Optional.ofNullable(hopCount).orElse("0"));
        return new POIDtoWithHops(dto, ancestorIri, hopCountInt);
    }

    public POIDto create(POIClass poiClass, FeatureDto featureDto, IRI parent) {
        return new POIDto(
                createId(poiClass.getId()),
                poiClass.getName(),
                poiClass.getSource(),
                parent,
                featureDto,
                poiClass.getFeatureSpecificType(),
                poiClass.getOsmLink(),
                poiClass.getSiteLinks(),
                poiClass.isHasImage(),
                poiClass.getOfficialWebsite(),
                poiClass.getImages(),
                poiClass.getOutgoingLinks(),
                poiClass.getStatements(),
                poiClass.isHasQuoraTopic(),
                poiClass.isHasTwitterAccount(),
                poiClass.isHasImdbKeyword(),
                poiClass.isHasTripAdvisorAccount(),
                poiClass.getInternetMentions(),
                poiClass.getCoords(),
                poiClass.getPercentageScore()
        );
    }
}

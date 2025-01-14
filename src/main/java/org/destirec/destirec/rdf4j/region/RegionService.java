package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class RegionService {
    private final RegionDao regionDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public RegionService(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    @Transactional
    public IRI createRegion(ExternalRegionDto regionDto) {
        if (regionDto.graphId() != null) {
            String msg = "Region with ID " + regionDto.id() + " is already present in the RDF database";
            throw new IllegalArgumentException(msg);
        }
        List<MonthDto> monthDtos = regionDto
                .getMonths()
                .entrySet()
                .stream()
                .map(month -> regionDao.getMonthDao().getDtoCreator().create(month))
                .map(dto -> regionDao.getMonthDao().save(dto))
                .toList();

        regionDao.getConfigFields()
                .setFeatureNames(regionDto.getFeatures().keySet().stream().toList());
        List<FeatureDto> featureDtos = regionDto
                .getFeatures()
                .entrySet()
                .stream()
                .map(feature -> regionDao.getFeatureDao().getDtoCreator().create(feature))
                .map(dto -> regionDao.getFeatureDao().save(dto))
                .toList();

        var cost = regionDto.getCost();
        CostDto costDto = regionDao.getCostDao()
                .save(regionDao.getCostDao()
                        .getDtoCreator().create(cost.get(0), cost.get(1)));

        RegionDto regionDtoForCreate = regionDao.getDtoCreator()
                .create(regionDto, featureDtos, monthDtos, costDto);
        logger.info("Create region with DTO" + regionDtoForCreate);
        IRI regionId = regionDao.saveAndReturnId(regionDtoForCreate);
        logger.info("Region with DTO" + regionId + " was created");
        return regionId;
    }
}

package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import eu.europa.ec.dgc.businessrule.repository.CountryListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountryListService {

    private final CountryListRepository countryListRepository;

    /**
     * returns the country list.
     */
    public String getCountryList() {
        CountryListEntity  cle = countryListRepository.getFirstByIdNotNull();
        if (cle != null) {
            return cle.getRawData();
        } else {
            return "[]";
        }
    }

}

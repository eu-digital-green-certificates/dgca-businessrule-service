package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import eu.europa.ec.dgc.businessrule.repository.CountryListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountryListService {

    private static final Long COUNTRY_LIST_ID = 1L;

    private final CountryListRepository countryListRepository;

    /**
     * returns the country list.
     */
    @Transactional
    public String getCountryList() {
        CountryListEntity  cle = countryListRepository.getFirstById(COUNTRY_LIST_ID);
        if (cle != null) {
            return cle.getRawData();
        } else {
            return "[]";
        }
    }

    /**
     *  Saves a country list by replacing an old one.
     */

    @Transactional
    public void saveCountryList(String listData) {
        CountryListEntity cle = new CountryListEntity(COUNTRY_LIST_ID,listData);
        countryListRepository.save(cle);
    }


}

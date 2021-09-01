/*-
 * ---license-start
 * eu-digital-green-certificates / dgca-businessrule-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import eu.europa.ec.dgc.businessrule.repository.CountryListRepository;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountryListService {

    private static final Long COUNTRY_LIST_ID = 1L;

    private final CountryListRepository countryListRepository;
    private final Optional<SigningService> signingService;
    private final BusinessRulesUtils businessRulesUtils;

    /**
     * Gets the actual country list.
     * @return the country list.
     */
    @Transactional
    public CountryListEntity getCountryList() {
        CountryListEntity  cle = countryListRepository.getFirstById(COUNTRY_LIST_ID);
        if (cle == null) {
            cle =  new CountryListEntity(COUNTRY_LIST_ID,"[]",null,null);
        }
        return cle;
    }


    /**
     * Updates a country List, if it is different from the old one.
     * @param newCountryListData new country list data
     */
    @Transactional
    public void updateCountryList(String newCountryListData) {
        CountryListEntity oldList = getCountryList();
        if (!newCountryListData.equals(oldList.getRawData())) {
            saveCountryList(newCountryListData);
        }
    }


    /**
     * Saves a country list by replacing an old one.
     * @param listData the country list to be saved.
     */
    @Transactional
    public void saveCountryList(String listData) {
        CountryListEntity cle = new CountryListEntity(COUNTRY_LIST_ID,listData,null,null);
        try {
            cle.setHash(businessRulesUtils.calculateHash(listData));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        if (signingService.isPresent()) {
            cle.setSignature(signingService.get().computeSignature(cle.getHash()));
        }
        countryListRepository.save(cle);
    }




}

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
     * Updates a country List, if it is different from the old one.
     * @param newCountryListData new country list data
     */
    @Transactional
    public void updateCountryList(String newCountryListData) {
        String oldList = getCountryList();
        if (!newCountryListData.equals(oldList)) {
            saveCountryList(newCountryListData);
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

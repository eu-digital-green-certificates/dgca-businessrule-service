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

import eu.europa.ec.dgc.businessrule.entity.BusinessRuleEntity;
import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.restapi.dto.BusinessRuleListItemDto;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class DomesticRuleService {

    private Map<String, BusinessRuleEntity> domesticRuleMap = new HashMap<>();
    private final ListSigningService listSigningService;
    private final Optional<SigningService> signingService;
    private final SignedListRepository signedListRepository;

    /**
     * Gets list of all rules ids and hashes.
     */
    public List<BusinessRuleListItemDto> getBusinessRulesList() {
        return domesticRuleMap.values().stream().map(bre -> new BusinessRuleListItemDto(
            bre.getIdentifier(),
            bre.getVersion(),
            bre.getCountry(),
            bre.getHash()
        )).collect(Collectors.toList());
    }

    public Optional<SignedListEntity> getBusinessRulesSignedList() {
        return signedListRepository.findById(ListType.DomesticRules);
    }

    /**
     * Gets a List of BusinessRules for a country.
     */
    public List<BusinessRuleListItemDto> getBusinessRulesListForCountry(String country) {
        return getBusinessRulesList().stream().filter(item ->
            country.equalsIgnoreCase(item.getCountry())).collect(Collectors.toList());
    }

    /**
     * Gets  a rule by country and hash.
     */
    @Transactional
    public BusinessRuleEntity getBusinessRuleByCountryAndHash(String country, String hash) {
        return domesticRuleMap.get(country + hash);
    }

    /**
     * Updates the list of rules.
     *
     * @param businessRules list of actual value sets
     */
    @Transactional
    public void updateBusinessRules(List<BusinessRuleItem> businessRules) {
        domesticRuleMap.clear();

        for (BusinessRuleItem rule : businessRules) {
            saveBusinessRule(rule);
        }

        listSigningService.updateSignedList(getBusinessRulesList(), ListType.DomesticRules);
    }

    /**
     * Saves a rule.
     *
     * @param rule The rule to be saved.
     */
    @Transactional
    public void saveBusinessRule(BusinessRuleItem rule) {
        BusinessRuleEntity bre = new BusinessRuleEntity();
        bre.setHash(rule.getHash());
        bre.setIdentifier(rule.getIdentifier());
        bre.setCountry(rule.getCountry().toUpperCase(Locale.ROOT));
        bre.setVersion(rule.getVersion());
        bre.setRawData(rule.getRawData());

        signingService.ifPresent(service -> bre.setSignature(service.computeSignature(bre.getHash())));
        domesticRuleMap.put(bre.getCountry() + bre.getHash(), bre);
    }

}

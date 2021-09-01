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
import eu.europa.ec.dgc.businessrule.repository.BusinessRuleRepository;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.restapi.dto.BusinessRuleListItemDto;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import eu.europa.ec.dgc.gateway.connector.model.ValidationRule;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    private Map<String,BusinessRuleEntity> domesticRuleMap = new HashMap<>();
    private final ListSigningService listSigningService;
    private final Optional<SigningService> signingService;
    private final SignedListRepository signedListRepository;

    private final BusinessRulesUtils businessRulesUtils;

    /**
     *  Gets list of all rules ids and hashes.
     */
    public List<BusinessRuleListItemDto> getBusinessRulesList() {

        List<BusinessRuleListItemDto> rulesItems = domesticRuleMap.values().stream().map(bre -> {
            BusinessRuleListItemDto listItem = new BusinessRuleListItemDto(
                bre.getIdentifier(),
                bre.getVersion(),
                bre.getCountry(),
                bre.getHash()
            );
            return listItem; }).collect(Collectors.toList());

        return rulesItems;
    }

    public Optional<SignedListEntity> getBusinessRulesSignedList() {
        return signedListRepository.findById(ListType.DomesticRules);
    }

    public List<BusinessRuleListItemDto> getBusinessRulesListForCountry(String country) {

        List<BusinessRuleListItemDto> ruleItems = getBusinessRulesList();

        return ruleItems.stream().filter(item ->
            country.equalsIgnoreCase(item.getCountry())).collect(Collectors.toList());

    }



    /**
     *  Gets  a rule by country and hash.
     */
    @Transactional
    public BusinessRuleEntity getBusinessRuleByCountryAndHash(String country, String hash) {

        return  domesticRuleMap.get(country + hash);
    }

    /**
     * Updates the list of rules.
     * @param businessRules list of actual value sets
     */
    @Transactional
    public void updateBusinessRules(List<BusinessRuleItem> businessRules) {
        domesticRuleMap.clear();

        for (BusinessRuleItem rule : businessRules) {
            saveBusinessRule(rule);
        }

        listSigningService.updateSignedList(getBusinessRulesList(),ListType.DomesticRules);
    }

    /**
     * Saves a rule.
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

        if (signingService.isPresent()) {
            bre.setSignature(signingService.get().computeSignature(bre.getHash()));
        }
        domesticRuleMap.put(bre.getCountry() + bre.getHash(), bre);
    }

}

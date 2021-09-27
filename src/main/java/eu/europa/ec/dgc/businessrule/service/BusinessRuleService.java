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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Service
public class BusinessRuleService {

    private final BusinessRuleRepository businessRuleRepository;
    private final ListSigningService listSigningService;
    private final Optional<SigningService> signingService;
    private final SignedListRepository signedListRepository;

    private final BusinessRulesUtils businessRulesUtils;

    /**
     * Creates the signature for the empty rules list after start up.
     */
    @PostConstruct
    @Transactional
    public void businessRuleServiceInit() {
        listSigningService.updateSignedList(getBusinessRulesList(),ListType.Rules);
    }

    /**
     *  Gets list of all business rules ids and hashes.
     *
     */
    @Cacheable("business_rules")
    public List<BusinessRuleListItemDto> getBusinessRulesList() {
        log.debug("Get Rules list executed.");
        List<BusinessRuleListItemDto> rulesItems = businessRuleRepository.findAllByOrderByIdentifierAsc();
        return rulesItems;
    }

    @Cacheable("business_rules")
    public Optional<SignedListEntity> getBusinessRulesSignedList() {
        log.debug("Get Rules list executed.");
        return signedListRepository.findById(ListType.Rules);
    }

    /**
     *  Gets list of all business rules ids and hashes for a country.
     */
    @Cacheable("business_rules")
    public List<BusinessRuleListItemDto> getBusinessRulesListForCountry(String country) {
        log.debug("Get Rules list for country ({}) executed.", country);
        List<BusinessRuleListItemDto> rulesItems =
            businessRuleRepository.findAllByCountryOrderByIdentifierAsc(country.toUpperCase(Locale.ROOT));
        return rulesItems;
    }

    /**f
     *  Gets  a business rule by country and hash.
     */
    @Transactional
    @Cacheable("business_rules")
    public BusinessRuleEntity getBusinessRuleByCountryAndHash(String country, String hash) {
        log.debug("Get rule for country ({}) and hash ({}) executed.", country, hash);
        return  businessRuleRepository.findOneByCountryAndHash(country, hash);
    }

    /**
     * Updates the list of business rules.
     * @param businessRules list of actual value sets
     */
    @Transactional
    @CacheEvict(value = "business_rules", allEntries = true)
    public void updateBusinessRules(List<BusinessRuleItem> businessRules) {
        List<String> ruleHashes =
            businessRules.stream().map(BusinessRuleItem::getHash).collect(Collectors.toList());
        List<String> alreadyStoredRules = getBusinessRulesHashList();

        if (ruleHashes.isEmpty()) {
            businessRuleRepository.deleteAll();
        } else {
            businessRuleRepository.deleteByHashNotIn(ruleHashes);
        }

        for (BusinessRuleItem rule : businessRules) {
            if (!alreadyStoredRules.contains(rule.getHash())) {
                saveBusinessRule(rule);
            }
        }
        listSigningService.updateSignedList(getBusinessRulesList(),ListType.Rules);
    }

    /**
     * Saves a Business rule.
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

        businessRuleRepository.save(bre);
    }

    /**
     * Creates a List of business rule items from a list of validation rules.
     * @param validationRules the list containing the validation rules.
     * @return List of BusinessRuleItems.
     */
    public List<BusinessRuleItem> createBusinessRuleItemList(List<ValidationRule> validationRules)
        throws NoSuchAlgorithmException {
        List<BusinessRuleItem> businessRuleItems = new ArrayList<>();

        for (ValidationRule validationRule: validationRules) {
            BusinessRuleItem businessRuleItem = new BusinessRuleItem();

            businessRuleItem.setHash(businessRulesUtils.calculateHash(validationRule.getRawJson()));
            businessRuleItem.setIdentifier(validationRule.getIdentifier());
            businessRuleItem.setCountry(validationRule.getCountry());
            businessRuleItem.setVersion(validationRule.getVersion());
            businessRuleItem.setRawData(validationRule.getRawJson());

            businessRuleItems.add(businessRuleItem);
        }

        return businessRuleItems;
    }

    /**
     * Gets a list of hash values of all stored business rules.
     * @return List of hash values
     */
    private List<String> getBusinessRulesHashList() {
        return getBusinessRulesList().stream().map(BusinessRuleListItemDto::getHash).collect(Collectors.toList());
    }
}

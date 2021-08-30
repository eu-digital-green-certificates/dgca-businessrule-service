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

import eu.europa.ec.dgc.businessrule.exception.DomesticRuleParseException;
import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;


/**
 * A service to download the valuesets, business rules and country list from the digital covid certificate gateway.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty("dgc.domestic-mode.enabled")
public class DataDownloadServiceDomesticImpl implements DataDownloadService {

    private final String identifierKeyName = "identifier";
    private final String regionKeyName = "region";
    private final String versionKeyName = "version";
    private final String rawDataKeyName = "raw_data";
    private final List<String> expectedKeys = Arrays.asList(
        identifierKeyName,
        regionKeyName,
        versionKeyName,
        rawDataKeyName);

    @Value("${dgc.domestic-mode.key-store}")
    private String keyStoreName;

    @Value("${dgc.domestic-mode.base-path}")
    private String rulesBasePath;

    private final VaultTemplate vaultTemplate;
    private final BusinessRulesUtils businessRulesUtils;
    private final BusinessRuleService businessRuleService;

    /**
     * A service to download the valuesets, business rules and country list from the digital covid certificate gateway.
     */
    @Override
    @Scheduled(fixedDelayString = "${dgc.businessRulesDownload.timeInterval}")
    @SchedulerLock(name = "DomesticRulesDownloadService_downloadDomesticRules", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dgc.businessRulesDownload.lockLimit}")
    public void downloadRules() {

        List<BusinessRuleItem> ruleItems = new ArrayList<>();

        log.info("Domestic rules download started");

        VaultKeyValueOperations kv = vaultTemplate.opsForKeyValue(
            keyStoreName,
            VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);

        List<String> ruleKeys = kv.list(rulesBasePath);


        for (String ruleKey : ruleKeys) {
            BusinessRuleItem ruleItem;

            try {
                ruleItem = getRuleFromVaultData(kv, ruleKey);
                ruleItems.add(ruleItem);
            } catch (NoSuchAlgorithmException e) {
                log.error("Failed to hash business rules on download.", e);
                return;
            } catch (DomesticRuleParseException e) {
                log.error("Failed to parse rule with rule key: " + ruleKey);
            }
        }

        if (!ruleItems.isEmpty()) {
            businessRuleService.updateBusinessRules(ruleItems);
        } else {
            log.warn("The download of the business rules seems to fail, as the download connector "
                + "returns an empty business rules list.-> No data was changed.");
        }


        log.info("Domestic rules download finished");
    }

    private BusinessRuleItem getRuleFromVaultData(VaultKeyValueOperations kv, String ruleKey)
        throws NoSuchAlgorithmException, DomesticRuleParseException {
        BusinessRuleItem ruleItem = new BusinessRuleItem();
        Map<String, Object> ruleRawData = kv.get(ruleKey).getData();

        if (!ruleRawData.keySet().containsAll(expectedKeys)) {
            log.info(String.format(
                "Not all expected keys value pairs present. Expected: %s , Received: %s",
                expectedKeys,
                ruleRawData.keySet()));
            throw new DomesticRuleParseException();
        }

        ruleItem.setIdentifier(ruleRawData.get(identifierKeyName).toString());
        ruleItem.setCountry(ruleRawData.get(regionKeyName).toString());
        ruleItem.setVersion(ruleRawData.get(versionKeyName).toString());
        ruleItem.setRawData(ruleRawData.get(rawDataKeyName).toString());
        ruleItem.setHash(businessRulesUtils.calculateHash(ruleItem.getRawData()));

        return ruleItem;
    }

    @Override
    public void downloadValueSets() {
        // Not implemented in domestic mode
    }

    ;

    @Override
    public void downloadCountryList() {
        // Not implemented in domestic mode
    }

    ;

}



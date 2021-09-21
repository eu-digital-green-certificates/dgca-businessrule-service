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

import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.model.ValueSetItem;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayCountryListDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValidationRuleDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValueSetDownloadConnector;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A service to download the valuesets, business rules and country list from the
 * digital covid certificate gateway.
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("dgc.gateway.connector.enabled")
@Component
@Profile("!btp")
public class GatewayDataDownloadServiceImpl implements GatewayDataDownloadService {

    private final DgcGatewayValidationRuleDownloadConnector dgcRuleConnector;

    private final DgcGatewayValueSetDownloadConnector dgcValueSetConnector;

    private final DgcGatewayCountryListDownloadConnector dgcCountryListConnector;

    private final BusinessRuleService businessRuleService;

    private final ValueSetService valueSetService;

    private final CountryListService countryListService;

    @Value("${dgc.valueSetsDownload.enabled}")
    private boolean valueSetsDownloadEnabled;

    @Value("${dgc.countryListDownload.enabled}")
    private boolean countryListDownloadEnabled;
    
    @Value("${dgc.businessRulesDownload.enabled}")
    private boolean businessRulesDownloadEnabled;
    
    @Override
    @Scheduled(fixedDelayString = "${dgc.businessRulesDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadBusinessRules", 
                   lockAtLeastFor = "PT0S", 
                   lockAtMostFor = "${dgc.businessRulesDownload.lockLimit}")
    public void downloadBusinessRules() {
        List<BusinessRuleItem> ruleItems;
       

        if (businessRulesDownloadEnabled) {
            log.info("Business rules download started");

            try {
                ruleItems = businessRuleService
                        .createBusinessRuleItemList(dgcRuleConnector.getValidationRules().flat());
            } catch (NoSuchAlgorithmException e) {
                log.error("Failed to hash business rules on download.", e);
                return;
            }

            if (!ruleItems.isEmpty()) {
                businessRuleService.updateBusinessRules(ruleItems);
            } else {
                log.warn("The download of the business rules seems to fail, as the download connector "
                        + "returns an empty business rules list.-> No data was changed.");
            }

            log.info("Business rules finished");
        } else {
            log.info("Business rules download disabled");
        }

    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.valueSetsDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadValueSets", 
                   lockAtLeastFor = "PT0S", 
                   lockAtMostFor = "${dgc.valueSetsDownload.lockLimit}")
    public void downloadValueSets() {

        if (valueSetsDownloadEnabled) {
            List<ValueSetItem> valueSetItems;
            log.info("Valuesets download started");

            try {
                valueSetItems = valueSetService.createValueSetItemListFromMap(dgcValueSetConnector.getValueSets());
            } catch (NoSuchAlgorithmException e) {
                log.error("Failed to hash business rules on download.", e);
                return;
            }

            if (!valueSetItems.isEmpty()) {
                valueSetService.updateValueSets(valueSetItems);
            } else {
                log.warn("The download of the value sets seems to fail, as the download connector "
                        + "returns an empty value sets list.-> No data was changed.");
            }

            log.info("Valuesets download finished");
        } else {
            log.info("Valuesets download disaabled");
        }

    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.countryListDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadCountryList", 
                   lockAtLeastFor = "PT0S", 
                   lockAtMostFor = "${dgc.countryListDownload.lockLimit}")
    public void downloadCountryList() {
    
        if (countryListDownloadEnabled) {

            log.info("Country list download started");

            List<String> countryList = dgcCountryListConnector.getCountryList();

            if (!countryList.isEmpty()) {
                String countryListJsonStr = JSONArray.toJSONString(countryList);
                countryListService.updateCountryList(countryListJsonStr);
            } else {
                log.warn("The download of the country list seems to fail, as the download connector "
                        + "returns an empty country list.-> No data was changed.");
            }

            log.info("Country list download finished");
        } else {
            log.info("Country list download disabled");
        }

    }

}

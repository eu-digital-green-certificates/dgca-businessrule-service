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

//import eu.europa.ec.dgc.gateway.connector.DgcGatewayDownloadConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * A service to download the valuesets, business rules and country list from the digital covid certificate gateway.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("!btp")
public class GatewayDataDownloadServiceImpl implements GatewayDataDownloadService {

    //private final DgcGatewayDownloadConnector dgcGatewayConnector;

    @Override
    @Scheduled(fixedDelayString = "${dgc.businessRulesDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadBusinessRules", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dgc.businessRulesDownload.lockLimit}")
    public void downloadBusinessRules() {
        log.info("Business rules download started");



        log.info("Business rules finished");
    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.valueSetsDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadValueSets", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dgc.valueSetsDownload.lockLimit}")
    public void downloadValueSets() {
        log.info("Valuesets download started");



        log.info("Valuesets download finished");
    }

    @Override
    @Scheduled(fixedDelayString = "${dgc.countryListDownload.timeInterval}")
    @SchedulerLock(name = "GatewayDataDownloadService_downloadCountryList", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dgc.countryListDownload.lockLimit}")
    public void downloadCountryList() {
        log.info("Country list download started");



        log.info("country list download finished");
    }

}

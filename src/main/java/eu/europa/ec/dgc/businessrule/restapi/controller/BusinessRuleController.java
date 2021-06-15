/*-
 * ---license-start
 * eu-digital-green-certificates / dgca-verifier-service
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

package eu.europa.ec.dgc.businessrule.restapi.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rules")
@Slf4j
@RequiredArgsConstructor
public class BusinessRuleController {

    private static final String X_SIGNATURE_HEADER = "X-SIGNATURE";

    /**
     * Http Method for getting the business rules list.
     */
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRules() {
        Resource resource = new ClassPathResource("/static/businessRuleProviderList.json");
        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_SIGNATURE_HEADER, "ECDSA_NOT_CALCULATED_YET");

            return  ResponseEntity.ok()
                .headers(responseHeaders)
                .body(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("Could not read business rule provider list file");
        }
        return ResponseEntity.ok("");
    }


    /**
     * Http Method for getting the business rules list for a country.
     */
    @GetMapping(path = "/{country}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRulesForCountry(
        @Valid @PathVariable("country") String country
    ) {
        Resource resource = new ClassPathResource("/static/businessRuleProviderList.json");
        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_SIGNATURE_HEADER, "ECDSA_NOT_CALCULATED_YET");

            return  ResponseEntity.ok()
                .headers(responseHeaders)
                .body(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("Could not read business rule provider list file");
        }
        return ResponseEntity.ok("");
    }


    /**
     * Http Method for getting  specific business rule set .
     */
    @GetMapping(path = "/{hash}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRuleByHash(
        @Valid @PathVariable("hash") String hash
    ) {
        Resource resource = new ClassPathResource("/static/businessRuleSet.json");
        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(X_SIGNATURE_HEADER, "ECDSA_NOT_CALCULATED_YET");

            return  ResponseEntity.ok()
                .headers(responseHeaders)
                .body(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            log.error("Could not read business rule set file");
        }
        return ResponseEntity.ok("");
    }

}

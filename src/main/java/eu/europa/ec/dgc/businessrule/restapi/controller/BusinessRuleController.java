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

import eu.europa.ec.dgc.businessrule.entity.BusinessRuleEntity;
import eu.europa.ec.dgc.businessrule.exception.DgcaBusinessRulesResponseException;
import eu.europa.ec.dgc.businessrule.restapi.dto.BusinessRuleListItemDto;
import eu.europa.ec.dgc.businessrule.service.BusinessRuleService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rules")
@Slf4j
@RequiredArgsConstructor
public class BusinessRuleController {

    private final BusinessRuleService businessRuleService;

    private static final String X_SIGNATURE_HEADER = "X-SIGNATURE";

    /**
     * Http Method for getting the business rules list.
     */
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BusinessRuleListItemDto>> getRules() {

        return ResponseEntity.ok(businessRuleService.getBusinessRulesList());
    }


    /**
     * Http Method for getting the business rules list for a country.
     */
    @GetMapping(path = "/{country}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BusinessRuleListItemDto>> getRulesForCountry(
        @Valid @PathVariable("country") String country
    ) {
        validateCountryParameter(country);

        return ResponseEntity.ok(businessRuleService.getBusinessRulesListForCountry(country));
    }


    /**
     * Http Method for getting  specific business rule set .
     */
    @GetMapping(path = "/{country}/{hash}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRuleByHash(
        @Valid @PathVariable("country") String country,
        @Valid @PathVariable("hash") String hash
    ) {
        validateCountryParameter(country);
        if (hash == null || hash.isBlank()) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x005", "Possible reasons: "
                + "The provided hash value is not correct", hash,"");
        }
        BusinessRuleEntity rule = businessRuleService.getBusinessRuleByHash(hash);

        if (rule == null) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.NOT_FOUND, "0x006", "Possible reasons: "
                + "The provided hash or country may not be correct.", "country: "+ country +", hash: "+ hash,"");
        }
        return ResponseEntity.ok(rule.getRawData());
    }

    /**
     * Http Method for uploading sample data.
     */
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createRule(
        @RequestHeader(value = "X_COUNTRY", required = true) String country,
        @RequestHeader(value = "X_ID", required = true) String id,
        @RequestBody String ruleData) {

        validateCountryParameter(country);

        if (id == null || id.isBlank()) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x003", "Possible reasons: "
                + "The id of the rule is not set.", id,"");
        }
        businessRuleService.saveBusinessRule(id, country.toUpperCase(Locale.ROOT), ruleData);
        return ResponseEntity.ok("Upload: OK");

    }

    private void validateCountryParameter(String country) throws DgcaBusinessRulesResponseException {
        if (!country.matches("^[a-zA-Z]{2}$")) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x004", "Possible reasons: "
                + "The Country Code has a wrong format. Should be 2 char format.", country,"");
        }
    }

}

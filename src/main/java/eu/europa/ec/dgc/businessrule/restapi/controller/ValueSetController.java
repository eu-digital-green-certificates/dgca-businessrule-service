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


import eu.europa.ec.dgc.businessrule.entity.ValueSetEntity;
import eu.europa.ec.dgc.businessrule.restapi.dto.ValueSetListItemDto;
import eu.europa.ec.dgc.businessrule.service.ValueSetService;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
@RequestMapping("/valuesets")
@Slf4j
@RequiredArgsConstructor
public class ValueSetController {

    private final BusinessRulesUtils businessRulesUtils;

    private final ValueSetService valueSetService;

    private static final String X_SIGNATURE_HEADER = "X-SIGNATURE";

    /**
     * Http Method for getting the value set list.
     */
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ValueSetListItemDto>> getValueSetList() {

        return ResponseEntity.ok(valueSetService.getValueSetsList());
    }

    /**
     * Http Method for getting  specific value set .
     */
    @GetMapping(path = "/{hash}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getValueSet(
        @Valid @PathVariable("hash") String hash
    ) {
        ValueSetEntity vse = valueSetService.getValueSetByHash(hash);

        if (vse == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(vse.getRawData());
    }

    /**
     * Http Method for getting  specific value set .
     */
    @GetMapping(path = "/loaddummy", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> loadDummyData() {
        loadValuesetFile("/static/valuesets/country-2-codes.json","country-2-codes");
        loadValuesetFile("/static/valuesets/disease-agent-targeted.json","disease-agent-targeted");
        loadValuesetFile("/static/valuesets/test-manf.json","covid-19-lab-test-manufacturer-and-name");
        loadValuesetFile("/static/valuesets/test-result.json","covid-19-lab-result");
        loadValuesetFile("/static/valuesets/test-type.json","covid-19-lab-test-type");
        loadValuesetFile("/static/valuesets/vaccine-mah-manf.json","vaccines-covid-19-auth-holders");
        loadValuesetFile("/static/valuesets/vaccine-medicinal-product.json","vaccines-covid-19-names");
        loadValuesetFile("/static/valuesets/vaccine-prophylaxis.json","sct-vaccines-covid-19");

        return ResponseEntity.ok("Loaded");
    }

    private void loadValuesetFile(String filename, String valueSetName) {
        Resource resource = new ClassPathResource(filename);
        try {
            valueSetService.saveValueSet(valueSetName,
                IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Could not read file: " + valueSetName);
        }
    }

}

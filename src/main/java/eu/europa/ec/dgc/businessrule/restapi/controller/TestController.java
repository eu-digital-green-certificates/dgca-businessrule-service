package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.exception.DgcaBusinessRulesResponseException;
import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.service.BusinessRuleService;
import eu.europa.ec.dgc.businessrule.service.CountryListService;
import eu.europa.ec.dgc.businessrule.service.ValueSetService;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test API to upload or init data.
 * It can be used if the gateway is not available
 */
@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
@Profile("testapi")
public class TestController {
    private final BusinessRulesUtils businessRulesUtils;
    private final ValueSetService valueSetService;
    private final CountryListService countryListService;
    private final BusinessRuleService businessRuleService;

    /**
     * Http Method for loading sample value sets from resources .
     */
    @GetMapping(path = "/valuesets/loaddummy", produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<String> loadDummyData() {
        loadValuesetFile("/static/valuesets/country-2-codes.json", "country-2-codes");
        loadValuesetFile("/static/valuesets/disease-agent-targeted.json", "disease-agent-targeted");
        loadValuesetFile("/static/valuesets/test-manf.json", "covid-19-lab-test-manufacturer-and-name");
        loadValuesetFile("/static/valuesets/test-result.json", "covid-19-lab-result");
        loadValuesetFile("/static/valuesets/test-type.json", "covid-19-lab-test-type");
        loadValuesetFile("/static/valuesets/vaccine-mah-manf.json", "vaccines-covid-19-auth-holders");
        loadValuesetFile("/static/valuesets/vaccine-medicinal-product.json", "vaccines-covid-19-names");
        loadValuesetFile("/static/valuesets/vaccine-prophylaxis.json", "sct-vaccines-covid-19");

        return ResponseEntity.ok("Loaded");
    }

    private void loadValuesetFile(String filename, String valueSetName) {
        Resource resource = new ClassPathResource(filename);
        String rawData;
        String hash;
        try {
            rawData = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
            hash = businessRulesUtils.calculateHash(rawData);

        } catch (NoSuchAlgorithmException e) {
            log.error("Calculation of hash failed:", e);
            return;
        } catch (IOException e) {
            log.error("Could not read file: " + valueSetName);
            return;
        }

        valueSetService.saveValueSet(hash, valueSetName, rawData);
    }

    /**
     * Http Method for uploading sample data.
     */
    @PostMapping(path = "/countrylist", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<String> createCountryList(
            @RequestBody String countryListData) {
        countryListService.saveCountryList(countryListData);

        return ResponseEntity.ok("\"Upload\": \"Ok\"");
    }

    /**
     * Http Method for uploading sample data.
     */
    @PostMapping(path = "/rules", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Hidden
    public ResponseEntity<String> createRule(
            @RequestHeader(value = "X_COUNTRY") String country,
            @RequestHeader(value = "X_ID") String id,
            @RequestHeader(value = "X_VER") String version,
            @RequestBody String ruleData) {

        String hash;
        validateCountryParameter(country);

        if (id == null || id.isBlank()) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x003", "Possible reasons: "
                    + "The id of the rule is not set.", id,"");
        }

        try {
            hash = businessRulesUtils.calculateHash(ruleData);
        } catch (NoSuchAlgorithmException e) {
            log.error("Calculation of hash failed:", e);
            throw new DgcaBusinessRulesResponseException(HttpStatus.INTERNAL_SERVER_ERROR, "0x500",
                    "Internal Server Error","","");
        }
        BusinessRuleItem bri = new BusinessRuleItem();
        bri.setHash(hash);
        bri.setIdentifier(id);
        bri. setCountry(country);
        bri.setVersion(version);
        bri.setRawData(ruleData);
        businessRuleService.saveBusinessRule(bri);
        return ResponseEntity.ok("Upload: OK");

    }

    private void validateCountryParameter(String country) throws DgcaBusinessRulesResponseException {
        if (!country.matches("^[a-zA-Z]{2}$")) {
            throw new DgcaBusinessRulesResponseException(HttpStatus.BAD_REQUEST, "0x004", "Possible reasons: "
                    + "The Country Code has a wrong format. Should be 2 char format.", country,"");
        }
    }
}

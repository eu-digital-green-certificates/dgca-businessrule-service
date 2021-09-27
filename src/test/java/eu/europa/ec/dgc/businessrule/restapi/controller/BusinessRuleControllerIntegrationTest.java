package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.repository.BusinessRuleRepository;
import eu.europa.ec.dgc.businessrule.service.BusinessRuleService;
import eu.europa.ec.dgc.businessrule.testdata.BusinessRulesTestHelper;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayCountryListDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValidationRuleDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValueSetDownloadConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessRuleControllerIntegrationTest {

    private static final String API_VERSION_HEADER = "X-VERSION";

    @MockBean
    DgcGatewayValidationRuleDownloadConnector dgcGatewayValidationRuleDownloadConnector;

    @MockBean
    DgcGatewayValueSetDownloadConnector dgcGatewayValueSetDownloadConnector;

    @MockBean
    DgcGatewayCountryListDownloadConnector dgcGatewayCountryListDownloadConnector;

    @Autowired
    BusinessRuleRepository businessRuleRepository;

    @Autowired
    BusinessRuleService businessRuleService;

    @Autowired
    BusinessRulesTestHelper businessRulesTestHelper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearRepositoryData() {

        businessRuleRepository.deleteAll();
        cacheManager.getCache("business_rules").clear();
        businessRuleService.businessRuleServiceInit();
    }

    @Test
    void getEmptyRulesList() throws Exception {
        mockMvc.perform(get("/rules").header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }

    @Test
    void getRulesList() throws Exception {
        String expectedJson = "[{\"identifier\":\"VR-DE-1\",\"version\":\"1.0.0\",\"country\":\"DE\",\"hash\":"
            + "\"ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\"},{\"identifier\":\"VR-DE-2\","
            + "\"version\":\"1.0.0\",\"country\":\"DE\",\"hash\":"
            + "\"edd69d42d52a7b52059cfbea379e647039fc16117b75bf3dfec68c965552a2fd\"},{\"identifier\":\"VR-EU-1\","
            + "\"version\":\"1.0.0\",\"country\":\"EU\",\"hash\":"
            + "\"7bbffe1ac60dc201cf4a1303de4b8ba25ffa5ab714d882a7e4e80dfbb2c08fe7\"}]";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        mockMvc.perform(get("/rules").header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRulesListSameRuleWithDiffrentVersions() throws Exception {
        String expectedJson = "[{\"identifier\":\"VR-DE-1\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\"},"
            + "{\"identifier\":\"VR-DE-1\",\"version\":\"2.0.0\",\"country\":\"DE\","
            + "\"hash\":\"1706b888b9abc095e78ab1ebf32f2445a36c6a263b72634ae56476ecac5c89de\"},"
            + "{\"identifier\":\"VR-DE-2\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"edd69d42d52a7b52059cfbea379e647039fc16117b75bf3dfec68c965552a2fd\"},"
            + "{\"identifier\":\"VR-EU-1\",\"version\":\"1.0.0\",\"country\":\"EU\","
            + "\"hash\":\"7bbffe1ac60dc201cf4a1303de4b8ba25ffa5ab714d882a7e4e80dfbb2c08fe7\"}]";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);


        mockMvc.perform(get("/rules").header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getEmptyRulesListForCountry() throws Exception {
        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }

    @Test
    void getRulesListForCountry() throws Exception {
        String expectedJson = "[{\"identifier\":\"VR-DE-1\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\"},"
            + "{\"identifier\":\"VR-DE-2\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"edd69d42d52a7b52059cfbea379e647039fc16117b75bf3dfec68c965552a2fd\"}]";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRulesListForCountryRuleWithDiffrentVersions() throws Exception {
        String expectedJson = "[{\"identifier\":\"VR-DE-1\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\"},"
            + "{\"identifier\":\"VR-DE-1\",\"version\":\"2.0.0\",\"country\":\"DE\","
            + "\"hash\":\"1706b888b9abc095e78ab1ebf32f2445a36c6a263b72634ae56476ecac5c89de\"},"
            + "{\"identifier\":\"VR-DE-2\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"edd69d42d52a7b52059cfbea379e647039fc16117b75bf3dfec68c965552a2fd\"}]";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);

        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRulesListForCountryWrongCountryFormat() throws Exception {
        String expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"EUR\",\"details\":\"\"}";

        mockMvc.perform(get("/rules/EUR")
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"E\",\"details\":\"\"}";

        mockMvc.perform(get("/rules/E")
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"22\",\"details\":\"\"}";

        mockMvc.perform(get("/rules/22")
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRuleByCountryAndHash() throws Exception {
        String expectedJson = "[{\"identifier\":\"VR-DE-1\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\"},"
            + "{\"identifier\":\"VR-DE-1\",\"version\":\"2.0.0\",\"country\":\"DE\","
            + "\"hash\":\"1706b888b9abc095e78ab1ebf32f2445a36c6a263b72634ae56476ecac5c89de\"},"
            + "{\"identifier\":\"VR-DE-2\",\"version\":\"1.0.0\",\"country\":\"DE\","
            + "\"hash\":\"edd69d42d52a7b52059cfbea379e647039fc16117b75bf3dfec68c965552a2fd\"}]";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);

        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_1 + "/"
            + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(BusinessRulesTestHelper.BR_DATA_1));
    }

    @Test
    void getRuleByCountryAndHashWrongCountryFormat() throws Exception {
        String expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"EUR\",\"details\":\"\"}";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);

        mockMvc.perform(get("/rules/EUR/" + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"E\",\"details\":\"\"}";

        mockMvc.perform(get("/rules/E/" + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        expectedJson = "{\"code\":\"0x004\",\"problem\":\"Possible reasons: The Country Code has a wrong format."
            + " Should be 2 char format.\",\"sendValue\":\"23\",\"details\":\"\"}";

        mockMvc.perform(get("/rules/23/" + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRuleByCountryAndHashWrongCountryHashCombination() throws Exception {
        String expectedJson = "{\"code\":\"0x006\",\"problem\":\"Possible reasons: The provided hash or country may "
            + "not be correct.\",\"sendValue\":\"country: EU, "
            + "hash: ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\",\"details\":\"\"}";

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);

        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_3
            + "/" + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }

    @Test
    void getRuleByCountryAndHashNotExist() throws Exception {
        String expectedJson = "{\"code\":\"0x006\",\"problem\":\"Possible reasons: The provided hash or country may "
            + "not be correct.\",\"sendValue\":\"country: DE, "
            + "hash: ce50e623fd57e482ad9edf63eae7c898d639056e716aeb7f9975a3471bf3e59c\",\"details\":\"\"}";


        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_2,
            BusinessRulesTestHelper.BR_IDENTIFIER_2, BusinessRulesTestHelper.BR_COUNTRY_2,
            BusinessRulesTestHelper.BR_VERSION_2, BusinessRulesTestHelper.BR_DATA_2);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_3,
            BusinessRulesTestHelper.BR_IDENTIFIER_3, BusinessRulesTestHelper.BR_COUNTRY_3,
            BusinessRulesTestHelper.BR_VERSION_3, BusinessRulesTestHelper.BR_DATA_3);

        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_4,
            BusinessRulesTestHelper.BR_IDENTIFIER_4, BusinessRulesTestHelper.BR_COUNTRY_4,
            BusinessRulesTestHelper.BR_VERSION_4, BusinessRulesTestHelper.BR_DATA_4);

        mockMvc.perform(get("/rules/" + BusinessRulesTestHelper.BR_COUNTRY_1
            + "/" + BusinessRulesTestHelper.BR_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }
}
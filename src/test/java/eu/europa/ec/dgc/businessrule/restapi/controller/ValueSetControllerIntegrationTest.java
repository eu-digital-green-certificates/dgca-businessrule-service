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

package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.repository.ValueSetRepository;
import eu.europa.ec.dgc.businessrule.service.BusinessRuleService;
import eu.europa.ec.dgc.businessrule.service.ListSigningService;
import eu.europa.ec.dgc.businessrule.service.SigningService;
import eu.europa.ec.dgc.businessrule.service.ValueSetService;
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
class ValueSetControllerIntegrationTest {

    private static final String API_VERSION_HEADER = "X-VERSION";

    @MockBean
    DgcGatewayValidationRuleDownloadConnector dgcGatewayValidationRuleDownloadConnector;

    @MockBean
    DgcGatewayValueSetDownloadConnector dgcGatewayValueSetDownloadConnector;

    @MockBean
    DgcGatewayCountryListDownloadConnector dgcGatewayCountryListDownloadConnector;


    @Autowired
    ValueSetRepository valueSetRepository;

    @Autowired
    BusinessRulesTestHelper businessRulesTestHelper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListSigningService listSigningService;

    @Autowired
    private ValueSetService valueSetService;

    @Autowired
    private SignedListRepository signedListRepository;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void clearRepositoryData()  {
        valueSetRepository.deleteAll();
        signedListRepository.deleteAll();
        cacheManager.getCache( "value_sets").clear();
        valueSetService.valueSetServiceInit();
    }

    @Test
    void getEmptyValueSetList() throws Exception {
        mockMvc.perform(get("/valuesets").header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }



    @Test
    void getValueSetList() throws Exception {

        String expectedJson = "[{\"id\":\""+BusinessRulesTestHelper.VALUESET_IDENTIFIER_1 +"\","
            + "\"hash\":\""+BusinessRulesTestHelper.VALUESET_HASH_1+"\"},"
            + "{\"id\":\""+BusinessRulesTestHelper.VALUESET_IDENTIFIER_2 +"\","
            + "\"hash\":\""+BusinessRulesTestHelper.VALUESET_HASH_2+"\"}]";

        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_1,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_1,
            BusinessRulesTestHelper.VALUESET_DATA_1);

        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_2,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_2,
            BusinessRulesTestHelper.VALUESET_DATA_2);



        mockMvc.perform(get("/valuesets").header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));
    }


    @Test
    void getValueSet() throws Exception {
        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_1,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_1,
            BusinessRulesTestHelper.VALUESET_DATA_1);

        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_2,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_2,
            BusinessRulesTestHelper.VALUESET_DATA_2);

        mockMvc.perform(get("/valuesets/" + BusinessRulesTestHelper.VALUESET_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(BusinessRulesTestHelper.VALUESET_DATA_1));

        mockMvc.perform(get("/valuesets/" + BusinessRulesTestHelper.VALUESET_HASH_2)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(BusinessRulesTestHelper.VALUESET_DATA_2));

    }

    @Test
    void getValueSetNotExist() throws Exception {

        String expectedJson = "{\"code\":\"0x001\",\"problem\":\"Possible reasons: The provided hash value is "
            + "not correct\",\"sendValue\":\""+BusinessRulesTestHelper.VALUESET_HASH_1+"\",\"details\":\"\"}";


        mockMvc.perform(get("/valuesets/" + BusinessRulesTestHelper.VALUESET_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_2,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_2,
            BusinessRulesTestHelper.VALUESET_DATA_2);

        mockMvc.perform(get("/valuesets/" + BusinessRulesTestHelper.VALUESET_HASH_2)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(BusinessRulesTestHelper.VALUESET_DATA_2));

        mockMvc.perform(get("/valuesets/" + BusinessRulesTestHelper.VALUESET_HASH_1)
            .header(API_VERSION_HEADER, "1.0"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson));

    }


}
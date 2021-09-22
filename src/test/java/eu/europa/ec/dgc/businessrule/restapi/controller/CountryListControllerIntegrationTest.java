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

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import eu.europa.ec.dgc.businessrule.repository.CountryListRepository;
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
class CountryListControllerIntegrationTest {

    private static final Long COUNTRY_LIST_ID = 1L;

    private static final String TEST_LIST_DATA = "[\"BE\", \"EL\", \"LT\", \"PT\", \"BG\", \"ES\", \"LU\", \"RO\", "
        + "\"CZ\", \"FR\", \"HU\", \"SI\", \"DK\", \"HR\", \"MT\", \"SK\", \"DE\", \"IT\", \"NL\", \"FI\", \"EE\", "
        + "\"CY\", \"AT\", \"SE\", \"IE\", \"LV\", \"PL\"]";

    @MockBean
    DgcGatewayValidationRuleDownloadConnector dgcGatewayValidationRuleDownloadConnector;

    @MockBean
    DgcGatewayValueSetDownloadConnector dgcGatewayValueSetDownloadConnector;

    @MockBean
    DgcGatewayCountryListDownloadConnector dgcGatewayCountryListDownloadConnector;

    @Autowired
    CountryListRepository countryListRepository;

    @BeforeEach
    void clearRepositoryData()  {
        countryListRepository.deleteAll();
        cacheManager.getCache("country_list").clear();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CacheManager cacheManager;

    @Test
    void getEmptyCountryList() throws Exception {
        mockMvc.perform(get("/countrylist"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }

    @Test
    void getCountryList() throws Exception {

        CountryListEntity cle = new CountryListEntity(COUNTRY_LIST_ID, TEST_LIST_DATA,null,null);
        countryListRepository.save(cle);

        mockMvc.perform(get("/countrylist"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(TEST_LIST_DATA));
    }
}




package eu.europa.ec.dgc.businessrule.restapi.controller;

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import eu.europa.ec.dgc.businessrule.repository.CountryListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    CountryListRepository countryListRepository;

    @BeforeEach
    void clearRepositoryData()  {
        countryListRepository.deleteAll();
    }

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getEmptyCountryList() throws Exception {
        mockMvc.perform(get("/countrylist"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));
    }

    @Test
    void getCountryList() throws Exception {

        CountryListEntity cle = new CountryListEntity(COUNTRY_LIST_ID, TEST_LIST_DATA);
        countryListRepository.save(cle);

        mockMvc.perform(get("/countrylist"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(TEST_LIST_DATA));
    }
}




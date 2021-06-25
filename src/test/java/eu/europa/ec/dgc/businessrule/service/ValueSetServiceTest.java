package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.ValueSetEntity;
import eu.europa.ec.dgc.businessrule.model.ValueSetItem;
import eu.europa.ec.dgc.businessrule.repository.ValueSetRepository;
import eu.europa.ec.dgc.businessrule.testdata.BusinessRulesTestHelper;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayCountryListDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValidationRuleDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValueSetDownloadConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
@AutoConfigureMockMvc
class ValueSetServiceTest {

    @MockBean
    DgcGatewayValidationRuleDownloadConnector dgcGatewayValidationRuleDownloadConnector;

    @MockBean
    DgcGatewayValueSetDownloadConnector dgcGatewayValueSetDownloadConnector;

    @MockBean
    DgcGatewayCountryListDownloadConnector dgcGatewayCountryListDownloadConnector;

    @Autowired
    ValueSetService valueSetService;

    @Autowired
    ValueSetRepository valueSetRepository;

    @Autowired
    BusinessRulesTestHelper businessRulesTestHelper;

    @Autowired
    BusinessRulesUtils businessRulesUtils;

    @BeforeEach
    void clearRepositoryData() {
        valueSetRepository.deleteAll();
    }

    @Test
    void updateValueSetsWithExisting()  {
        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_1,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_1, BusinessRulesTestHelper.VALUESET_DATA_1);

        List<ValueSetItem> items = new ArrayList<>();

        ValueSetItem item = new ValueSetItem();
        item.setHash(BusinessRulesTestHelper.VALUESET_HASH_1);
        item.setId(BusinessRulesTestHelper.VALUESET_IDENTIFIER_1);
        item.setRawData(BusinessRulesTestHelper.VALUESET_DATA_1);

        items.add(item);

        valueSetService.updateValueSets(items);

        Assertions.assertEquals(1, valueSetRepository.count());
    }

    @Test
    void updateValueSetsWithEmptyList()  {
        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_1,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_1, BusinessRulesTestHelper.VALUESET_DATA_1);

        List<ValueSetItem> items = new ArrayList<>();

        valueSetService.updateValueSets(items);

        Assertions.assertEquals(0, valueSetRepository.count());
    }

    @Test
    void updateValueSets()  {
        businessRulesTestHelper.insertValueSet(BusinessRulesTestHelper.VALUESET_HASH_1,
            BusinessRulesTestHelper.VALUESET_IDENTIFIER_1, BusinessRulesTestHelper.VALUESET_DATA_1);

        List<ValueSetItem> items = new ArrayList<>();

        ValueSetItem item = new ValueSetItem();
        item.setHash(BusinessRulesTestHelper.VALUESET_HASH_1);
        item.setId(BusinessRulesTestHelper.VALUESET_IDENTIFIER_1);
        item.setRawData(BusinessRulesTestHelper.VALUESET_DATA_1);

        items.add(item);

        ValueSetItem item2 = new ValueSetItem();
        item2.setHash(BusinessRulesTestHelper.VALUESET_HASH_2);
        item2.setId(BusinessRulesTestHelper.VALUESET_IDENTIFIER_2);
        item2.setRawData(BusinessRulesTestHelper.VALUESET_DATA_2);

        items.add(item2);

        valueSetService.updateValueSets(items);

        Assertions.assertEquals(2, valueSetRepository.count());

        items.remove(0);

        valueSetService.updateValueSets(items);

        List<ValueSetEntity> result = valueSetRepository.findAll();
        Assertions.assertEquals(1, result.size());

        ValueSetEntity resultEntity = result.get(0);
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_HASH_2, resultEntity.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_IDENTIFIER_2, resultEntity.getId());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_DATA_2, resultEntity.getRawData());
    }


    @Test
    void createValueSetItemListFromMap() throws Exception{
        Map<String, String> map = new HashMap<>();
        map.put(BusinessRulesTestHelper.VALUESET_IDENTIFIER_1, BusinessRulesTestHelper.VALUESET_DATA_1);
        map.put(BusinessRulesTestHelper.VALUESET_IDENTIFIER_2, BusinessRulesTestHelper.VALUESET_DATA_2);

        List<ValueSetItem> list = valueSetService.createValueSetItemListFromMap(map);


        Assertions.assertEquals(2, list.size());

        ValueSetItem resultItem1 = list.get(0);
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_HASH_1, resultItem1.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_IDENTIFIER_1, resultItem1.getId());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_DATA_1, resultItem1.getRawData());

        ValueSetItem resultItem2 = list.get(1);
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_HASH_2, resultItem2.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_IDENTIFIER_2, resultItem2.getId());
        Assertions.assertEquals(BusinessRulesTestHelper.VALUESET_DATA_2, resultItem2.getRawData());

    }
}
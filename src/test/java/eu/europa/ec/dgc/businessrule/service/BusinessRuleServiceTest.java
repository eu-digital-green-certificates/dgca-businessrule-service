package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.BusinessRuleEntity;
import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.repository.BusinessRuleRepository;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.testdata.BusinessRulesTestHelper;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayCountryListDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValidationRuleDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.DgcGatewayValueSetDownloadConnector;
import eu.europa.ec.dgc.gateway.connector.model.ValidationRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jks-signing")
class BusinessRuleServiceTest {

    @MockBean
    DgcGatewayValidationRuleDownloadConnector dgcGatewayValidationRuleDownloadConnector;

    @MockBean
    DgcGatewayValueSetDownloadConnector dgcGatewayValueSetDownloadConnector;

    @MockBean
    DgcGatewayCountryListDownloadConnector dgcGatewayCountryListDownloadConnector;

    @Autowired
    BusinessRuleService businessRuleService;

    @Autowired
    SignedListRepository signedListRepository;

    @Autowired
    BusinessRuleRepository businessRuleRepository;

    @Autowired
    BusinessRulesTestHelper businessRulesTestHelper;

    @Autowired
    BusinessRulesUtils businessRulesUtils;

    @BeforeEach
    void clearRepositoryData() {
        businessRuleRepository.deleteAll();
    }

    @Test
    void updateBusinessRulesWithExisting() throws Exception {
        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        List<BusinessRuleItem> businessRuleItems = new ArrayList<>();

        BusinessRuleItem businessRuleItem = new BusinessRuleItem();

        businessRuleItem.setHash(businessRulesUtils.calculateHash(BusinessRulesTestHelper.BR_DATA_1));
        businessRuleItem.setIdentifier(BusinessRulesTestHelper.BR_IDENTIFIER_1);
        businessRuleItem.setCountry(BusinessRulesTestHelper.BR_COUNTRY_1);
        businessRuleItem.setVersion(BusinessRulesTestHelper.BR_VERSION_1);
        businessRuleItem.setRawData(BusinessRulesTestHelper.BR_DATA_1);
        businessRuleItems.add(businessRuleItem);

        businessRuleService.updateBusinessRules(businessRuleItems);

        Assertions.assertEquals(1, businessRuleRepository.count());
    }

    @Test
    void updateBusinessRulesWithEmptyList() {
        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        List<BusinessRuleItem> businessRuleItems = new ArrayList<>();

        businessRuleService.updateBusinessRules(businessRuleItems);

        Assertions.assertEquals(0, businessRuleRepository.count());
    }

    @Test
    void updateBusinessRule() throws Exception {
        businessRulesTestHelper.insertBusinessRule(BusinessRulesTestHelper.BR_HASH_1,
            BusinessRulesTestHelper.BR_IDENTIFIER_1, BusinessRulesTestHelper.BR_COUNTRY_1,
            BusinessRulesTestHelper.BR_VERSION_1, BusinessRulesTestHelper.BR_DATA_1);

        List<BusinessRuleItem> businessRuleItems = new ArrayList<>();

        BusinessRuleItem businessRuleItem = new BusinessRuleItem();

        businessRuleItem.setHash(businessRulesUtils.calculateHash(BusinessRulesTestHelper.BR_DATA_1));
        businessRuleItem.setIdentifier(BusinessRulesTestHelper.BR_IDENTIFIER_1);
        businessRuleItem.setCountry(BusinessRulesTestHelper.BR_COUNTRY_1);
        businessRuleItem.setVersion(BusinessRulesTestHelper.BR_VERSION_1);
        businessRuleItem.setRawData(BusinessRulesTestHelper.BR_DATA_1);
        businessRuleItems.add(businessRuleItem);

        BusinessRuleItem Item2 = new BusinessRuleItem();

        Item2.setHash(businessRulesUtils.calculateHash(BusinessRulesTestHelper.BR_DATA_2));
        Item2.setIdentifier(BusinessRulesTestHelper.BR_IDENTIFIER_2);
        Item2.setCountry(BusinessRulesTestHelper.BR_COUNTRY_2);
        Item2.setVersion(BusinessRulesTestHelper.BR_VERSION_2);
        Item2.setRawData(BusinessRulesTestHelper.BR_DATA_2);
        businessRuleItems.add(Item2);

        businessRuleService.updateBusinessRules(businessRuleItems);

        Assertions.assertEquals(2, businessRuleRepository.count());

        businessRuleItems.remove(0);

        businessRuleService.updateBusinessRules(businessRuleItems);

        List<BusinessRuleEntity> result = businessRuleRepository.findAll();
        Assertions.assertEquals(1, result.size());

        BusinessRuleEntity resultEntity = result.get(0);
        Assertions.assertEquals(businessRulesUtils.calculateHash(BusinessRulesTestHelper.BR_DATA_2),
            resultEntity.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_IDENTIFIER_2, resultEntity.getIdentifier());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_COUNTRY_2, resultEntity.getCountry());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_VERSION_2, resultEntity.getVersion());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_DATA_2, resultEntity.getRawData());

        Optional<SignedListEntity> rules = signedListRepository.findById(ListType.Rules);
        assertTrue(rules.isPresent());
        assertNotNull(rules.get().getSignature());
    }


    @Test
    void createBusinessRuleItemList() throws Exception{
        List <ValidationRule> validationRules = new ArrayList<>();
        List<BusinessRuleItem> businessRuleItems;

        ValidationRule item1 = new ValidationRule();
        item1.setIdentifier(BusinessRulesTestHelper.BR_IDENTIFIER_1);
        item1.setVersion(BusinessRulesTestHelper.BR_VERSION_1);
        item1.setCountry(BusinessRulesTestHelper.BR_COUNTRY_1);
        item1.setRawJson(BusinessRulesTestHelper.BR_DATA_1);

        validationRules.add(item1);

        ValidationRule item2 = new ValidationRule();
        item2.setIdentifier(BusinessRulesTestHelper.BR_IDENTIFIER_3);
        item2.setVersion(BusinessRulesTestHelper.BR_VERSION_3);
        item2.setCountry(BusinessRulesTestHelper.BR_COUNTRY_3);
        item2.setRawJson(BusinessRulesTestHelper.BR_DATA_3);

        validationRules.add(item2);

        businessRuleItems = businessRuleService.createBusinessRuleItemList(validationRules);

        Assertions.assertEquals(2, businessRuleItems.size());

        BusinessRuleItem resultItem1 = businessRuleItems.get(0);
        Assertions.assertEquals(BusinessRulesTestHelper.BR_HASH_1, resultItem1.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_IDENTIFIER_1, resultItem1.getIdentifier());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_VERSION_1, resultItem1.getVersion());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_COUNTRY_1, resultItem1.getCountry());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_DATA_1, resultItem1.getRawData());

        BusinessRuleItem resultItem2 = businessRuleItems.get(1);
        Assertions.assertEquals(BusinessRulesTestHelper.BR_HASH_3, resultItem2.getHash());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_IDENTIFIER_3, resultItem2.getIdentifier());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_VERSION_3, resultItem2.getVersion());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_COUNTRY_3, resultItem2.getCountry());
        Assertions.assertEquals(BusinessRulesTestHelper.BR_DATA_3, resultItem2.getRawData());

    }
}
package eu.europa.ec.dgc.businessrule.mapper;

import eu.europa.ec.dgc.businessrule.model.BusinessRuleItem;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import eu.europa.ec.dgc.gateway.connector.model.ValidationRule;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;


@Mapper(componentModel = "spring")
public abstract class BusinessRuleMapper {

    @Autowired
    protected BusinessRulesUtils businessRulesUtils;

    /**
     * Maps a Validation rule to a business rule item.
     */
    public BusinessRuleItem map(ValidationRule validationRule) throws NoSuchAlgorithmException {
        BusinessRuleItem businessRuleItem = new BusinessRuleItem();

        businessRuleItem.setHash(businessRulesUtils.calculateHash(validationRule.getRawJson()));
        businessRuleItem.setIdentifier(validationRule.getIdentifier());
        businessRuleItem.setCountry(validationRule.getCountry());
        businessRuleItem.setVersion(validationRule.getVersion());
        businessRuleItem.setRawData(validationRule.getRawJson());

        return businessRuleItem;
    }

    ;

    public abstract List<BusinessRuleItem> map(List<ValidationRule> validationRules) throws NoSuchAlgorithmException;
}

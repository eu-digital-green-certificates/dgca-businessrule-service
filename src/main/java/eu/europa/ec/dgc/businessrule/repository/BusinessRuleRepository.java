package eu.europa.ec.dgc.businessrule.repository;

import eu.europa.ec.dgc.businessrule.entity.BusinessRuleEntity;
import eu.europa.ec.dgc.businessrule.entity.ValueSetEntity;
import eu.europa.ec.dgc.businessrule.restapi.dto.BusinessRuleListItemDto;
import eu.europa.ec.dgc.businessrule.restapi.dto.ValueSetListItemDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRuleRepository extends JpaRepository<BusinessRuleEntity, String> {

    List<BusinessRuleListItemDto> findAllByOrderByIdAsc();

    List<BusinessRuleListItemDto> findAllByCountryOrderByIdAsc(String country);

    BusinessRuleEntity findOneByHash(String hash);
}
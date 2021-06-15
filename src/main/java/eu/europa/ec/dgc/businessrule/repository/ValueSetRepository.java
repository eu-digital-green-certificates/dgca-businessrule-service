package eu.europa.ec.dgc.businessrule.repository;

import eu.europa.ec.dgc.businessrule.entity.ValueSetEntity;
import eu.europa.ec.dgc.businessrule.restapi.dto.ValueSetListItemDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValueSetRepository extends JpaRepository<ValueSetEntity, String> {
    List<ValueSetListItemDto> findAllByOrderByIdAsc();

    ValueSetEntity findOneByHash(String hash);
}
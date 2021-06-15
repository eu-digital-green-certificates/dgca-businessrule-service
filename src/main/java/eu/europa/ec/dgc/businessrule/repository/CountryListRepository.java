package eu.europa.ec.dgc.businessrule.repository;

import eu.europa.ec.dgc.businessrule.entity.CountryListEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryListRepository extends JpaRepository<CountryListEntity, Long> {

    CountryListEntity getFirstByIdNotNull();
}
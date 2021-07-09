package eu.europa.ec.dgc.businessrule.repository;

import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignedListRepository extends JpaRepository<SignedListEntity, ListType> {
}

package eu.europa.ec.dgc.businessrule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.restapi.dto.BusinessRuleListItemDto;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import liquibase.pro.packaged.T;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ListSigningService {
    private final SignedListRepository signedListRepository;
    private final MappingJackson2HttpMessageConverter jacksonHttpMessageConverter;
    private final Optional<SigningService> signingService;
    private final BusinessRulesUtils businessRulesUtils;

    public <T> void updateSignedList(List<T> list,ListType listType)  {
        try {
            String listRaw = jacksonHttpMessageConverter.getObjectMapper().writeValueAsString(list);
            String hash = businessRulesUtils.calculateHash(listRaw);
            Optional<SignedListEntity> ruleList = signedListRepository.findById(listType);
            if (ruleList.isEmpty()) {
                SignedListEntity signedListEntity = new SignedListEntity();
                signedListEntity.setListType(listType);
                signedListEntity.setHash(hash);
                signedListEntity.setRawData(listRaw);
                calculateSignature(signedListEntity);
                signedListRepository.save(signedListEntity);
            } else {
                if (!ruleList.get().getHash().equals(hash)) {
                    ruleList.get().setHash(hash);
                    calculateSignature(ruleList.get());
                    signedListRepository.save(ruleList.get());
                }
            }
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            log.error("can not create siglist",e);
        }
    }

    private void calculateSignature(SignedListEntity signedListEntity) {
        if (signingService.isPresent()) {
            signedListEntity.setSignature(signingService.get().computeSignature(signedListEntity.getHash()));
        } else {
            signedListEntity.setSignature("");
        }
    }

}

package eu.europa.ec.dgc.businessrule.service;

import eu.europa.ec.dgc.businessrule.entity.ValueSetEntity;
import eu.europa.ec.dgc.businessrule.repository.ValueSetRepository;
import eu.europa.ec.dgc.businessrule.restapi.dto.ValueSetListItemDto;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ValueSetService {

    private final BusinessRulesUtils businessRulesUtils;

    private final ValueSetRepository valueSetRepository;

    /**
     *  Saves a valueset.
     *
     */
    @Transactional
    public void saveValueSet(String valueSetName, String valueSetData) {
        String hash;
        try {
            hash = businessRulesUtils.calculateHash(valueSetData);
        } catch (NoSuchAlgorithmException e) {
            log.error("Calculation of hash failed:", e);
            return;
        }

        ValueSetEntity vse = new ValueSetEntity();
        vse.setId(valueSetName);
        vse.setRawData(valueSetData);
        vse.setHash(hash);

        valueSetRepository.save(vse);
    }

    /**
     *  gets list of all valueset ids and hashes.
     */
    public List<ValueSetListItemDto> getValueSetsList() {

        List<ValueSetListItemDto> valueSetItems = valueSetRepository.findAllByOrderByIdAsc();
        return valueSetItems;
    }


    /**
     *  Gets  valueset by hash.
     */
    @Transactional
    public ValueSetEntity getValueSetByHash(String hash) {

        return  valueSetRepository.findOneByHash(hash);
    }

}

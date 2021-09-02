/*-
 * ---license-start
 * eu-digital-green-certificates / dgca-businessrule-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.businessrule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.dgc.businessrule.entity.ListType;
import eu.europa.ec.dgc.businessrule.entity.SignedListEntity;
import eu.europa.ec.dgc.businessrule.repository.SignedListRepository;
import eu.europa.ec.dgc.businessrule.utils.BusinessRulesUtils;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
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

    /**
     * update or create signed list.
     * @param list list of elements
     * @param listType type of list
     * @param <T> type of list elem
     */
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
                    ruleList.get().setRawData(listRaw);
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

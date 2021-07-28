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

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.VaultTransitKey;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("vault-signing")
public class VaultTransitSigningService implements SigningService {
    private final VaultTemplate vaultTemplate;

    @Value("${dgc.signKey:businessrule}")
    private String signKey;

    @Override
    public String computeSignature(String hash) {
        String signature = vaultTemplate.opsForTransit().sign(signKey, Plaintext.of(hash))
                .getSignature();
        if (signature.startsWith("vault:v1:")) {
            signature = signature.substring(9);
        }
        return signature;
    }

    @Override
    public String getPublicKey() {
        VaultTransitKey publicKey = vaultTemplate.opsForTransit().getKey(signKey);
        String key = Integer.toString(publicKey.getLatestVersion());
        Map<String,String> keyValues = (Map<String, String>) publicKey.getKeys().get(key);
        String publicKeyStr = keyValues.get("public_key");
        // Remove -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY-----
        publicKeyStr = publicKeyStr.replaceAll("-----[A-Z ]+-----","");
        // Remove new lines too
        publicKeyStr = publicKeyStr.replaceAll("\\R","");
        return publicKeyStr;
    }
}

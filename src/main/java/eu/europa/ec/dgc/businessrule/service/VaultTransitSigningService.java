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

    @Value( "${dgc.signKey:businessrule}" )
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
        String public_key = keyValues.get("public_key");
        // Remove -----BEGIN PUBLIC KEY----- and -----END PUBLIC KEY-----
        public_key = public_key.replaceAll("-----[A-Z ]+-----","");
        // Remove new lines too
        public_key = public_key.replaceAll("\\R","");
        return public_key;
    }
}

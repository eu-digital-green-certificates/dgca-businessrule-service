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

import eu.europa.ec.dgc.businessrule.config.JksSigningConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("jks-signing")
public class JksSigningService implements SigningService {
    private final JksSigningConfig jksSigningConfig;

    private Certificate cert;
    private PrivateKey privateKey;

    /**
     * PostConstruct method to load KeyStore for issuing certificates.
     */
    @PostConstruct
    public void loadKeyStore() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        if (jksSigningConfig == null || jksSigningConfig.getKeyStorePassword() == null) {
            throw new IllegalArgumentException("missing configuration jwk-signing.keyStorePassword; "
                    + "can not init jks signing");
        }
        final char[] keyStorePassword = jksSigningConfig.getKeyStorePassword().toCharArray();
        final String keyName = jksSigningConfig.getCertAlias();

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        KeyStore keyStore = KeyStore.getInstance("JKS");

        File keyFile = new File(jksSigningConfig.getKeyStoreFile());
        if (!keyFile.isFile()) {
            log.error("keyfile not found on: {} please adapt the configuration property: jwk-signing.keyStoreFile",
                    keyFile);
            throw new IllegalArgumentException("keyfile not found on: " + keyFile
                    + " please adapt the configuration property: jwk-signing.keyStoreFile");
        }
        try (InputStream is = new FileInputStream(jksSigningConfig.getKeyStoreFile())) {
            final char[] privateKeyPassword = jksSigningConfig.getPrivateKeyPassword().toCharArray();
            keyStore.load(is, privateKeyPassword);
            KeyStore.PasswordProtection keyPassword =
                    new KeyStore.PasswordProtection(keyStorePassword);

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyName, keyPassword);
            cert = keyStore.getCertificate(keyName);
            privateKey = privateKeyEntry.getPrivateKey();
        }
    }

    @Override
    public String computeSignature(String hash) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initSign(privateKey);
            sig.update(hash.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalArgumentException("can not compute signature", e);
        }
    }

    @Override
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded());
    }
}

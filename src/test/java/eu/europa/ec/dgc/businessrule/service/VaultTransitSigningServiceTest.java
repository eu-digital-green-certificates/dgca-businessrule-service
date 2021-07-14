package eu.europa.ec.dgc.businessrule.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VaultTransitSigningServiceTest {
    @Test
    void testSignCheck() throws Exception {
        byte[] keyRaw = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEM9LJdncn3n0Hedj0G6AvUhbKA" +
                "cqDA7Xg16vZWOIvnNiEyxID/8xbPkN4iq2oqQxnrNHfXlxMao4Kz2p5xpdoQA==");
        String sig = "vault:v1:MEUCIEtd+LiMKz/PxrBNu3NcJt2T1ddaZ/H5gCMfYs69dWnYAiEAv9qfheA+Pxznvt2X22BcIApZJFW+q" +
                "+ARPLPi1bhXEi0=";
        byte[] sigBytes = Base64.getDecoder().decode(sig.substring(9));

        String hash = "e2ff17c59d32878232324a733f84a7dac6cdb535fa79c4cc9f1411886e581c1f";
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyRaw);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey ecPublicKey = keyFactory.generatePublic(spec);

        Signature sigInst = Signature.getInstance("SHA256withECDSA");
        sigInst.initVerify(ecPublicKey);
        sigInst.update(hash.getBytes(StandardCharsets.UTF_8));
        assertTrue(sigInst.verify(sigBytes));

    }
}
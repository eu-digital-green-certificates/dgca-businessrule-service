package eu.europa.ec.dgc.businessrule.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BusinessRulesUtils {

    /**
     * returns SHA-256 Thumbprint of the data (hex encoded).
     */
    public String calculateHash(String data) throws NoSuchAlgorithmException {
        return calculateHash(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * returns SHA-256 Thumbprint of the data (hex encoded).
     */
    public String calculateHash(byte[] data) throws NoSuchAlgorithmException {
        byte[] certHashBytes = MessageDigest.getInstance("SHA-256").digest(data);
        String hexString = new BigInteger(1, certHashBytes).toString(16);

        if (hexString.length() == 63) {
            hexString = "0" + hexString;
        }

        return hexString;
    }
}

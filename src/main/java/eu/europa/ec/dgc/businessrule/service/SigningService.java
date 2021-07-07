package eu.europa.ec.dgc.businessrule.service;

public interface SigningService {
    /**
     * compute hash.
     * As EC Curve is P256 (with SHA256 or "SHA256WITHECDSA" in Bouncycastle).
     * @param hash
     * @return ans1 base64 coded signature
     */
    String computeSignature(String hash);
}

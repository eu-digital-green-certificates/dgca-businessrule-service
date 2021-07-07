package eu.europa.ec.dgc.businessrule.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("jks-signing")
public class JksSigningConfig {
    private String keyStoreFile;
    private String keyStorePassword;
    private String certAlias;
    private String privateKeyPassword;
}

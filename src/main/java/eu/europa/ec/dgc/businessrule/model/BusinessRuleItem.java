package eu.europa.ec.dgc.businessrule.model;

import javax.persistence.Column;
import javax.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessRuleItem {

    private String hash;

    private String identifier;

    private String version;

    private String country;

    private String rawData;
}

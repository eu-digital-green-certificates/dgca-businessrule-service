package eu.europa.ec.dgc.businessrule.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "business_rules")
public class BusinessRuleEntity {

    /**
     * SHA-256 Thumbprint of the rule (hex encoded).
     */
    @Id
    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "identifier_name", nullable = false)
    private String identifier;

    @Column(name = "version", nullable = false)
    String version = "1.0.0";

    @Column(name = "country_code", nullable = false, length = 2)
    String country;

    @Lob
    @Column(name = "raw_data", nullable = false)
    String rawData;
}
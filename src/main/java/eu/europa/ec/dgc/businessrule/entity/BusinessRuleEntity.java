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
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "country_code", nullable = false, length = 2)
    String country;

    /**
     * SHA-256 Thumbprint of the rule (hex encoded).
     */
    @Column(name = "thumbprint", nullable = false, length = 64)
    private String hash;

    @Lob
    @Column(name = "raw_data", nullable = false)
    String rawData;
}
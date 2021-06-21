package eu.europa.ec.dgc.businessrule.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "valuesets")
public class ValueSetEntity {
    /**
     * SHA-256 Thumbprint of the valueset (hex encoded).
     */
    @Id
    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "identifier_name")
    private String id;

    @Lob
    @Column(name = "raw_data", nullable = false)
    String rawData;
}
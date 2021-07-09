package eu.europa.ec.dgc.businessrule.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "signed_list")
@AllArgsConstructor
@NoArgsConstructor
public class SignedListEntity {
    @Id
    @Column(name = "list_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ListType listType;

    @Column(name = "hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "signature", nullable = false, length = 256)
    private String signature;

    @Lob
    @Column(name = "raw_data", nullable = false)
    String rawData;
}

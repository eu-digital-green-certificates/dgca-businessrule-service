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

package eu.europa.ec.dgc.businessrule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
    String version;

    @Column(name = "country_code", nullable = false, length = 2)
    String country;

    @Lob
    @Column(name = "raw_data", nullable = false)
    String rawData;

    @Column(name = "signature", length = 256)
    private String signature;
}
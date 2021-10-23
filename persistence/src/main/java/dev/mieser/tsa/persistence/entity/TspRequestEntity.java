package dev.mieser.tsa.persistence.entity;

import dev.mieser.tsa.domain.HashAlgorithm;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Builder
@Table(name = "TSP_REQUEST")
public class TspRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private HashAlgorithm hashAlgorithm;

    private String hash;

    private String nonce;

    private boolean certificateRequested;

    private String tsaPolicyId;

    private String asnEncoded;

}

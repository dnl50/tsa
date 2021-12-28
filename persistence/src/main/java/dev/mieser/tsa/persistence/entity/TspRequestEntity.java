package dev.mieser.tsa.persistence.entity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import dev.mieser.tsa.domain.HashAlgorithm;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TSP_REQUEST")
public class TspRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private HashAlgorithm hashAlgorithm;

    @NotNull
    private String hash;

    private String nonce;

    private boolean certificateRequested;

    private String tsaPolicyId;

    @NotEmpty
    private String asnEncoded;

}

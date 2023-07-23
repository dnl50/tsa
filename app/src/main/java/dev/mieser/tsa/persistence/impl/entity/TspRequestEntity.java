package dev.mieser.tsa.persistence.impl.entity;

import java.math.BigInteger;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String hashAlgorithmIdentifier;

    @NotNull
    @Convert(converter = Base64Converter.class)
    private byte[] hash;

    @Convert(converter = HexAttributeConverter.class)
    private BigInteger nonce;

    private boolean certificateRequested;

    private String tsaPolicyId;

    @NotNull
    @Convert(converter = Base64Converter.class)
    private byte[] asnEncoded;

}

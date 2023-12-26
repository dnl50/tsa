package dev.mieser.tsa.persistence.impl.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import dev.mieser.tsa.domain.FailureInfo;
import dev.mieser.tsa.domain.ResponseStatus;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TSP_RESPONSE")
public class TspResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ResponseStatus status;

    private String statusString;

    @Enumerated(EnumType.STRING)
    private FailureInfo failureInfo;

    @NotNull
    private ZonedDateTime receptionTime;

    private ZonedDateTime generationTime;

    private Long serialNumber;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_ID")
    private TspRequestEntity request;

    @Convert(converter = Base64Converter.class)
    private byte[] asnEncoded;

}

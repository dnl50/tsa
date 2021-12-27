package dev.mieser.tsa.persistence.entity;

import dev.mieser.tsa.domain.FailureInfo;
import dev.mieser.tsa.domain.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_ID")
    private TspRequestEntity request;

    @NotEmpty
    private String asnEncoded;

}

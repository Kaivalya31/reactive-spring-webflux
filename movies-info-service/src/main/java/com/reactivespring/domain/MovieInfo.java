package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document//This is like an entity in RDBMS, 'Document' is a term used in MongoDB.
//Each entity is a Document in MongoDB as each resource is created as a Document in MongoDB.
public class MovieInfo {

    @Id
    private String movieInfoId;

    @NotNull
    @NotBlank(message = "Movie name can't be left empty")//Bean validation example
    private String name;

    @NotNull
    private List<@NotBlank(message = "You must mention atleast a single cast") String> cast;
    private LocalDate releaseDate;

    @NotNull
    @Positive
    private Integer year;

    @Positive(message = "IMDb rating can't be less than 0")
    private Double IMDbRating;
}

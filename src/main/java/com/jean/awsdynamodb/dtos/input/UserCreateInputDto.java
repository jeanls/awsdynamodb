package com.jean.awsdynamodb.dtos.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateInputDto {

    @NotNull
    @NotBlank
    @JsonProperty(value = "name")
    private String name;

    @NotNull
    @NotBlank
    @JsonProperty(value = "birth_date")
    private String birthDate;

    @NotNull
    @NotBlank
    @JsonProperty(value = "email")
    private String email;

    @NotNull
    @NotBlank
    @JsonProperty(value = "phone")
    private String phone;
}

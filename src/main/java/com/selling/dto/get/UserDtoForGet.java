package com.selling.dto.get;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class UserDtoForGet {
    private Long id;
    private String name;
    private String telephone;
    private String role;
    private String registration_date;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
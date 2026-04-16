package com.rochak.payflow.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long Id;
    private String name;
    private String email;
}

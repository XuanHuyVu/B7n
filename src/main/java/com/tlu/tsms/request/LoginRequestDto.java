package com.tlu.tsms.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto extends BaseReq {

    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "Password is required")
    private String password;

    private String userAgent;

    public static final LoginRequestDto EMPTY = LoginRequestDto.builder().build();
}

package com.iscode.coaching_fee_portal.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountsLoginRequest {

    private String email;
    private String password;
}
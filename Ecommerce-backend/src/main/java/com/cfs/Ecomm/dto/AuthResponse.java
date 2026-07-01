package com.cfs.Ecomm.dto;

import com.cfs.Ecomm.enums.Role;

public class AuthResponse {

    private String token;
    private Long   userId;
    private String name;
    private String email;
    private Role role;

    public AuthResponse() {}

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AuthResponse(String token, Long userId, String name, String email, Role role) {
        this.token  = token;
        this.userId = userId;
        this.name   = name;
        this.email  = email;
        this.role = role;
    }

    public String getToken()  { return token;  }
    public Long   getUserId() { return userId; }
    public String getName()   { return name;   }
    public String getEmail()  { return email;  }

    public void setToken(String token)   { this.token  = token;  }
    public void setUserId(Long userId)   { this.userId = userId; }
    public void setName(String name)     { this.name   = name;   }
    public void setEmail(String email)   { this.email  = email;  }

}

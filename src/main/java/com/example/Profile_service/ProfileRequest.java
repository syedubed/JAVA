package com.example.Profile_service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileRequest
   ( @NotBlank String name,
@NotBlank @Email String email,
@Size(max=500) String bio,

     @NotBlank
     String address,
     @NotBlank
     @Pattern(
             regexp = "^\\+[1-9]\\d{7,14}$",
             message = "Mobile number must use international format, for example +16025550123"
     )
     String mobileNumber
){
}
package com.onlineshop.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientDto(
        Long id,
        String name,
        String email,
        String address
){
}

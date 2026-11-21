package com.onlineshop.order.controller;

import com.onlineshop.order.dto.ClientDto;
import com.onlineshop.order.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Create a new client")
    @ApiResponse(responseCode = "201", description = "Client created successfully")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDto create(@Valid @RequestBody ClientDto dto) {
        return clientService.createClient(dto);
    }

    @Operation(summary = "Get all clients")
    @GetMapping
    public List<ClientDto> getAll() {
        return clientService.getAllClients();
    }

    @Operation(summary = "Get a client by ID")
    @GetMapping("/{id}")
    public ClientDto getById(@PathVariable("id") Long id) {
        return clientService.getClientById(id);
    }
}
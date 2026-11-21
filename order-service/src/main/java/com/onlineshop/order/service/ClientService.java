package com.onlineshop.order.service;

import com.onlineshop.order.dto.ClientDto;
import com.onlineshop.order.entity.ClientEntity;
import com.onlineshop.order.exception.ClientAlreadyExistsException;
import com.onlineshop.order.mapper.OrderMapper;
import com.onlineshop.order.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository repo;
    private final OrderMapper mapper;

    public ClientService(ClientRepository repo, OrderMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public ClientDto createClient(ClientDto dto) {
        String email = dto.email().trim();

        if (repo.existsByEmailIgnoreCase(email)) {
            throw new ClientAlreadyExistsException(email);
        }

        ClientEntity client = new ClientEntity(
                dto.name().trim(),
                email,
                dto.address().trim()
        );

        return mapper.toClientDto(repo.save(client));
    }

    public List<ClientDto> getAllClients() {
        return repo.findAll()
                .stream()
                .map(mapper::toClientDto)
                .toList();
    }

    public ClientDto getClientById(Long id) {
        return mapper.toClientDto(repo.requireById(id));
    }
}
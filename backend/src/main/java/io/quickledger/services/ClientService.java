package io.quickledger.services;

import io.quickledger.dto.ClientDto;
import io.quickledger.entities.Client;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.mappers.ClientMapper;
import io.quickledger.repositories.ClientRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

//import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

@Service
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private ClientRepository clientRepository;
    private UserCompanyService userCompanyService;
    private ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository,
            UserCompanyService userCompanyService, ClientMapper clientMapper) {
        this.userCompanyService = userCompanyService;
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    public Page<ClientDto> getAllClients(Long companyId, User user, Pageable pageable) {
        userCompanyService.checkUserBelongs(user, companyId);
        Sort sort = pageable.getSort().and(Sort.by(Sort.Order.desc("modifiedDate")));
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Client> clients = clientRepository.findAll(sortedPageable);
        return clients.map(c -> clientMapper.toDto(c));

    }

    public List<ClientDto> search(Long companyId, String phone, String email, String entityName, String contactName,
            String contactSurname) {
        Pageable pageable = PageRequest.of(0, 10);
        // how do i add companyId to the search?

        List<Client> clients = clientRepository.search(companyId, phone, email, entityName, contactName, contactSurname,
                pageable);
        return clients.stream()
                .map(ClientMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    public List<ClientDto> search(Long companyId, String searchTerm) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Client> clients = clientRepository.search(companyId, searchTerm, pageable);
        return clients.stream()
                .map(ClientMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    /*
     * TODO
     * at some point in the future we should allow merges of client when they are
     * created with the same email or phone...
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClientDto saveClient(ClientDto dto, Long companyId) {
        if (StringUtils.isBlank(dto.getEmail())) {
            throw new IllegalArgumentException("Client email is mandatory");
        }
        if (StringUtils.isBlank(dto.getPhone())) {
            throw new IllegalArgumentException("Client phone is mandatory");
        }

        Client client = ClientMapper.INSTANCE.toEntity(dto);
        client.setCompany(new Company(companyId));
        logger.debug("Client to save: {}", client);
        if (!StringUtils.isBlank(client.getEmail())) {
            client.setEmail(client.getEmail().toLowerCase());
        }

        client = clientRepository.save(client);
        dto.setId(client.getId());
        return dto;
    }

    public void deleteClient(Long clientId, User user, Long companyId) {
        userCompanyService.checkUserBelongs(user, companyId);
        clientRepository.deleteById(clientId);
    }

    public ClientDto getClient(Long companyId, User user, Long clientId) {
        userCompanyService.checkUserBelongs(user, companyId);
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new RuntimeException("Client not found"));
        return clientMapper.toDto(client);
    }
}

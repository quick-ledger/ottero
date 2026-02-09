package io.quickledger.controllers;

import io.quickledger.dto.ClientDto;
import io.quickledger.entities.User;
import io.quickledger.security.UserIdAuth;
import io.quickledger.services.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies/{companyId}/clients")
public class ClientController {

    private final ClientService clientService;
    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<Page<ClientDto>> getAllClients(@PathVariable Long companyId, @UserIdAuth final User user, Pageable pageable) {
        Page<ClientDto> clients = clientService.getAllClients(companyId, user, pageable);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Long companyId, @UserIdAuth final User user, @PathVariable Long clientId) {
        ClientDto clients = clientService.getClient(companyId, user, clientId);
        return ResponseEntity.ok(clients);
    }

    //TODO this might be called with duplicate clients. make sure silently perform replace and merge. let's discuss quote page usecase. RG
    @RequestMapping( method = { RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<ClientDto> saveClient(@PathVariable Long companyId, @RequestBody ClientDto clientDto) {
        ClientDto savedClient = clientService.saveClient(clientDto, companyId);
        return ResponseEntity.ok(savedClient);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> search(@PathVariable Long companyId, @RequestParam(required = true) String searchTerm) {
        List<ClientDto> clients = clientService.search(companyId, searchTerm);
        return ResponseEntity.ok(clients);
    }


    @DeleteMapping("/{clientId}")
    public void delete(@PathVariable Long companyId, @UserIdAuth final User user, @PathVariable Long clientId) {
        clientService.deleteClient(clientId, user, companyId);
    }

}

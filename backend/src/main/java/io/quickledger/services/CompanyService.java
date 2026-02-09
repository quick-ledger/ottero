package io.quickledger.services;

import io.quickledger.dto.product.ProductDefinitionDto;
import io.quickledger.dto.SequenceConfigDTO;
import io.quickledger.entities.*;
import io.quickledger.exception.BusinessException;
import io.quickledger.mappers.product.ProductDefinitionMapper;
import io.quickledger.repositories.CompanyRepository;
import io.quickledger.repositories.UserCompanyRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    // private final ProductDefinitionRepository productDefinitionRepository;
    private final ProductDefinitionMapper productDefinitionMapper;
    private final UserService userService;
    private final UserCompanyService userCompanyService;
    private final SequenceConfigService sequenceConfigService;

    @Autowired
    public CompanyService(CompanyRepository companyRepository, UserCompanyRepository userCompanyRepository,
            ProductDefinitionMapper productDefinitionMapper,
            UserService userService, UserCompanyService userCompanyService,
            SequenceConfigService sequenceConfigService) {
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.productDefinitionMapper = productDefinitionMapper;
        this.userService = userService;
        this.userCompanyService = userCompanyService;
        this.sequenceConfigService = sequenceConfigService;
    }

    public Iterable<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompaniesByUser(Long userId) {
        List<UserCompany> userCompanies = userCompanyRepository.findByUserId(userId);
        return userCompanies.stream()
                .map(UserCompany::getCompany)
                .collect(Collectors.toList());
    }

    // TODO MBH check this
    public List<ProductDefinitionDto> getProductDefinitionsForCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Company Id %d not found", companyId)));
        return productDefinitionMapper.toDtoList(company.getProductDefinitions());
    }

    public Company saveCompany(Company company) {
        Company fromDB = companyRepository.findById(company.getId()).get();
        company.setImage(fromDB.getImage());
        // Preserve Stripe fields from DB
        company.setStripeConnectedAccountId(fromDB.getStripeConnectedAccountId());
        company.setStripeChargesEnabled(fromDB.isStripeChargesEnabled());
        // Preserve Template Config from DB
        company.setTemplateConfig(fromDB.getTemplateConfig());
        return companyRepository.save(company);
    }

    @Transactional
    public Company createCompanyWithUser(Company company, User user, String groupName) {
        try {
            User thisUser = userService.getUserById(user.getId()).get();

            List<UserCompany> existingCompanies = userCompanyRepository.findByUserId(user.getId());
            /*
             * TEMPORARY RESTRICTION: One Company Per User
             * 
             * For the MVP, we are restricting users to a single company.
             * 
             * Future Considerations:
             * 1. Multi-Company Support: We will likely support multiple companies per user
             * in the future.
             * 2. Billing Refactor: Currently, subscriptions are linked to the User entity.
             * To support multiple companies, we may need to move billing to the Company
             * entity
             * (per-company pricing) or allow a 'Pro' user tier that includes multiple
             * companies.
             * 3. Accountants/Bookkeepers: This user persona naturally requires access to
             * multiple companies.
             * 
             * When lifting this restriction, remove this check and implement a
             * "Company Switcher" in the UI.
             */
            if (!existingCompanies.isEmpty()) {
                throw new BusinessException("User is restricted to one company.");
            }
            // set the default company for the user if it is not set
            if (thisUser.getDefaultCompany() == null) {
                thisUser.setDefaultCompany(company);
                userService.saveUser(thisUser);
            }

            company = companyRepository.save(company);
            UserCompanyId userCompanyId = new UserCompanyId();
            userCompanyId.setUserId(user.getId());
            userCompanyId.setCompanyId(company.getId());

            UserCompany userCompany = new UserCompany();
            userCompany.setId(userCompanyId);
            userCompany.setUser(user);
            userCompany.setCompany(company);
            userCompany.setRole(UserRole.ROLE_OWNER.getRole());

            userCompanyRepository.save(userCompany);

            // create default sequences
            SequenceConfigDTO defaultInvoiceSeq = new SequenceConfigDTO(SequenceConfig.EntityType.INVOICE,
                    company.getId(), "I-", "", 0, 4);
            SequenceConfigDTO defaultQuoteSeq = new SequenceConfigDTO(SequenceConfig.EntityType.QUOTE, company.getId(),
                    "Q-", "", 0, 4);

            sequenceConfigService.createOrUpdateSequenceConfig(company.getId(), defaultInvoiceSeq);
            sequenceConfigService.createOrUpdateSequenceConfig(company.getId(), defaultQuoteSeq);

            return company;
        } catch (DataAccessException e) {
            logger.error("Failed to create company or user or update keycloak: ", e);
            throw new RuntimeException(e);
        }
    }

    public Company findCompanyById(Long companyId) {
        logger.debug("Entering findCompanyById with companyId: {}", companyId);
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Company Id %d not found", companyId)));
    }

    public List<Company> findCompaniesByUser(Long userId) {
        List<UserCompany> userCompanies = userCompanyRepository.findByUserId(userId);
        return userCompanies.stream()
                .map(UserCompany::getCompany)
                .collect(Collectors.toList());
    }

    public void deleteCompany(Long companyId, User user) {
        /*
         * 1- check user has access to company
         * 2- delete userCompany
         * 3- delete company
         */
        UserCompany uc = userCompanyRepository.findByUserId(user.getId()).stream()
                .filter(userCompany -> userCompany.getCompany().getId().equals(companyId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("You do not have access to this company"));

        if (uc.getRole().equalsIgnoreCase(UserRole.ROLE_OWNER.getRole())) {
            userCompanyRepository.delete(uc);
            companyRepository.deleteById(companyId);
        } else {
            throw new BusinessException("You do not have permission to delete this company");
        }
    }
}
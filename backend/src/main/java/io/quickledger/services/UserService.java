package io.quickledger.services;

import io.quickledger.dto.UserDto;
import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.exception.BusinessException;
import io.quickledger.repositories.CompanyRepository;
import io.quickledger.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public UserService(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void getUserByUserId(Long id) {
        userRepository.findById(id);
    }

    public User createUserFromKcEvent(String firstName, String lastName, String email, String username,
            LocalDateTime registerDateTime) {
        try {
            logger.debug("===> Creating user: " + username);
            // Create a new User object and set the fields
            User user = new User();
            user.setUsername(username);
            user.setName(firstName);
            user.setSurname(lastName);
            user.setEmail(email);
            user.setRegisterDateTime(registerDateTime);
            user = saveUser(user);
            // INFO - commenting this since we no longer need it as user context has user
            // details based on username and fetch from DB anyway
            // keycloakAdminService.addUserAttributeDbId(user.getUsername(),
            // user.getId().toString());

            // Save the user to the database
            logger.debug("===> User created with username: " + username);
            return user;
        } catch (DataAccessException e) {
            logger.error("Failed to create user: " + username, e);
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId);
    }

    public User updateUser(Long userID, UserDto userDto) {
        // we better get the user by id from db and then update it with dto values. RG
        // TODO we have to change the mappers so that when dto fields are null, they are
        // not updated in entity. RG
        // TODO validate if user has access to the company. RG
        Optional<User> existingUserOptional = getUserById(userID);
        if (!existingUserOptional.isPresent()) {
            throw new BusinessException("User with id " + userID + " not found");
        }
        User existingUser = existingUserOptional.get();

        if (userDto.getDefaultCompany() != null && userDto.getDefaultCompany().getId() != null) {
            Optional<Company> companyOptional = companyRepository.findById(userDto.getDefaultCompany().getId());
            if (!companyOptional.isPresent()) {
                throw new BusinessException("Company with id " + userDto.getDefaultCompany().getId() + " not found");
            }
            existingUser.setDefaultCompany(companyOptional.get());
        }
        // TODO we have to set other fields as well BEFORE CHECKING FOR NULL. RG
        return saveUser(existingUser);
    }

    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setSurname(user.getSurname());
        userDto.setEmail(user.getEmail());
        userDto.setSubscriptionPlan(user.getSubscriptionPlan());
        userDto.setSubscriptionStatus(user.getSubscriptionStatus());
        userDto.setStripeCustomerId(user.getStripeCustomerId());

        return userDto;
    }
}

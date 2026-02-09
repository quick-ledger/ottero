package io.quickledger.services;

import io.quickledger.entities.Company;
import io.quickledger.entities.User;
import io.quickledger.entities.UserCompany;
import io.quickledger.repositories.UserCompanyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCompanyService {

    private UserCompanyRepository userCompanyRepository;

    public UserCompanyService(UserCompanyRepository userCompanyRepository) {
        this.userCompanyRepository = userCompanyRepository;
    }

    // check user and company relation exists. we have to user this on most APIs!
    public boolean checkUserBelongs(User user, Long companyID) {
        Optional<UserCompany> output = userCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyID);
        if (!output.isPresent())
            throw new RuntimeException("User " + user.getId() + " does not have access to company " + companyID
                    + ". Please check user_companies table.");
        return true;
    }

    public UserCompany saveUserCompany(UserCompany userCompany) {
        return userCompanyRepository.save(userCompany);
    }

    // Add other methods as needed...
}
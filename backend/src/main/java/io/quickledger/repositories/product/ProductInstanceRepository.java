package io.quickledger.repositories.product;

import io.quickledger.entities.product.ProductInstance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductInstanceRepository extends CrudRepository<ProductInstance, Long> {
}
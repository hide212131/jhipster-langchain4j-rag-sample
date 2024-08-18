package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.DocumentAsset;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DocumentAsset entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DocumentAssetRepository extends JpaRepository<DocumentAsset, Long> {}

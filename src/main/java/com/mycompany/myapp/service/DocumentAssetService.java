package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DocumentAsset;
import com.mycompany.myapp.repository.DocumentAssetRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.DocumentAsset}.
 */
@Service
@Transactional
public class DocumentAssetService {

    private static final Logger log = LoggerFactory.getLogger(DocumentAssetService.class);

    private final DocumentAssetRepository documentAssetRepository;

    private final RAGService ragService;

    public DocumentAssetService(DocumentAssetRepository documentAssetRepository, RAGService ragService) {
        this.documentAssetRepository = documentAssetRepository;
        this.ragService = ragService;
    }

    /**
     * Save a documentAsset.
     *
     * @param documentAsset the entity to save.
     * @return the persisted entity.
     */
    public DocumentAsset save(DocumentAsset documentAsset) {
        log.debug("Request to save DocumentAsset : {}", documentAsset);
        documentAssetRepository.save(documentAsset);
        return ragService.storeFile(documentAsset);
    }

    /**
     * Update a documentAsset.
     *
     * @param documentAsset the entity to save.
     * @return the persisted entity.
     */
    public DocumentAsset update(DocumentAsset documentAsset) {
        log.debug("Request to update DocumentAsset : {}", documentAsset);
        documentAssetRepository.save(documentAsset);
        return ragService.storeFile(documentAsset);
    }

    /**
     * Partially update a documentAsset.
     *
     * @param documentAsset the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DocumentAsset> partialUpdate(DocumentAsset documentAsset) {
        log.debug("Request to partially update DocumentAsset : {}", documentAsset);

        return documentAssetRepository
            .findById(documentAsset.getId())
            .map(existingDocumentAsset -> {
                if (documentAsset.getFilename() != null) {
                    existingDocumentAsset.setFilename(documentAsset.getFilename());
                }
                if (documentAsset.getData() != null) {
                    existingDocumentAsset.setData(documentAsset.getData());
                }
                if (documentAsset.getDataContentType() != null) {
                    existingDocumentAsset.setDataContentType(documentAsset.getDataContentType());
                }

                return existingDocumentAsset;
            })
            .map(documentAssetRepository::save);
    }

    /**
     * Get all the documentAssets.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DocumentAsset> findAll() {
        log.debug("Request to get all DocumentAssets");
        return documentAssetRepository.findAll();
    }

    /**
     * Get one documentAsset by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DocumentAsset> findOne(Long id) {
        log.debug("Request to get DocumentAsset : {}", id);
        return documentAssetRepository.findById(id);
    }

    /**
     * Delete the documentAsset by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete DocumentAsset : {}", id);
        ragService.deleteFile(id);
        documentAssetRepository.deleteById(id);
    }
}

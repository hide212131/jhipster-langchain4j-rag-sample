package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.domain.DocumentAsset;
import com.mycompany.myapp.repository.DocumentAssetRepository;
import com.mycompany.myapp.service.DocumentAssetService;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.DocumentAsset}.
 */
@RestController
@RequestMapping("/api/document-assets")
public class DocumentAssetResource {

    private static final Logger log = LoggerFactory.getLogger(DocumentAssetResource.class);

    private static final String ENTITY_NAME = "documentAsset";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DocumentAssetService documentAssetService;

    private final DocumentAssetRepository documentAssetRepository;

    public DocumentAssetResource(DocumentAssetService documentAssetService, DocumentAssetRepository documentAssetRepository) {
        this.documentAssetService = documentAssetService;
        this.documentAssetRepository = documentAssetRepository;
    }

    /**
     * {@code POST  /document-assets} : Create a new documentAsset.
     *
     * @param documentAsset the documentAsset to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new documentAsset, or with status {@code 400 (Bad Request)} if the documentAsset has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DocumentAsset> createDocumentAsset(@Valid @RequestBody DocumentAsset documentAsset) throws URISyntaxException {
        log.debug("REST request to save DocumentAsset : {}", documentAsset);
        if (documentAsset.getId() != null) {
            throw new BadRequestAlertException("A new documentAsset cannot already have an ID", ENTITY_NAME, "idexists");
        }
        documentAsset = documentAssetService.save(documentAsset);
        return ResponseEntity.created(new URI("/api/document-assets/" + documentAsset.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, documentAsset.getId().toString()))
            .body(documentAsset);
    }

    /**
     * {@code PUT  /document-assets/:id} : Updates an existing documentAsset.
     *
     * @param id the id of the documentAsset to save.
     * @param documentAsset the documentAsset to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentAsset,
     * or with status {@code 400 (Bad Request)} if the documentAsset is not valid,
     * or with status {@code 500 (Internal Server Error)} if the documentAsset couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentAsset> updateDocumentAsset(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DocumentAsset documentAsset
    ) throws URISyntaxException {
        log.debug("REST request to update DocumentAsset : {}, {}", id, documentAsset);
        if (documentAsset.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentAsset.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentAssetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        documentAsset = documentAssetService.update(documentAsset);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentAsset.getId().toString()))
            .body(documentAsset);
    }

    /**
     * {@code PATCH  /document-assets/:id} : Partial updates given fields of an existing documentAsset, field will ignore if it is null
     *
     * @param id the id of the documentAsset to save.
     * @param documentAsset the documentAsset to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated documentAsset,
     * or with status {@code 400 (Bad Request)} if the documentAsset is not valid,
     * or with status {@code 404 (Not Found)} if the documentAsset is not found,
     * or with status {@code 500 (Internal Server Error)} if the documentAsset couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DocumentAsset> partialUpdateDocumentAsset(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DocumentAsset documentAsset
    ) throws URISyntaxException {
        log.debug("REST request to partial update DocumentAsset partially : {}, {}", id, documentAsset);
        if (documentAsset.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, documentAsset.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!documentAssetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DocumentAsset> result = documentAssetService.partialUpdate(documentAsset);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, documentAsset.getId().toString())
        );
    }

    /**
     * {@code GET  /document-assets} : get all the documentAssets.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of documentAssets in body.
     */
    @GetMapping("")
    public List<DocumentAsset> getAllDocumentAssets() {
        log.debug("REST request to get all DocumentAssets");
        return documentAssetService.findAll();
    }

    /**
     * {@code GET  /document-assets/:id} : get the "id" documentAsset.
     *
     * @param id the id of the documentAsset to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the documentAsset, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentAsset> getDocumentAsset(@PathVariable("id") Long id) {
        log.debug("REST request to get DocumentAsset : {}", id);
        Optional<DocumentAsset> documentAsset = documentAssetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(documentAsset);
    }

    /**
     * {@code DELETE  /document-assets/:id} : delete the "id" documentAsset.
     *
     * @param id the id of the documentAsset to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentAsset(@PathVariable("id") Long id) {
        log.debug("REST request to delete DocumentAsset : {}", id);
        documentAssetService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}

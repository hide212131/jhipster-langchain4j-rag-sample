package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.DocumentAssetAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.DocumentAsset;
import com.mycompany.myapp.repository.DocumentAssetRepository;
import jakarta.persistence.EntityManager;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DocumentAssetResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DocumentAssetResourceIT {

    private static final String DEFAULT_FILENAME = "AAAAAAAAAA";
    private static final String UPDATED_FILENAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_DATA = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_DATA = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_DATA_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_DATA_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/document-assets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DocumentAssetRepository documentAssetRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDocumentAssetMockMvc;

    private DocumentAsset documentAsset;

    private DocumentAsset insertedDocumentAsset;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentAsset createEntity(EntityManager em) {
        DocumentAsset documentAsset = new DocumentAsset()
            .filename(DEFAULT_FILENAME)
            .data(DEFAULT_DATA)
            .dataContentType(DEFAULT_DATA_CONTENT_TYPE);
        return documentAsset;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DocumentAsset createUpdatedEntity(EntityManager em) {
        DocumentAsset documentAsset = new DocumentAsset()
            .filename(UPDATED_FILENAME)
            .data(UPDATED_DATA)
            .dataContentType(UPDATED_DATA_CONTENT_TYPE);
        return documentAsset;
    }

    @BeforeEach
    public void initTest() {
        documentAsset = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedDocumentAsset != null) {
            documentAssetRepository.delete(insertedDocumentAsset);
            insertedDocumentAsset = null;
        }
    }

    @Test
    @Transactional
    void createDocumentAsset() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the DocumentAsset
        var returnedDocumentAsset = om.readValue(
            restDocumentAssetMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentAsset)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DocumentAsset.class
        );

        // Validate the DocumentAsset in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertDocumentAssetUpdatableFieldsEquals(returnedDocumentAsset, getPersistedDocumentAsset(returnedDocumentAsset));

        insertedDocumentAsset = returnedDocumentAsset;
    }

    @Test
    @Transactional
    void createDocumentAssetWithExistingId() throws Exception {
        // Create the DocumentAsset with an existing ID
        documentAsset.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDocumentAssetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentAsset)))
            .andExpect(status().isBadRequest());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkFilenameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        documentAsset.setFilename(null);

        // Create the DocumentAsset, which fails.

        restDocumentAssetMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentAsset)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDocumentAssets() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        // Get all the documentAssetList
        restDocumentAssetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(documentAsset.getId().intValue())))
            .andExpect(jsonPath("$.[*].filename").value(hasItem(DEFAULT_FILENAME)))
            .andExpect(jsonPath("$.[*].dataContentType").value(hasItem(DEFAULT_DATA_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].data").value(hasItem(Base64.getEncoder().encodeToString(DEFAULT_DATA))));
    }

    @Test
    @Transactional
    void getDocumentAsset() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        // Get the documentAsset
        restDocumentAssetMockMvc
            .perform(get(ENTITY_API_URL_ID, documentAsset.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(documentAsset.getId().intValue()))
            .andExpect(jsonPath("$.filename").value(DEFAULT_FILENAME))
            .andExpect(jsonPath("$.dataContentType").value(DEFAULT_DATA_CONTENT_TYPE))
            .andExpect(jsonPath("$.data").value(Base64.getEncoder().encodeToString(DEFAULT_DATA)));
    }

    @Test
    @Transactional
    void getNonExistingDocumentAsset() throws Exception {
        // Get the documentAsset
        restDocumentAssetMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDocumentAsset() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentAsset
        DocumentAsset updatedDocumentAsset = documentAssetRepository.findById(documentAsset.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDocumentAsset are not directly saved in db
        em.detach(updatedDocumentAsset);
        updatedDocumentAsset.filename(UPDATED_FILENAME).data(UPDATED_DATA).dataContentType(UPDATED_DATA_CONTENT_TYPE);

        restDocumentAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDocumentAsset.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedDocumentAsset))
            )
            .andExpect(status().isOk());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDocumentAssetToMatchAllProperties(updatedDocumentAsset);
    }

    @Test
    @Transactional
    void putNonExistingDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, documentAsset.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentAsset))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(documentAsset))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(documentAsset)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDocumentAssetWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentAsset using partial update
        DocumentAsset partialUpdatedDocumentAsset = new DocumentAsset();
        partialUpdatedDocumentAsset.setId(documentAsset.getId());

        partialUpdatedDocumentAsset.filename(UPDATED_FILENAME).data(UPDATED_DATA).dataContentType(UPDATED_DATA_CONTENT_TYPE);

        restDocumentAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocumentAsset.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocumentAsset))
            )
            .andExpect(status().isOk());

        // Validate the DocumentAsset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentAssetUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedDocumentAsset, documentAsset),
            getPersistedDocumentAsset(documentAsset)
        );
    }

    @Test
    @Transactional
    void fullUpdateDocumentAssetWithPatch() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the documentAsset using partial update
        DocumentAsset partialUpdatedDocumentAsset = new DocumentAsset();
        partialUpdatedDocumentAsset.setId(documentAsset.getId());

        partialUpdatedDocumentAsset.filename(UPDATED_FILENAME).data(UPDATED_DATA).dataContentType(UPDATED_DATA_CONTENT_TYPE);

        restDocumentAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocumentAsset.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDocumentAsset))
            )
            .andExpect(status().isOk());

        // Validate the DocumentAsset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDocumentAssetUpdatableFieldsEquals(partialUpdatedDocumentAsset, getPersistedDocumentAsset(partialUpdatedDocumentAsset));
    }

    @Test
    @Transactional
    void patchNonExistingDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, documentAsset.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentAsset))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(documentAsset))
            )
            .andExpect(status().isBadRequest());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDocumentAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        documentAsset.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDocumentAssetMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(documentAsset)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DocumentAsset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDocumentAsset() throws Exception {
        // Initialize the database
        insertedDocumentAsset = documentAssetRepository.saveAndFlush(documentAsset);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the documentAsset
        restDocumentAssetMockMvc
            .perform(delete(ENTITY_API_URL_ID, documentAsset.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return documentAssetRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected DocumentAsset getPersistedDocumentAsset(DocumentAsset documentAsset) {
        return documentAssetRepository.findById(documentAsset.getId()).orElseThrow();
    }

    protected void assertPersistedDocumentAssetToMatchAllProperties(DocumentAsset expectedDocumentAsset) {
        assertDocumentAssetAllPropertiesEquals(expectedDocumentAsset, getPersistedDocumentAsset(expectedDocumentAsset));
    }

    protected void assertPersistedDocumentAssetToMatchUpdatableProperties(DocumentAsset expectedDocumentAsset) {
        assertDocumentAssetAllUpdatablePropertiesEquals(expectedDocumentAsset, getPersistedDocumentAsset(expectedDocumentAsset));
    }
}

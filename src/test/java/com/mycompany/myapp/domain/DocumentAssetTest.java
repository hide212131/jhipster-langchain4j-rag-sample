package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.DocumentAssetTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DocumentAssetTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DocumentAsset.class);
        DocumentAsset documentAsset1 = getDocumentAssetSample1();
        DocumentAsset documentAsset2 = new DocumentAsset();
        assertThat(documentAsset1).isNotEqualTo(documentAsset2);

        documentAsset2.setId(documentAsset1.getId());
        assertThat(documentAsset1).isEqualTo(documentAsset2);

        documentAsset2 = getDocumentAssetSample2();
        assertThat(documentAsset1).isNotEqualTo(documentAsset2);
    }
}

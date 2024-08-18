package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DocumentAssetTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static DocumentAsset getDocumentAssetSample1() {
        return new DocumentAsset().id(1L).filename("filename1");
    }

    public static DocumentAsset getDocumentAssetSample2() {
        return new DocumentAsset().id(2L).filename("filename2");
    }

    public static DocumentAsset getDocumentAssetRandomSampleGenerator() {
        return new DocumentAsset().id(longCount.incrementAndGet()).filename(UUID.randomUUID().toString());
    }
}

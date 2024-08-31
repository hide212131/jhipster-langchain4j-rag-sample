package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.DocumentAsset;
import com.mycompany.myapp.domain.graphrag.Entity;
import com.mycompany.myapp.repository.DocumentAssetRepository;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mycompany.myapp.service.graphrag.EntityExtractionAssistant;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class RAGService {

    private final DocumentAssetRepository documentAssetRepository;
    private final EmbeddingStore<TextSegment> vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final EntityExtractionAssistant entityExtractionAssistant;

    public RAGService(
        DocumentAssetRepository documentAssetRepository,
        EmbeddingStore<TextSegment> vectorStore,
        JdbcTemplate jdbcTemplate,
        EmbeddingModel embeddingModel,
        EntityExtractionAssistant entityExtractionAssistant
    ) {
        this.documentAssetRepository = documentAssetRepository;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.entityExtractionAssistant = entityExtractionAssistant;
    }

    public DocumentAsset storeFile(DocumentAsset savedFile) {

        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(new ByteArrayInputStream(savedFile.getData()));
        document.metadata().put("documentAssetId", savedFile.getId().toString());

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        Gson gson = new Gson();
        String examples = gson.toJson(Arrays.asList(
            new Entity("name1", "type1", "description1"),
            new Entity("name2", "type2", "description2")
        ));

        List<Entity> entities = segments.stream()
            .map(TextSegment::text)
            .map(text -> entityExtractionAssistant.extractEntities(text, List.of("Person", "Organization", "Location"), "en", examples))
            .reduce(new ArrayList<>(), (acc, list) -> {
                acc.addAll(list);
                return acc;
            });

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        vectorStore.addAll(embeddings, segments);
        return savedFile;
    }

    public void deleteFile(Long id) {
        DocumentAsset document = documentAssetRepository.findById(id).orElse(null);
        if (document != null) {
            String deleteSql = "DELETE FROM vector_store WHERE metadata->>'documentAssetId' = ?";
            jdbcTemplate.update(deleteSql, document.getId().toString());
            documentAssetRepository.deleteById(id);
        }
    }

    public List<ChatMessage> retrieveAndGeneratePrompt(List<ChatMessage> instructions) {
        UserMessage lastUserMessage = null;
        for (int i = instructions.size() - 1; i >= 0; i--) {
            if (instructions.get(i) instanceof UserMessage) {
                lastUserMessage = (UserMessage) instructions.get(i);
                Embedding queryEmbedding = embeddingModel.embed(lastUserMessage.singleText()).content();
                EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding).build();
                EmbeddingSearchResult<TextSegment> results = vectorStore.search(embeddingSearchRequest);
                String references = results.matches().stream()
                    .map(EmbeddingMatch::embedded).map(TextSegment::text).collect(Collectors.joining("\n"));
                var newInstructions = new ArrayList<>(instructions);
                String newMessage =
                    "Please answer the questions in the 'UserMessage'. Find the information you need to answer in the 'References' section. If you do not have the information, please answer with 'I don't know'.\n" +
                    "UserMessage: " +
                    lastUserMessage.singleText() +
                    "\n" +
                    "References: " +
                    references;
                System.out.println("newMessage: " + newMessage);
                newInstructions.set(i, new UserMessage(newMessage));
                return newInstructions;
            }
        }
        return instructions;
    }
}

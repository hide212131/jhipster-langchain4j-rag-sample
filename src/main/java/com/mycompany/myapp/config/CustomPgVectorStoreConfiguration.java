package com.mycompany.myapp.config;

import com.mycompany.myapp.service.Assistant;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;

@Configuration
public class CustomPgVectorStoreConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties firstDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource firstDataSource(DataSourceProperties firstDataSourceProperties) {
        return firstDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.ai.vectorstore.pgvector.customize.datasource")
    public DataSourceProperties secondDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.ai.vectorstore.pgvector.customize.datasource.hikari")
    public HikariDataSource secondDataSource(@Qualifier("secondDataSourceProperties") DataSourceProperties secondDataSourceProperties) {
        return secondDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public JdbcTemplate firstJdbcTemplate(DataSource secondaryDataSource) {
        return new JdbcTemplate(secondaryDataSource);
    }

    @Bean
    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondDataSource") DataSource secondaryDataSource) {
        return new JdbcTemplate(secondaryDataSource);
    }

    @Bean
    public PgVectorEmbeddingStore vectorStore(
        EmbeddingModel embeddingModel,
        @Qualifier("secondDataSource") DataSource secondaryDataSource
    ) {
        return PgVectorEmbeddingStore.datasourceBuilder().datasource(secondaryDataSource)
            .table("vector_store")
            .dimension(embeddingModel.dimension())
            .build();
    }

    @Bean
    public Assistant contentRetrieverService(PgVectorEmbeddingStore embeddingStore, EmbeddingModel embeddingModel
    , StreamingChatLanguageModel streamingChatLanguageModel) {
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
            .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
            .build();

        return AiServices.builder(Assistant.class)
            .streamingChatLanguageModel(streamingChatLanguageModel)
            .contentRetriever(contentRetriever)
            .build();
    }
}

package com.mycompany.myapp.service.graphrag;

import com.mycompany.myapp.domain.graphrag.Entity;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface EntityExtractionAssistant {
    @UserMessage("""
        -Goal-
        Given a text document that is potentially relevant to this activity and a list of entity types, identify all entities of those types from the text and all relationships among the identified entities.

        -Steps-
        1. Identify all entities. For each identified entity, extract the following information:
        - entity_name: Name of the entity, capitalized
        - entity_type: One of the following types: [{{entity_types}}]
        - entity_description: Comprehensive description of the entity's attributes and activities
        Format each entity output as a JSON entry with the following format. Do not enclose the output in markdown tags, only json:

        {"name": <entity name>, "type": <type>, "description": <entity description>}

        2. From the entities identified in step 1, identify all pairs of (source_entity, target_entity) that are *clearly related* to each other.
        For each pair of related entities, extract the following information:
        - source_entity: name of the source entity, as identified in step 1
        - target_entity: name of the target entity, as identified in step 1
        - relationship_description: explanation as to why you think the source entity and the target entity are related to each other
        - relationship_strength: an integer score between 1 to 10, indicating strength of the relationship between the source entity and target entity
        Format each relationship as a JSON entry with the following format:

        {"source": <source_entity>, "target": <target_entity>, "relationship": <relationship_description>, "relationship_strength": <relationship_strength>}

        3. Return output in {{language}} as a single list of all JSON entities and relationships identified in steps 1 and 2. If you have to translate, just translate the descriptions, nothing else!

        -Examples-
        ######################
        {{examples}}

        -Real Data-
        ######################
        entity_types: {{entity_types}}
        text: {{input_text}}
        ######################
        output:
        """)
    List<Entity> extractEntities(@V("input_text") String inputText, @V("entity_types") List<String> entityTypes, @V("language") String language, @V("examples") String examples);
}

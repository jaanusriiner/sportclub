package ee.sportclub.controller.ask.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BenchmarkAskRequest(

        @NotBlank(message = "Question must not be blank")
        @Size(max = 500, message = "Question must be at most 500 characters")
        String question,

        // ajalugu ei piirata - andmestik on system promptis ja vestlus peab säilima tervikuna
        @Valid
        List<AskHistoryEntry> history,

        // paneeli vestluse id (FE genereerib, uueneb vestluse tühjendamisel); selle järgi hoitakse vestluse andmestikku
        @NotBlank(message = "Conversation id must not be blank")
        @Size(max = 36, message = "Conversation id must be at most 36 characters")
        String conversationId,

        @NotNull(message = "LLM model must be selected")
        Integer llmModelId,

        @NotNull(message = "Temperature must not be null")
        @DecimalMin(value = "0.0", message = "Temperature must be at least 0.0")
        @DecimalMax(value = "2.0", message = "Temperature must be at most 2.0")
        Double temperature,

        @NotNull(message = "Tone must be selected")
        Integer toneId,

        // null = mudeli vaikimisi mõtlemistase; mudelitele, mis seda ei toeta, ei saadeta
        @Pattern(regexp = "MINIMAL|LOW|MEDIUM|HIGH", message = "Thinking level must be MINIMAL, LOW, MEDIUM or HIGH")
        String thinkingLevel

) {}

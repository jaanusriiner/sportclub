package ee.sportclub.controller.ask.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BenchmarkAskRequest(

        @NotBlank(message = "Question must not be blank")
        @Size(max = 500, message = "Question must be at most 500 characters")
        String question,

        // paneeli vestluse id (FE genereerib, uueneb vestluse tühjendamisel). Selle järgi hoiab server vestluse
        // ajalugu (BenchmarkConversationMemory) ja andmestikku - klient ajalugu ei saada. UUID, et id-d ei saaks arvata.
        @NotBlank(message = "Conversation id must not be blank")
        @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                message = "Conversation id must be a UUID")
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

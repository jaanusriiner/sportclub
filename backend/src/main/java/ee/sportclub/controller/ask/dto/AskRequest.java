package ee.sportclub.controller.ask.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record AskRequest(

        @NotBlank(message = "Question must not be blank")
        @Size(max = 500, message = "Question must be at most 500 characters")
        String question,

        @Valid
        @Size(max = 5, message = "History must contain at most 5 entries")
        List<AskHistoryEntry> history

) {}

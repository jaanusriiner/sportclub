package ee.sportclub.controller.ask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AskHistoryEntry(

        @NotBlank(message = "History question must not be blank")
        @Size(max = 500, message = "History question must be at most 500 characters")
        String question,

        @NotBlank(message = "History answer must not be blank")
        @Size(max = 2000, message = "History answer must be at most 2000 characters")
        String answer

) {}

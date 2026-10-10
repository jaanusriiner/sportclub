package ee.sportclub.controller.llmmodel;

import ee.sportclub.persistence.llmmodel.LlmModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link LlmModel}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmModelDto implements Serializable {
    Integer llmModelId;
    String provider;
    String modelName;
    BigDecimal maxTemperature;
    Boolean supportsTemperature;
    Boolean supportsThinkingLevel;
}

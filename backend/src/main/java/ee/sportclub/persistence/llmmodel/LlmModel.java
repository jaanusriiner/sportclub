package ee.sportclub.persistence.llmmodel;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "llm_model", schema = "sportclub")
public class LlmModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "input_price_usd", nullable = false, precision = 10, scale = 4)
    private BigDecimal inputPriceUsd;

    @NotNull
    @Column(name = "cached_input_price_usd", nullable = false, precision = 10, scale = 4)
    private BigDecimal cachedInputPriceUsd;

    // null = vahemällu kirjutamise eest eraldi tasu ei ole (Gemini, OpenAI)
    @Column(name = "cache_write_price_usd", precision = 10, scale = 4)
    private BigDecimal cacheWritePriceUsd;

    @NotNull
    @Column(name = "output_price_usd", nullable = false, precision = 10, scale = 4)
    private BigDecimal outputPriceUsd;

    @NotNull
    @Column(name = "max_temperature", nullable = false, precision = 2, scale = 1)
    private BigDecimal maxTemperature;

    @NotNull
    @Column(name = "supports_temperature", nullable = false)
    private Boolean supportsTemperature;

    @NotNull
    @Column(name = "supports_thinking_level", nullable = false)
    private Boolean supportsThinkingLevel;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}

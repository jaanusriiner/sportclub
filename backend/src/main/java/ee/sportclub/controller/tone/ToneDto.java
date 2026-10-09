package ee.sportclub.controller.tone;

import ee.sportclub.persistence.tone.Tone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link Tone}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ToneDto implements Serializable {
    Integer toneId;
    String toneName;
}

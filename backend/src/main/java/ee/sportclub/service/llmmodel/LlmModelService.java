package ee.sportclub.service.llmmodel;

import ee.sportclub.controller.llmmodel.LlmModelDto;
import ee.sportclub.infrastructure.exception.PrimaryKeyNotFoundException;
import ee.sportclub.persistence.llmmodel.LlmModel;
import ee.sportclub.persistence.llmmodel.LlmModelMapper;
import ee.sportclub.persistence.llmmodel.LlmModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmModelService {

    private final LlmModelRepository llmModelRepository;
    private final LlmModelMapper llmModelMapper;

    public List<LlmModelDto> findActiveLlmModels() {
        List<LlmModel> llmModels = llmModelRepository.findActiveLlmModels();
        return llmModelMapper.toLlmModelDtos(llmModels);
    }

    public LlmModel getValidLlmModelBy(Integer llmModelId) {
        return llmModelRepository.findById(llmModelId)
                .orElseThrow(() -> new PrimaryKeyNotFoundException("llmModelId", llmModelId));
    }
}

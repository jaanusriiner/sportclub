package ee.sportclub.controller.area;

import ee.sportclub.controller.area.dto.AreaDto;
import ee.sportclub.service.area.AreaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AreaController.class)
class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AreaService areaService;

    @Test
    void findAreas_returns200AndAreasInServiceOrder() throws Exception {
        when(areaService.findAreas()).thenReturn(List.of(
                new AreaDto(1, "Harjumaa"),
                new AreaDto(4, "Hiiumaa"),
                new AreaDto(2, "Läänemaa"),
                new AreaDto(5, "Pärnumaa"),
                new AreaDto(3, "Saaremaa")));

        mockMvc.perform(get("/api/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].areaId").value(1))
                .andExpect(jsonPath("$[0].areaName").value("Harjumaa"))
                .andExpect(jsonPath("$[1].areaName").value("Hiiumaa"))
                .andExpect(jsonPath("$[2].areaId").value(2))
                .andExpect(jsonPath("$[2].areaName").value("Läänemaa"))
                .andExpect(jsonPath("$[3].areaName").value("Pärnumaa"))
                .andExpect(jsonPath("$[4].areaId").value(3))
                .andExpect(jsonPath("$[4].areaName").value("Saaremaa"));
    }

    @Test
    void findAreas_returns200AndEmptyArrayWhenNoAreasExist() throws Exception {
        when(areaService.findAreas()).thenReturn(List.of());

        mockMvc.perform(get("/api/areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

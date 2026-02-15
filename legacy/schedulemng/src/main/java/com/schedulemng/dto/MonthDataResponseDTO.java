package com.schedulemng.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.schedulemng.entity.SelectedDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class MonthDataResponseDTO {

    /*  직관적인 변수명을 위해 is를 사용했으나 Jackson에서 json으로 변환시 is를 제외시킴
    JsonProperty를 추가하여 변수명 유지 */
    @JsonProperty("isAdmin")
    private boolean isAdmin;

    private List<SelectedDate> data;
}

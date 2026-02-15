package co.com.bancolombia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFranchiseNameRequest {

    @NotBlank(message = "Franchise name is required")
    @Size(max = 100, message = "Franchise name must not exceed 100 characters")
    private String name;
}

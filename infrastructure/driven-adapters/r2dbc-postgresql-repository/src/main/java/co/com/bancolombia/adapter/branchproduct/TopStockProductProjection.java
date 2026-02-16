package co.com.bancolombia.adapter.branchproduct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopStockProductProjection {

    @Column("product_id")
    private Long productId;

    @Column("product_name")
    private String productName;

    private Integer stock;

    @Column("branch_id")
    private Long branchId;

    @Column("branch_name")
    private String branchName;
}

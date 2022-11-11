package uctech.Unimed.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class ComplementoSolicitacaoDTO {
    private BigDecimal item;
    private String complemento;
    private String qtdSolic;
    private String qtdAut;
    private String situacao;
}

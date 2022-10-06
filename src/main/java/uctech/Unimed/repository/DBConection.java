package uctech.Unimed.repository;

import org.springframework.stereotype.Repository;
import uctech.Unimed.dtos.BeneficiarioDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class DBConection {

    @PersistenceContext
    private EntityManager entityManager;

    public List<BeneficiarioDTO> getBeneficiarioByCpfOrCarteirinha(String cpfOrCarteirinha) {
        String whereClause = "";
        if (cpfOrCarteirinha.length() > 11) {
           whereClause = whereClause.concat("and dbaunimed.f_formata_cartao_dig_num(b.uni_cod_respon," +
                    "       b.bnf_cod_cntrat_cart," +
                    "       b.bnf_cod, b.bnf_cod_depnte) = '").concat(cpfOrCarteirinha).concat("'");
        } else {
           whereClause = whereClause.concat("and trim(pd.doc_nro) = '").concat(cpfOrCarteirinha).concat("'");
        }


        Query query = entityManager.createNativeQuery("select trim(pd.doc_nro) cpf," +
                "       dbaunimed.f_formata_cartao_dig_num(b.uni_cod_respon," +
                "       b.bnf_cod_cntrat_cart," +
                "       b.bnf_cod, b.bnf_cod_depnte) cartao," +
                "       initcap(p.pes_nom_comp) nome," +
                "       p.pes_nom_comp nome_completo," +
                "       trunc(dbaunimed.k_geral.f_verif_null(p.pes_dat_nasc)) data_nascimento," +
                "       trunc(dbaunimed.k_geral.f_verif_null(b.bnf_dat_excl)) data_exclusao," +
                "       trunc(b.bnf_dat_inic_vigen) data_inicio_vigencia," +
                "       substr(trim(dbaunimed.f_busca_mask_param('DM_IND_GRAU_DEPCIA'," +
                "                                                b.bnf_ind_grau_depcia))," +
                "              4) grau_dependencia," +
                "       trim(cv.plano_nro_reg_ans) plano_id," +
                "       p.pes_ind_sexo sexo" +
                "  from dbaunimed.bnfrio       b," +
                "       dbaunimed.pessoa_doc   pd," +
                "       dbaunimed.pessoa       p," +
                "       dbaunimed.cntrat_venda cv" +
                " where b.bnf_cod_pessoa = p.pes_cod " +
                whereClause +
                "   and pd.pes_cod = p.pes_cod" +
                "   and pd.tpdoc_cod = 2" +
                "   and cv.cv_nro = b.cv_nro");

        List<Object[]> rows = query.getResultList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<BeneficiarioDTO> list = new ArrayList<>();

        for (Object[] obj : rows) {
            list.add(new BeneficiarioDTO(
                    (String) obj[0],
                    (String) obj[1],
                    (String) obj[2],
                    (String) obj[3],
                    (Date) obj[4],
                    (Date) obj[5],
                    (Date) obj[6],
                    (String) obj[7],
                    (String) obj[8],
                    (String) obj[9]
            ));
        }

        return list;
    }

}
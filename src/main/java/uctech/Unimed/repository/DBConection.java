package uctech.Unimed.repository;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import uctech.Unimed.dtos.*;
import uctech.Unimed.exception.DataNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
@Transactional
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
            whereClause = whereClause.concat("and lpad(trim(pd.doc_nro), 11, 0) = '").concat(cpfOrCarteirinha).concat("'");
        }


        Query query = entityManager.createNativeQuery("select lpad(trim(pd.doc_nro), 11, 0) cpf," +
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

        List<BeneficiarioDTO> list = new ArrayList<>();

        for (Object[] obj : rows) {
            list.add(new BeneficiarioDTO(
                    (String) obj[0],
                    (String) obj[1],
                    (String) obj[2],
                    (String) obj[3],
                    formataDate(obj[4]),
                    formataDate(obj[5]),
                    formataDate(obj[6]),
                    (String) obj[7],
                    (String) obj[8],
                    (String) obj[9]
            ));
        }

        return list;
    }

    private LocalDate formataDate(Object o) {
        if (!Objects.isNull(o)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(sdf.format((Date) o), formatter);
        }
        return null;
    }

    public BeneficiarioCartaoDTO getBeneficiarioBoleto(String cartao) throws DataNotFoundException {

        Query query = entityManager.createNativeQuery("select dbenef.cartao," +
                " dbenef.nome_completo," +
                " dbenef.grau_dependencia," +
                " dbenef.tipo_contrato," +
                " (select cpf from dbaunimed.v_ud178_dados_beneficiario where cartao = dbenef.cartao_titular), " +
                " dbenef.cartao_titular " +
                " from dbaunimed.v_ud178_dados_beneficiario dbenef" +
                " where 1=1" +
                " and tipo_contrato = 'PF'" +
                " and cartao = '" + cartao.trim() + "'");

        try {
            Object[] row = (Object[]) query.getSingleResult();
            return new BeneficiarioCartaoDTO((String) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4], (String) row[5]);

        } catch (Exception e) {
            throw new DataNotFoundException("Beneficiário não encontrado");
        }
    }

    public BigDecimal getBoletosAtrasadosApartir60Dias(String cartao) {

        Query query = entityManager.createNativeQuery("select COUNT(*) " +
                "       from dbaunimed.v_ud178_mobile_fatura mf " +
                "       where mf.usuario_cartao = '" + cartao + "'" +
                "       and (trunc(current_date) - mf.VENCIMENTO_SEGUNDA_VIA) > 59" +
                "       and mf.valor_pago = '0.00'");

        return (BigDecimal) query.getSingleResult();
    }

    public BoletoPathDTO getDadosBoletoPendente(String cartaoTitular) throws DataNotFoundException {

        Query query = entityManager.createNativeQuery("select d.PRCPD_DES_DIR_REDE, d.PRCPD_DES_CAMINH " +
                " from dbaunimed.prcsso_param_doc d " +
                "inner join dbaunimed.prcsso_param_fatura_rec f " +
                " on f.prcp_cod = d.prcp_cod " +
                "where 1=1 " +
                " and d.prcpd_des_dir_oracle = 'DIR_REL' " +
                " and d.prcpd_des like ('%CAPA FATURA CONTRA TITULAR%') " +
                " and f.fr_nro = (select FR_NRO " +
                " from dbaunimed.v_ud178_mobile_fatura " +
                " where usuario_cartao = '" + cartaoTitular + "' " +
                "      and FR_SITUACAO = 'Pendente' " +
                "      and VALOR_PAGO = '0.00') " +
                "order by 2 desc");

        try {
            Object[] row = (Object[]) query.getSingleResult();
            return new BoletoPathDTO((String) row[0], (String) row[1]);

        } catch (Exception e) {
            throw new DataNotFoundException("Boleto não encontrado");
        }


    }


    public List<BoletoDTO> getCodigoDeBarras(String cartao) throws DataNotFoundException {

        Query query = entityManager.createNativeQuery("select usuario_cartao," +
                "       competencia_segunda_via," +
                "       vencimento_segunda_via," +
                "       codigo_barras" +
                " FROM dbaunimed.v_ud178_mobile_fatura mf" +
                " WHERE mf.usuario_cartao = '" + cartao + "'" +
                "  AND (trunc(CURRENT_DATE) - mf.VENCIMENTO_SEGUNDA_VIA) < 59" +
                "  AND mf.valor_pago = '0.00'");

        try {
            return query.getResultList();
        } catch (Exception e) {
            throw new DataNotFoundException("Email não encontrado");
        }


    }

    public GuiaDTO getStatusGuia(String numeroGuia) {
        Query query = entityManager.createNativeQuery("   select aut.nr_guia NUMERO_GUIA," +
                "       DECODE(aut.cd_situacao," +
                "              1,'Negada'," +
                "              2,'Aprovada'," +
                "              3,'Em estudo'," +
                "              4,'Cancelada'," +
                "              5,'Executada'," +
                "              6,'Aguardando Cancelamento') STATUS_GUIA" +
                "  from datacenter.autsc2_solicitacoes aut" +
                "  where aut.nr_guia = '" + numeroGuia + "'");

        Object[] row = (Object[]) query.getSingleResult();

        if (ObjectUtils.isEmpty(row)) {
            return null;
        }


        return new GuiaDTO((String) row[0], (String) row[1]);
    }

    public EmailDTO getEmailByCpf(String cpf) throws DataNotFoundException {

        Query query = entityManager.createNativeQuery("select distinct " +
                "       dbaunimed.f_formata_cartao_dig_num(b.uni_cod_respon,b.bnf_cod_cntrat_cart,b.bnf_cod,b.bnf_cod_depnte) AS cartao_benef," +
                "       pe.pes_nom_comp NOME," +
                "       dbaunimed.f_format_cnpj_cpf('CPF',dbaunimed.f_busca_doc_pessoa(pe.pes_cod,'CPF')) cpf," +
                "       lower(pec.CON_DES_EMAIL) EMAIL," +
                "       lower(pec.con_des_email_altern) EMAIL_ALTERNATIVO   " +
                "             " +
                "  from dbaunimed.pessoa               pe," +
                "       dbaunimed.bnfrio               b," +
                "       dbaunimed.pessoa_end_contat    pec" +
                " where pec.pes_cod = pe.pes_cod" +
                "       and pec.pes_cod = b.bnf_cod_pessoa " +
                "       and b.bnf_cod_pessoa = pe.pes_cod" +
                "       and pec.end_ind = 1" +
                "       and dbaunimed.k_geral.F_VERIF_NULL(pec.CON_DES_EMAIL) is not null" +
                "       and dbaunimed.f_busca_doc_pessoa(pe.pes_cod,'CPF') = '" + cpf + "'");

        try {
            Object[] row = (Object[]) query.getSingleResult();
            return new EmailDTO((String) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4]);

        } catch (Exception e) {
            throw new DataNotFoundException("Email não encontrado");
        }

    }


    public BeneficiarioSolicitacaoDTO getBeneficiarioSolicitacao(String cod) throws DataNotFoundException {
        Query query1 = entityManager.createNativeQuery("SELECT S.CD_BENEF_COMPLETO Beneficiario," +
                "       S.NM_COMPLETO_PESSOA NOME_COMPLETO," +
                "       S.NM_PRESTADOR SOLICITANTE," +
                "       TO_CHAR(S.DT_SOLICITACAO, 'DD/MM/RRRR') SOLICITACAO," +
                "       TO_CHAR(S.DT_VALIDADE_SENHA, 'DD/MM/RRRR') VALIDADE" +
                "  FROM datacenter.v_autsc_solic_resultado S" +
                " WHERE S.CD_SOLICITACAO = '" + cod + "'" +
                " ORDER BY S.DT_VALIDADE_SENHA");

        try {
            Object[] row = (Object[]) query1.getSingleResult();
            return new BeneficiarioSolicitacaoDTO((String) row[0], (String) row[1], (String) row[2], (String) row[3], (String) row[4]);

        } catch (Exception e) {
            throw new DataNotFoundException("Lembrete de solicitação não encontrado");
        }
    }


    public List<ComplementoSolicitacaoDTO> getComplementoSolicitacao(String cod) throws DataNotFoundException {
        Query query2 = entityManager.createNativeQuery("select s.cd_item_servico ITEM," +
                "       t.ds_item COMPLEMENTO," +
                "       to_char(s.nr_qtd_solic,'9999999') QTD_SOLIC," +
                "       to_char(s.nr_qtd,'9999999') QTD_AUT," +
                "       decode (s.cd_situacao_item," +
                "       '1', 'Negado', '2', 'Aprovado', '3', 'Em estudo', '4', 'Cancelado', '5', 'Executado') as Situacao" +
                "  from datacenter.autsc2_solic_itens s" +
                " inner join ud178.sce_cfg_itens t on t.cd_item = s.cd_item_servico" +
                " inner join datacenter.autsc2_solicitacoes a on a.cd_solicitacao = s.cd_solicitacao" +
                " where s.cd_solicitacao = '" + cod + "'");

        return query2.getResultList();

    }

    public ObservacaoSolicitacaoDTO getObservacaoSolicitacao(String cod) throws DataNotFoundException {

        Query query3 = entityManager.createNativeQuery("SELECT ASOL.DS_OBS OBSERVACAO," +
                "       CB.DT_NASCIMENTO DATA_DE_NASC," +
                "       DS_INDIC_CLINICA INDICACAO_CLINICA" +
                " FROM datacenter.AUTSC2_SOLICITACOES ASOL, datacenter.CM_BENEF CB" +
                " WHERE ASOL.CD_UNIMED = CB.CD_UNIMED" +
                "   AND ASOL.CD_CONTRATO_CARTAO = CB.CD_CARTEIRA" +
                "   AND ASOL.CD_BENEF = CB.CD_FAMI" +
                "   AND ASOL.CD_DEPEN = CB.CD_DEPEN" +
                "   AND ASOL.CD_SOLICITACAO = '" + cod + "'");

        try {
            Object[] row = (Object[]) query3.getSingleResult();
            return new ObservacaoSolicitacaoDTO((String) row[0], (Date) row[1], (String) row[2]);

        } catch (Exception e) {
            throw new DataNotFoundException("Lembrete de solicitação não encontrado");
        }
    }

    public Object getValorMensalidade(String cpf) throws DataNotFoundException {

        Query query = entityManager.createNativeQuery("select valor_total" +
                " from dbaunimed.v_demonstrativo_ir_pago p" +
                " where not exists" +
                " (select 1" +
                " from dbaunimed.param_valor pv" +
                " WHERE pv.parsi_cod = rpad('CF_BLOQUE_ANO_DEMONS_IR_WEB', 45)" +
                " AND TRIM(pv.prval_des_val) = p.ano)" +
                " and lpad(trim(p.cpf_titular), 11, 0) = '" + cpf + "'");


        try {
            return query.getSingleResult();

        } catch (Exception e) {
            throw new DataNotFoundException("Valor não encontrado");
        }

    }

    public List<PlanoDTO> getPlanoByCpfOrCard(String cpfOrCard) throws DataNotFoundException {
        String whereClause = "";
        if (cpfOrCard.length() > 11) {
            whereClause = whereClause.concat("and dbaunimed.f_formata_cartao_dig_num(b.uni_cod_respon," +
                    " b.bnf_cod_cntrat_cart," +
                    " b.bnf_cod, b.bnf_cod_depnte) = '" + cpfOrCard + "'");
        } else {
            whereClause = whereClause.concat("and lpad(trim(pd.doc_nro), 11, 0) = '").concat(cpfOrCard).concat("'");
        }


        Query query = entityManager.createNativeQuery("select lpad(trim(pd.doc_nro), 11, 0)," +
                "dbaunimed.f_formata_cartao_dig_num(b.uni_cod_respon," +
                "b.bnf_cod_cntrat_cart," +
                "b.bnf_cod, b.bnf_cod_depnte) cartao," +
                "trim(cv.plano_nro_reg_ans) plano_id" +
                " from dbaunimed.bnfrio       b," +
                "dbaunimed.pessoa_doc   pd," +
                "dbaunimed.pessoa       p," +
                "dbaunimed.cntrat_venda cv" +
                " where b.bnf_cod_pessoa = p.pes_cod " +
                whereClause +
                " and pd.pes_cod = p.pes_cod" +
                " and pd.tpdoc_cod = 2" +
                " and cv.cv_nro = b.cv_nro");

        try {
            return query.getResultList();
        } catch (Exception e) {
            throw new DataNotFoundException("Plano não encontrado");
        }

    }

}




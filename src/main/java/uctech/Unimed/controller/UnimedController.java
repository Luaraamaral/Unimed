package uctech.Unimed.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import uctech.Unimed.dtos.*;
import uctech.Unimed.exception.DataNotFoundException;
import uctech.Unimed.repository.DBConection;
import uctech.Unimed.service.UnimedService;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@RestController
@CrossOrigin("*")
public class UnimedController {

    @Autowired
    private DBConection dbConection;

    @Autowired
    private UnimedService unimedService;

    @GetMapping("/getBeneficiario")
    public ResponseEntity<List<BeneficiarioDTO>> getBeneficiario(@RequestParam("cpfOrCard") String cpfOrCard) {
        List<BeneficiarioDTO> beneficiarios = unimedService.getBeneficiarioByCpfOrCarteirinha(cpfOrCard);
        if (ObjectUtils.isEmpty(beneficiarios)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(beneficiarios);
    }

    @GetMapping("/getBoletosAbertos")
    public ResponseEntity<?> getBoletosAbertos(@RequestParam("cartao") String cartao, @RequestParam(value = "cpf", required = false) String cpf, HttpServletResponse response) throws DataNotFoundException, IOException {

        unimedService.getBoletosAbertos(cartao, cpf, response);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/getStatusGuia")
    public ResponseEntity<GuiaDTO> getStatusGuia(@RequestParam("numeroGuia") String numeroGuia) {
        GuiaDTO guiaDTO = unimedService.getStatusGuia(numeroGuia);
        if (ObjectUtils.isEmpty(guiaDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(guiaDTO);
    }

    @GetMapping("/getEmail")
    public ResponseEntity<EmailDTO> getEmailByCpf(@RequestParam("cpf") String cpf) throws DataNotFoundException {
        EmailDTO emailDTO = unimedService.getEmailByCpf(cpf);
        if (ObjectUtils.isEmpty(emailDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(emailDTO);
    }


    @GetMapping("/getCodigoDeBarras")
    public ResponseEntity<List<BoletoDTO>> getCodigoDeBarras(@RequestParam("cartao") String cartao) throws DataNotFoundException {
        List<BoletoDTO> boletoDTO = unimedService.getCodigoDeBarras(cartao);
        if (ObjectUtils.isEmpty(boletoDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(boletoDTO);
    }

    @GetMapping(value = "/imposto/{cpfOrCartao}")
    public ModelAndView listarData(@PathVariable("cpfOrCartao") String cpfOrCartao) {
        ModelAndView andView = new ModelAndView("impostoModel");
        return andView;
    }

    @GetMapping("/getBeneficiarioSolicitacao")
    public ResponseEntity<BeneficiarioSolicitacaoDTO> getBeneficiarioSolicitacao(@RequestParam("cod") String cod) throws DataNotFoundException {
        BeneficiarioSolicitacaoDTO beneficiarioSolicitacaoDTO = dbConection.getBeneficiarioSolicitacao(cod);
        if (ObjectUtils.isEmpty(beneficiarioSolicitacaoDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(beneficiarioSolicitacaoDTO);
    }

    @GetMapping("/getComplementoSolicitacao")
    public ResponseEntity<List<ComplementoSolicitacaoDTO>> getComplementoSolicitacao(@RequestParam("cod") String cod) throws DataNotFoundException {
        List<ComplementoSolicitacaoDTO> complementoSolicitacaoDTO = dbConection.getComplementoSolicitacao(cod);
        if (ObjectUtils.isEmpty(complementoSolicitacaoDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(complementoSolicitacaoDTO);
    }

    @GetMapping("/getObservacaoSolicitacao")
    public ResponseEntity<ObservacaoSolicitacaoDTO> getObservacaoSolicitacao(@RequestParam("cod") String cod) throws DataNotFoundException {
        ObservacaoSolicitacaoDTO observacaoSolicitacaoDTO = dbConection.getObservacaoSolicitacao(cod);
        if (ObjectUtils.isEmpty(observacaoSolicitacaoDTO)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(observacaoSolicitacaoDTO);
    }

    @GetMapping("/getValorMensalidade")
    public ResponseEntity<?> getValorMensalidade(@RequestParam("cpf") String cartao, @RequestParam(value = "cpf", required = false) String cpf, HttpServletResponse response) throws DataNotFoundException, IOException {

        unimedService.getValorMensalidade(cpf);

        return ResponseEntity.ok(unimedService.getValorMensalidade(cpf));
    }

    @GetMapping("/getPlanoByCpfOrCard")
    public String getPlanoByCpfOrCard(@RequestParam("cpfOrCard") String cpfOrCard) throws DataNotFoundException, JSONException, IOException {
        JsonReader json = new JsonReader();
        JSONObject json2 = json.readJson("http://187.17.144.244:8443/getBeneficiario?cpfOrCard="+cpfOrCard);;

        String plano = (String) json2.get("planoId");
        String tipoPlano = "Livre";
        switch (plano){
            case "476363167":
            case "476364165":
            case "476365163":
            case "476400165":
                tipoPlano = "Essencial";
                break;
            case "478669176":
            case "478670170":
            case "478671178":
            case "478672176":
                tipoPlano = "Especial";
                break;
        }
        return tipoPlano;

    }

    @GetMapping("/EnvioPdfEmail")
    public ResponseEntity<?> getJustEmailByCard(@RequestParam("e-mail") String email, @RequestParam(value = "cartaoTitular") String cartaoTitular, HttpServletResponse response) throws DataNotFoundException, IOException {
        EnvioPDF post = new EnvioPDF();

        BoletoPathDTO boletoPath = dbConection.getDadosBoletoPendente(cartaoTitular);
        String pdfName = boletoPath.getFileName();

        String filePath = "C:/PDF_BOLETOS/";
        String originalFileName = pdfName;
        String newFileName = "test.pdf";

        byte[] input_file = Files.readAllBytes(Paths.get(filePath + originalFileName));

        byte[] encodedBytes = Base64.getEncoder().encode(input_file);
        String encodedString = new String(encodedBytes);
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString.getBytes());

        FileOutputStream fos = new FileOutputStream(filePath + newFileName);
        fos.write(decodedBytes);
        fos.flush();
        fos.close();

        String attachment = encodedString;
        String fileName = pdfName;
        String extensao = "pdf";

        String adress = email;
        String subject = "UNIMED (BOLETO ABERTO)";
        String message = "";

        String Json = "{\n" +
                "\"attachment\": \"" + attachment + "\",\n" +
                "\"fileName\": \"" + fileName + "\",\n" +
                "\"extension\": \"" + extensao + "\",\n" +
                "\"address\": \""+ adress + "\",\n" +
                "\"subject\": \"" + subject + "\",\n" +
                "\"message\": \"" + message + "\"\n" +
                "}";
        post.postDados("http://187.60.56.85:5414/envioEmail", Json);
        return ResponseEntity.ok(pdfName);
    }
    @GetMapping("/EnvioLembreteEmail")
    public ResponseEntity<?> EnvioImpostoEmail(@RequestParam("e-mail") String email, @RequestParam(value = "cod") String cod, HttpServletResponse response) throws DataNotFoundException, IOException, JSONException {
        EnvioPDF post = new EnvioPDF();
        JsonReader json = new JsonReader();

        String attachment = "";
        String fileName = "";
        String extensao = "pdf";

        String adress = email;
        String subject = "UNIMED (LEMBRETE DE SOLICITAÇÃO)";

        //Beneficiario Solicitação
        JSONObject beneficiarioSolicitacao = json.readJsonSoli("http://187.17.144.244:8443/getBeneficiarioSolicitacao?cod="+cod);
        String beneficiario = beneficiarioSolicitacao.get("beneficiario").toString();
        String nomeCompleto = beneficiarioSolicitacao.get("nomeCompleto").toString();
        String solicitante = beneficiarioSolicitacao.get("solicitante").toString();
        String solicitacao = beneficiarioSolicitacao.get("solicitacao").toString();
        String validade = beneficiarioSolicitacao.get("validade").toString();

        //Complemento Solicitação
        String complementoSolicitacao = json.readJsonSolicitacao("http://187.17.144.244:8443/getComplementoSolicitacao?cod="+cod);

        //Observação Solicitação
        JSONObject observaçãoSolicitacao = json.readJsonSoli("http://187.17.144.244:8443/getObservacaoSolicitacao?cod="+cod);
        String observacao = observaçãoSolicitacao.get("observacao").toString();
        String dataDeNasc = observaçãoSolicitacao.get("dataDeNasc").toString();
        String indicacaoClinica = observaçãoSolicitacao.get("indicacaoClinica").toString();


        String messageBeneficiario = "Beneficiario: "+beneficiario +"\nnomeCompleto: "+nomeCompleto+"\nSolicitante: "+solicitante+
                "\nSolicitação: "+solicitacao+"\nValidade: "+validade;


        String observaçãoComplemento = "Obsertação: "+observacao+"\nData de Nascimento: "+dataDeNasc+"\nIndicação Clinica: "+indicacaoClinica;

        String mensagemAspasD = messageBeneficiario+"\n\n"+"\nComplementos: "+"\n"+complementoSolicitacao+"\n\n"+observaçãoComplemento;
        String mensagemAspasS = mensagemAspasD.replaceAll("\""," ");
        String Json = "{\n" +
                "\"attachment\": \""+attachment+"\",\n" +
                "\"fileName\": \""+fileName+"\",\n" +
                "\"extension\": \""+extensao+"\",\n" +
                "\"address\": \""+adress+"\",\n" +
                "\"subject\": \""+subject+"\",\n" +
                "\"message\": \""+mensagemAspasS+"\"\n" +
                "}";

        post.postDados("http://187.60.56.85:5414/lembrete", adress+"|"+mensagemAspasS);
        return ResponseEntity.ok(mensagemAspasS);
    }
}

package uctech.Unimed.controller;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonReader {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String readJsonSolicitacao(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return jsonText;
        } finally {
            is.close();
        }
    }

    public static JSONObject readJson(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            int endIndex = jsonText.length();
            JSONObject json = new JSONObject(jsonText.substring(1, endIndex - 1));
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject readJsonSoli(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    //Plano
//    public static void main(String[] args) throws IOException, JSONException {
//
//        JSONObject json = readJsonFromUrl("http://187.17.144.244:8443/getBeneficiario?cpfOrCard=01787561010133000");
//
//        String plano = (String) json.get("planoId");
//        switch (plano){
//            case "703890999":
//                System.out.println("Essencial");
//            case "7038909991":
//                System.out.println("M");
//        }
//    }
    //Lembrete
//    public static void main(String[] args) throws JSONException, IOException {
//        JSONArray json = readJsonFromUrl("http://187.17.144.244:8443/getComplementoSolicitacao?cod=6171728");
//        System.out.println(json);
//    }
    public static void main(String[] args) throws JSONException, IOException {
        JSONObject observaçãoSolicitacao = readJsonSoli("http://187.17.144.244:8443/getObservacaoSolicitacao?cod=6171728");
        String observacao = observaçãoSolicitacao.get("observacao").toString();
        System.out.println(observacao);
    }

}



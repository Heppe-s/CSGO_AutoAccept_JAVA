package com.Applications;

import java.net.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainLolAccept {

    static String ClientLol = "LeagueClientUx.exe";
    static String CheckStatus = "/lol-gameflow/v1/gameflow-phase";
    static String AcceptMatch = "/lol-matchmaking/v1/ready-check/accept";
    static String ProcessId;
    static String Token;
    static String Port;

    public static void main(String[] args) throws Exception {
        GetTokenAndPort();
        SSLSocketFactory SocketFactory = disableCertificateValidation();
        WebSocketClient lol = new WebSocketClient(new URI( "wss://127.0.0.1:" + Port)) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                String subscriber = CheckStatus.replaceAll("/", "_");
                this.send("[5, \"OnJsonApiEvent" + subscriber  + "\"]");
            }

            @Override
            public void onMessage(String s) {
                JSONObject Data = (JSONObject) new JSONArray(s).get(2);
                String Status = Data.getString("data");

                if (Status.equals("ReadyCheck")) {
                    try {
                        URL request = new URI("https://127.0.0.1:" + Port + AcceptMatch).toURL();
                        HttpsURLConnection con = (HttpsURLConnection) request.openConnection();

                        con.setSSLSocketFactory(SocketFactory);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Authorization", Token);
                        con.connect();
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {}

            @Override
            public void onError(Exception e) {}
        };

        lol.setSocketFactory(SocketFactory);
        lol.addHeader("Authorization", Token);
        lol.connect();
    }
    private static void GetTokenAndPort() throws IOException {
        GetLolProcessId();
        String command = "wmic process where 'ProcessId=" + ProcessId + "' get commandline";

        try (BufferedReader reader = ExecuteCommand(command)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\"")) {
                    Port = extractPattern(line, "--app-port=([^\"]+)");

                    String RawToken = extractPattern(line, "--remoting-auth-token=([^\"]+)");
                    String PrefixedToken = "riot:" + RawToken;
                    String EncodedToken = Base64.getEncoder().encodeToString(PrefixedToken.getBytes());
                    Token = "Basic " + EncodedToken;
                    return;
                }
            }
        }
        throw new IOException("Token and Port as not found");
    }

    private static void GetLolProcessId() throws IOException {
        try (BufferedReader in = ExecuteCommand("tasklist /FI \"IMAGENAME eq "+ ClientLol +"\" /FO CSV")) {
            String lines = in.lines().collect(Collectors.joining(","));
            String[] words = lines.replaceAll("\"", "").split(",");

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (word.equals(ClientLol)) {
                    ProcessId = words[i + 1];
                    return;
                }
            }
            throw new IOException("The LOL Client Process Id is not found");
        }
    }

    private static BufferedReader ExecuteCommand(String command) throws IOException {
        ProcessBuilder process = new ProcessBuilder("cmd.exe", "/c", command);
        Process p = process.start();

        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private static String extractPattern(String input, String regex) throws IOException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) return matcher.group(1);

        throw new IOException("Pattern not found");
    }
    // https://stackoverflow.com/questions/69361605/disable-ssl-certification-in-websocket-java
    private static SSLSocketFactory disableCertificateValidation() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        return sc.getSocketFactory();
    }
}
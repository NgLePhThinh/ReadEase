//package com.ReadEase.ReadEase.Config;
//
//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.drive.Drive;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.File;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.ArrayList;
//import java.util.Collection;
//
//@RequiredArgsConstructor
//public class GoogleAccess {
//
//    private GoogleCredential googleCredential;
//
//
//    public Drive getService() throws GeneralSecurityException, IOException {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        return new Drive.Builder(HTTP_TRANSPORT,
//                JacksonFactory.getDefaultInstance(), googleCredential)
//                .build();
//    }
//
//    public GoogleCredential googleCredential() throws GeneralSecurityException, IOException {
//        Collection<String> elenco = new ArrayList<String>();
//        elenco.add("https://www.googleapis.com/auth/drive");
//        HttpTransport httpTransport = new NetHttpTransport();
//        JacksonFactory jsonFactory = new JacksonFactory();
//        GoogleCredential gg = new GoogleCredential.Builder()
//                .setTransport(httpTransport)
//                .setJsonFactory(jsonFactory)
//                .setServiceAccountId("readease")
//                .setServiceAccountScopes(elenco)
//                .setServiceAccountPrivateKeyFromP12File(new File("src/main/resources/readease-399502-b15ebcf7e585.p12"))
//                .build();
//        return gg;
//    }
//
//}

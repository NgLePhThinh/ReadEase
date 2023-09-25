package com.ReadEase.ReadEase.Controller;

import com.ReadEase.ReadEase.Config.DriveQuickstart;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveTest {

    @GetMapping("")
    public ResponseEntity<?> getToken() throws GeneralSecurityException,  IOException {
        DriveQuickstart drive = new DriveQuickstart();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = drive.getCredentials(HTTP_TRANSPORT);

//        Drive Driver = drive.getInstance();

//        credential.setExpirationTimeMilliseconds(1L);
//        revokeToken(credential.getAccessToken());
        TokenResponse response =  refreshAccessToken(credential);
        return new ResponseEntity<>(response.getAccessToken(), HttpStatus.OK);
    }
    public  TokenResponse refreshAccessToken(Credential credential) throws IOException{
        try {
            TokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    credential.getRefreshToken(), "726593784919-cqfv8c53np0li2n3d3eb3soens15ui0u.apps.googleusercontent.com",
                    "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").execute();

            return response;
        }catch (TokenResponseException e) {
            if (e.getDetails() != null) {
                System.err.println("Error: " + e.getDetails().getError());
                if (e.getDetails().getErrorDescription() != null) {
                    System.err.println(e.getDetails().getErrorDescription());
                }
                if (e.getDetails().getErrorUri() != null) {
                    System.err.println(e.getDetails().getErrorUri());
                }
            } else {
                System.err.println(e.getMessage());
            }
        }
        return null;

    }
    public String Test (){

        return "";
    }
}

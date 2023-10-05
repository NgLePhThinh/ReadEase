package com.ReadEase.ReadEase.Service;

import com.ReadEase.ReadEase.Config.DriveConfig;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;


@Component
@Service
@RequiredArgsConstructor
public class DriveService {
    @Value("${application.drive-api.client-id}")
    private String  clientID;
    @Value("${application.drive-api.client-secret}")
    private String clientSecret;
    public TokenResponse getToken() throws GeneralSecurityException,  IOException {
        DriveConfig drive = new DriveConfig();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = drive.getCredentials(HTTP_TRANSPORT);

        TokenResponse accessToken = new TokenResponse();
        accessToken.setAccessToken(credential.getAccessToken());
        accessToken.setExpiresInSeconds(credential.getExpiresInSeconds());

        if(credential.getExpiresInSeconds() < 0L)
            accessToken = refreshAccessToken(credential);

        return accessToken;
    }
    private   TokenResponse refreshAccessToken(Credential credential) throws IOException{
        try {

            TokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    credential.getRefreshToken(), clientID,
                    clientSecret).execute();

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
}

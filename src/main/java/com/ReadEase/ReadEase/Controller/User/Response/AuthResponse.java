package com.ReadEase.ReadEase.Controller.User.Response;

import com.ReadEase.ReadEase.Model.Collection;
import com.ReadEase.ReadEase.Model.Document;
import lombok.*;

import java.util.HashMap;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String userID;
    private String email;
    private String avatar;
    private String token;
    private Document currentDocumentReading;
    private Set<Document> documents;
    private  Set <HashMap<String,Object>> collections;
    private int totalDocument;
    private String targetLanguage;
    private String idDriveFolder;
}

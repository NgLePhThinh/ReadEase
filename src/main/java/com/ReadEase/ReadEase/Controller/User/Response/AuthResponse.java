package com.ReadEase.ReadEase.Controller.User.Response;

import com.ReadEase.ReadEase.Model.Collection;
import com.ReadEase.ReadEase.Model.Document;
import lombok.*;

import javax.print.Doc;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String userID;
    private String email;
    private String avatar;
    private String token;
    private Document currentDocumentReading;
    private Set<Document> documents;
    private Set<Collection> collections;
}

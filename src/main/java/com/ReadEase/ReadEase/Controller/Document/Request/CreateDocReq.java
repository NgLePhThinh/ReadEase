package com.ReadEase.ReadEase.Controller.Document.Request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateDocReq {
    private String userID;
    private String name;
    private String url;
    private int totalPages;
    private float size;

}

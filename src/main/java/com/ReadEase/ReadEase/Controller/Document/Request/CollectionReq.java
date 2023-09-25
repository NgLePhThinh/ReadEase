package com.ReadEase.ReadEase.Controller.Document.Request;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CollectionReq {
    private String UserID;
    private String collectionName;
}

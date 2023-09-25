package com.ReadEase.ReadEase.Controller.Document.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HighLightReq {
    private int docID;
    private String colorHexCode;
    private String position;

}

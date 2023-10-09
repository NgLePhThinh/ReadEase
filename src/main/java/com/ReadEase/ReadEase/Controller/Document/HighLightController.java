package com.ReadEase.ReadEase.Controller.Document;


import com.ReadEase.ReadEase.Controller.Document.Request.HighLightReq;
import com.ReadEase.ReadEase.Model.Color;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.HighLight;
import com.ReadEase.ReadEase.Repo.ColorRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.HighLightRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/file/highlight")
@RequiredArgsConstructor
public class HighLightController {
    private final DocumentRepo docRepo;;
    private final ColorRepo colorRepo;
    private final HighLightRepo highLightRepo;
    @PostMapping("/add")
    public ResponseEntity<?> createHighLight(@RequestBody HighLightReq req){

        Color color =  colorRepo.findColorByColorHexCode(req.getColorHexCode()).orElse(null);
        if(color == null) return new ResponseEntity<>("Color not found",HttpStatus.NOT_FOUND);

        Document doc = docRepo.findById(req.getDocID()).orElse(null);
        if(doc == null) return new ResponseEntity<>("Document not found",HttpStatus.NOT_FOUND);

        HighLight res = HighLight.builder()
                .color(color)
                .position(req.getPosition())
                .build();

        doc.getHighLights().add(res);
        highLightRepo.save(res);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity <?> updateHighLight(@PathVariable("id") int highLightID, @RequestBody HighLightReq req){

        HighLight _highLight = highLightRepo.findById(highLightID).orElse(null);
        if(_highLight == null) return new ResponseEntity<>("Highlight not found",HttpStatus.NOT_FOUND);

        Color color =  colorRepo.findColorByColorHexCode(req.getColorHexCode()).orElse(null);
        if(color == null) return new ResponseEntity<>("Color not found",HttpStatus.NOT_FOUND);

        _highLight.setColor(color);
        _highLight.setPosition(req.getPosition());
        return new ResponseEntity<> (highLightRepo.save(_highLight), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <?> deleteHighLight(@PathVariable("id") int highLightID){
        highLightRepo.deleteById(highLightID);
        return new ResponseEntity<> ("Delete note successfully",HttpStatus.NO_CONTENT);
    }


}

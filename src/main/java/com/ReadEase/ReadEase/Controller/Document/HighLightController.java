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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user/file/highlight")
@RequiredArgsConstructor
public class HighLightController {
    private final DocumentRepo docRepo;;
    private final ColorRepo colorRepo;
    private final HighLightRepo highLightRepo;
    @PostMapping("")
    public ResponseEntity<?> createHighLight(@RequestBody HighLightReq req){
        Color color =  colorRepo.findColorByColorHexCode(req.getColorHexCode())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not find color by hex code")
                );

        HighLight res = HighLight.builder()
                .color(color)
                .position(req.getPosition())
                .build();

        Document doc = docRepo.findById(req.getDocID()).orElseThrow();
        doc.getHighLights().add(res);
        highLightRepo.save(res);

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity <?> updateHighLight(@PathVariable("id") int highLightID, @RequestBody HighLightReq req){
        HighLight _highLight = highLightRepo.findById(highLightID).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not found note by id: " + highLightID)
        );
        Color color =  colorRepo.findColorByColorHexCode(req.getColorHexCode())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can not find color by hex code")
                );
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

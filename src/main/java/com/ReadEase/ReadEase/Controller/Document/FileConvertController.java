package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.DriveService;
import com.ReadEase.ReadEase.Service.TokenService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.bind.annotation.RequestBody;

import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/api/user/file")
@RequiredArgsConstructor
public class FileConvertController {
    private final DriveService driveService;
    private final TokenService tokenService;
    private final UserRepo userRepo;
    private final DocumentRepo docRepo;


    @PostMapping("/convert-to-pdf")
    public ResponseEntity<?> convertToPDF(
            @Nonnull HttpServletRequest servletRequest,
            @org.springframework.web.bind.annotation.RequestBody byte[] data,
            @RequestParam("type") String documentType,
            @RequestParam("name") String documentName

    ) throws GeneralSecurityException, IOException {
        String userID = tokenService.getUserID(servletRequest);

        String mediaType = getMediaType(documentType);
        String fileResource = "src/main/resources/" + userID + ".pdf";

        if (mediaType == null)
            return new ResponseEntity<>("{ .doc .docx .xls .xlsx .ppt .pptx } are accepted", HttpStatus.NOT_ACCEPTABLE);

        User user = userRepo.findById(userID).orElse(null);

        if(docRepo.countDocumentByName(userID,documentName) > 0){
            return new ResponseEntity<>("The document name must not be duplicated.", HttpStatus.BAD_REQUEST);
        }
        convertFile(data,fileResource,documentType,mediaType);

         Document document =  driveService.updateFile(userID, user.getIdDriveFolder(), documentName,fileResource);
        document.setSize(getDocumentSize(fileResource));
        document.setTotalPages(getTotalPage(fileResource));

        user.setTotalCapacity(user.getTotalCapacity() + document.getSize());
        user.getDocuments().add(document);
        docRepo.save(document);
        userRepo.save(user);

        deleteFile(fileResource);

        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    private String getMediaType(String documentType) {
        return switch (documentType) {
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> null;
        };
    }

    private void convertFile(byte [] data, String fileResource, String documentType, String mediaType) throws IOException {
        final RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "document",
                        "document." + documentType,
                        RequestBody.create(
                                data,
                                MediaType.parse(mediaType)
                        )
                )
                .addFormDataPart(
                        "instructions",
                        new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject()
                                                .put("file", "document")
                                        )
                                ).toString()
                )
                .build();

        final Request request = new Request.Builder()
                .url("https://api.pspdfkit.com/build")
                .method("POST", body)
                .addHeader("Authorization", "Bearer pdf_live_HUKvCjdkzOH9cJZzrRoNmGaBbuHpsbuS3naVQtPOKpj")
                .build();

        final OkHttpClient client = new OkHttpClient()
                .newBuilder()
//                .connectTimeout(1000000, TimeUnit.MILLISECONDS)
                .readTimeout(180000,TimeUnit.MILLISECONDS)
//                .writeTimeout(1000000, TimeUnit.MILLISECONDS)
//                .callTimeout(1000000,TimeUnit.MILLISECONDS)
                .build();

        final Response response = client.newCall(request).execute();



        if (response.isSuccessful()) {
            Files.copy(
                    response.body().byteStream(),
                    FileSystems.getDefault().getPath(fileResource),
                    StandardCopyOption.REPLACE_EXISTING
            );

        } else {
            // Handle the error
            throw new IOException(response.body().string());
        }
    }

    private int getTotalPage( String fileResource){
        int pageCount = 0;
        try {
            File pdfFile = new File(fileResource);

            PDDocument document = PDDocument.load(pdfFile);

            pageCount= document.getNumberOfPages();

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageCount;
    }

    private float getDocumentSize(String fileResource){
        Path filePath = Path.of(fileResource);
        byte[] fileBytes = "".getBytes();
        try {
            fileBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (float) (fileBytes.length*1.0 / (1024*1024));
    }
    private void deleteFile(String fileResource){
        File file = new File(fileResource);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully.");
            }
        }
    }

}



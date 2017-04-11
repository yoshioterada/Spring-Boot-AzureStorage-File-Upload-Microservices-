package com.yoshio3;

import com.yoshio3.services.BlobStorageEntity;
import com.yoshio3.services.StorageService;
import org.glassfish.jersey.media.multipart.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;


/**
 * REST Invocation Handler class
 *
 * This classs handle a REST request.
 *
 * @author Yoshio Terada
 */

@Component
@Path("/rest")
public class FileUpOperation {

    @Autowired
    StorageService storageService;

    /**
     * Upload a file to Azure Storage.
     *
     * For example, if you use curl, you can upload a file by using following.
     * curl -F foo.png=@/tmp/Pictures/foo.png http://localhost:8080/rest/upload
     *
     * @param multiPart Multi Part Data
     * @return success JSON: {"status":"success","reason":null}
     *         failed  JSON: {"status":"error", "reason": "error message"}
     */

    @POST
    @Path(value = "/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(FormDataMultiPart multiPart) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        List<BodyPart> bodyPartList = multiPart.getBodyParts();
        bodyPartList
                .stream()
                .filter(bodyPart -> bodyPart.getContentDisposition().getFileName() != null)
                .forEach(bodyPart -> {
                    BodyPartEntity bodyPartEntity = (BodyPartEntity)bodyPart.getEntity();
                    byte[]  data = getBytes(bodyPartEntity.getInputStream());
                    try {
                        storageService.uploadFile(data, bodyPart.getContentDisposition().getFileName());
                        jsonObjectBuilder.add("status", "success");
                        jsonObjectBuilder.add("reason", JsonValue.NULL);
                    }catch(Exception e){
                        jsonObjectBuilder.add("status", "error");
                        jsonObjectBuilder.add("reason", e.getMessage());
                    }
                });
        return jsonObjectBuilder.build().toString();
    }

    /**
     * List all files in Azure Storage.
     *
     * For example, if you use curl, you can upload a file by using following.
     * curl http://localhost:8080/rest/listFiles
     *
     * @return success JSON: [{"name":"test.png","size":226415,"uri":"https://yjmscreport.blob.core.windows.net/fileup/test.png"}]
     *         failed  JSON: {"status":"error", "reason": "error message"}
     */
    @GET
    @Path(value="/listFiles")
    public String getFileList() {
        // Get List in Azure Storage
        List<BlobStorageEntity> allFiles;
        try {
            allFiles = storageService.getAllFiles();
        }catch(Exception e){
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder()
                    .add("status", "error")
                    .add("reason", e.getMessage());
            return jsonObjectBuilder.build().toString();
        }
        //Create JSON data from the result
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        allFiles.stream()
                .forEach(blobStorageEntity -> {
                    JsonObject jsonObj = Json.createObjectBuilder()
                            .add("name", blobStorageEntity.getName())
                            .add("size", blobStorageEntity.getSize())
                            .add("uri", blobStorageEntity.getURI())
                            .build();
                    System.out.println(jsonObj.toString());
                    arrayBuilder.add(jsonObj);
                });
        return arrayBuilder.build().toString();
    }

    private byte[] getBytes(InputStream is) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStream os = new BufferedOutputStream(b);
        int c;
        try {
            while ((c = is.read()) != -1) {
                os.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b.toByteArray();
    }

}

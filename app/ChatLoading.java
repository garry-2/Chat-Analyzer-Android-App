import android.content.ContentResolver;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ChatLoading extends Thread{

    private final Uri uri;
    private final String apiUrl;
    private final String ChatFile;

    private final ContentResolver contentResolver;

    public ChatLoading(Uri uri, String apiUrl, String chatFile, ContentResolver contentResolver) {
        this.uri = uri;
        this.apiUrl = apiUrl;
        ChatFile = chatFile;
        this.contentResolver = contentResolver;
    }

    @Override
    public void run() {
        super.run();
        try {


            InputStream inputStream = contentResolver.openInputStream(uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            byte[] chatFileBytes = byteArrayOutputStream.toByteArray();
            //String chatFileBase64 = Base64.encodeToString(chatFileBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_file", chatFileName, RequestBody.create(mediaType, chatFileBytes))
                    .build();
            Request request = new Request.Builder()
                    .url(API_ENDPOINT)
                    .post(requestBody)
                    .build();
//                Response response = client.newCall(request).execute();
//                int statusCode = response.code();
//                if (statusCode == 200) {
//                    // Handle successful response from the API
//                } else {
//                    // Handle error response from the API
//                }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

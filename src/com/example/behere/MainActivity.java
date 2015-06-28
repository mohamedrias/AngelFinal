package com.example.behere;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.behere.R;

import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int CAMERA_REQUEST = 1; 
	private static final int GALLERY_REQUEST=2;
	private ImageView imageView;
	ImageButton a;
	TextView t;
	Bitmap bmp;
	String result;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button b1=(Button)findViewById(R.id.button1);
		Button b2=(Button)findViewById(R.id.button2);
		Button b3=(Button)findViewById(R.id.button3);
		imageView=(ImageView)findViewById(R.id.imageView1);
		t=(TextView)findViewById(R.id.textView1);
		
		
	}
	public void method1(View v)
	{
		imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                startActivityForResult(cameraIntent, CAMERA_REQUEST); 
            }
        });
		
	}
	public void method2(View v)
	{
		imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button2);
        
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);  
	}
	public void method3(View v)
	{
		
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        callmethod(byteArray);	
		
	}

	public void callmethod(byte[] byteArray)
	{ 
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);
               result=multipartRequest("https://api.idolondemand.com/1/api/sync/detectfaces/v1",
                        "apikey=e8ede92d-2a9a-4c8b-ae90-3e3bcd7676a7",
                        "",
                        "",bmp);
                t=(TextView)findViewById(R.id.textView1);
                t.setText(result);
              /* Intent i=new Intent(this, Second.class);
               i.putExtra("jsonobj", result);
               startActivityForResult(i, 10);*/
               
            
            
                try{
                JSONObject obj = new JSONObject(result);
                JSONArray jarr= obj.getJSONArray("face");
                for(int i=0;i<jarr.length();i++)
                {
                	JSONObject jtemp=(JSONObject)jarr.get(i);
                	int tem1=jtemp.getInt("left");
                	Toast.makeText(this, "left: "+tem1+" ", Toast.LENGTH_SHORT).show();
                	int tem2=jtemp.getInt("top:");
                	Toast.makeText(this, "top: "+tem2+" ", Toast.LENGTH_SHORT).show();
                	int tem3=jtemp.getInt("width");
                	Toast.makeText(this, "width: "+tem3+" ", Toast.LENGTH_SHORT).show();
                	int tem4=jtemp.getInt("height");
                	Toast.makeText(this, "height: "+tem4+" ", Toast.LENGTH_SHORT).show();
        	        }  	
                String totalRecs = obj.getString("total_records");
                t.setText(totalRecs);
                }catch(Exception e){e.printStackTrace();}
                t.setText(result);
            } catch (IOException e) {
                e.printStackTrace();
            }  
            
    }
	
    public String multipartRequest(String urlTo, String post, String filepath, String filefield, Bitmap imageBitmap) throws ParseException, IOException {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;
        String twoHyphens = "--";
        String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
        String lineEnd = "\r\n";
        String result = "";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        String[] q = filepath.split("/");
        int idx = q.length - 1;
        try {
            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "file" + "\"; filename=\"" + "test" +"\"" + lineEnd);
            outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);
            byte[] image= bitmap(imageBitmap);
            outputStream.write(image,0,image.length);
            outputStream.writeBytes(lineEnd);
            String[] posts = post.split("&amp;");
            int max = posts.length;
            for(int i=0; i<max;i++) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                String[] kv = posts[i].split("=");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(kv[1]);
                outputStream.writeBytes(lineEnd);
            }
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            inputStream = connection.getInputStream();
            result = this.convertStreamToString(inputStream);
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return result;
        } catch(Exception e) {
            Log.e("MultipartRequest", "Multipart Form Upload Error");
            e.printStackTrace();
            return "error";
        }
    }
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
// see http://androidsnippets.com/multipart-http-requests
    public byte[] bitmap(Bitmap imageBitmap){
        if(imageBitmap==null)
             imageBitmap=    BitmapFactory.decodeResource(getResources(), R.drawable.pic);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        return bitmapdata;
    }
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
            Bitmap photo = (Bitmap) data.getExtras().get("data"); 
            imageView.setImageBitmap(photo);
        }
        else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
        {
        	Uri selectedImage = data.getData();
            InputStream imageStream = null;
			try {
				imageStream = getContentResolver().openInputStream(selectedImage);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(yourSelectedImage);
        }
    
        
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

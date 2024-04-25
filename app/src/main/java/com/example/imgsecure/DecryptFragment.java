package com.example.imgsecure;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.InputStream;


public class DecryptFragment extends Fragment {
    private Activity activity;
    private ImageView decrypt_img_attached;
    private TextView decrypt_TXT_encryptedData;

    private ActivityResultLauncher<Intent> resultLauncher;

    public DecryptFragment(Activity activity) {
        // Required empty public constructor
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_decrypt, container, false);
        findViews(root);
        initVars();
        return root;
    }


    private void initVars() {
        registerResult();
        decrypt_img_attached.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                    resultLauncher.launch(intent);
                }else {
                    Toast.makeText(activity, "No permissions to access gallery!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void findViews(View root) {
        decrypt_img_attached = root.findViewById(R.id.decrypt_img_attached);
        decrypt_TXT_encryptedData = root.findViewById(R.id.decrypt_TXT_encryptedData);
    }

    public void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try{
                            Uri imageUri = result.getData().getData();
                            InputStream inputStream =activity.getContentResolver().openInputStream(imageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                            inputStream.close();
                            decrypt_img_attached.setImageBitmap(selectedImage);
                            try{
                                String text = ImageEncryption.decryptText(selectedImage);
                                decrypt_TXT_encryptedData.setText(text);
                            }catch (Exception err){
                                System.out.println(err.getMessage());
                                decrypt_TXT_encryptedData.setText("No data encrypted!");
                            }


                        }catch (Exception e){
                            Toast.makeText(activity, "No image selected!", Toast.LENGTH_LONG).show();
                            decrypt_TXT_encryptedData.setText("");
                            decrypt_img_attached.setImageResource(R.drawable.attach_image);
                        }
                    }
                }
        );
    }

    public void clean(){
        decrypt_img_attached.setImageResource(R.drawable.attach_image);
        decrypt_TXT_encryptedData.setText("");
    }
}
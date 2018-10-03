package com.example.ocrcamerareader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class OCRCameraReader extends AppCompatActivity {

    SurfaceView mCameraView;
    CameraSource mCameraSource;
    TextView mTextView;

    private static final int requestPermissionID = 101;

    AtomicBoolean onSearch;

    private OCR_DNI ocr_dni;
    private int explore_level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ocrcamera_reader);

        mCameraView = findViewById(R.id.surface_view);
        mTextView = findViewById(R.id.ocr_camera_reader_text);

        onSearch = new AtomicBoolean(false);

        explore_level = getIntent().getIntExtra("explore_level", -1);

        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (textRecognizer.isOperational()) {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1260, 1024) //1024x768
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(300.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    startCamera();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * */
                @Override
                public void receiveDetections(final Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {

                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            stringBuilder.append("\n");
                        }
                        String text = stringBuilder.toString();
                        text = text.replaceAll("\\s+", "");
                        try {

                            ocr_dni = readDNI(text);

                            if (ocr_dni != null)
                                switch (explore_level) {
                                    case 0:
                                        if (ocr_dni.documentNumber != null)
                                            finishActivity();
                                        break;
                                    case 1:
                                        if (ocr_dni.documentNumber != null &&
                                                ocr_dni.expirationDate != null)
                                            finishActivity();
                                        break;
                                    case 2:
                                        if (ocr_dni.documentNumber != null &&
                                                ocr_dni.expirationDate != null &&
                                                ocr_dni.birthDate != null)
                                            finishActivity();
                                        break;
                                    case 3:
                                        if (ocr_dni.documentNumber != null &&
                                                ocr_dni.expirationDate != null &&
                                                ocr_dni.birthDate != null &&
                                                ocr_dni.documentName != null)
                                            finishActivity();
                                        break;
                                    default:
                                        if (ocr_dni.documentNumber != null)
                                            finishActivity();
                                }

                        } catch (Exception e) {
                            text = null;
                        }
                    }
                }
            });

        }

    }

    private void finishActivity() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", ocr_dni.getBundle());
        setResult(RESULT_OK, returnIntent);

     //   finish();

    }

    private void startCamera() {
        try {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(OCRCameraReader.this,
                        new String[]{Manifest.permission.CAMERA},
                        requestPermissionID);
                return;
            }
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OCR_DNI readDNI(String text) {
        if (!text.contains("ID")) {
            return null;
        } else {
            int start_index = text.indexOf("ID");

            if (start_index != -1) {

                ocr_dni = new OCR_DNI();
                String documentData = null;

                //Read first line of the DNI
                boolean DNIe = false;
                try {
                    documentData = text.substring(start_index, start_index + 24);
                    DNIe = true;
                } catch (Exception e) {
                    try {
                        documentData = text.substring(start_index, start_index + 14);
                        DNIe = false;
                    } catch (Exception ex) {
                        return null;
                    }
                } finally {
                    String documentNumber = null;
                    if (DNIe) {
                        try {
                            documentNumber = documentData.substring(15, 24);
                        } catch (Exception e) {
                        }
                    } else {
                        try {
                            documentNumber = documentData.substring(5, 13);
                        } catch (Exception e) {
                            documentNumber = null;
                        }
                    }

                    if (documentNumber != null && checkDocument(documentNumber.toCharArray()))
                        ocr_dni.setDocumentNumber(documentNumber);
                }

                String nationality = null;
                try {
                    nationality = documentData.substring(2, 5);
                } catch (Exception e) {
                    nationality = null;
                } finally {
                    if (nationality != null)
                        ocr_dni.setNationality(nationality);
                }


                text = consumeDocumentLine(start_index, documentData.length(), text);
                String secondLine;
                try {
                    secondLine = text.trim().substring(0, 18);
                } catch (Exception e) {
                    secondLine = null;
                }

                if (secondLine != null) {
                    //Extracting the birthDate
                    try {
                        String year = secondLine.substring(0, 2);
                        String month = secondLine.substring(2, 4);
                        String day = secondLine.substring(4, 6);
                        String controlDigit1 = secondLine.substring(6, 7);

                        if (checkControlDigit(year + month + day, controlDigit1)) {
                            ocr_dni.setBirhtDate(year, month, day);
                        }
                    } catch (Exception e) {
                    }
                    //Extracting the gender and expiration date
                    try {
                        String gender = secondLine.substring(7, 8);
                        ocr_dni.setGender(gender);

                        String expYear = secondLine.substring(8, 10);
                        String expMonth = secondLine.substring(10, 12);
                        String expDay = secondLine.substring(12, 14);
                        String controlDigit2 = secondLine.substring(14, 15);

                        if (checkControlDigit(expYear + expMonth + expDay, controlDigit2)) {
                            ocr_dni.setExpirationDate(expYear, expMonth, expDay);
                        }
                    } catch (Exception e) {
                    }
                    //Extracting the name of the DNI
                    try {
                        text = consumeSecondLine(0, secondLine.length(), text);

                        ArrayList<String> name = getNameFromOCR(text);
                        if (!name.isEmpty())
                            ocr_dni.setName(name);
                    } catch (Exception e) {
                    }
                }
            }
        }

        return ocr_dni;
    }

    private ArrayList<String> getNameFromOCR(String text) {
        ArrayList<String> ret = new ArrayList<>();
        String word;
        if (text.contains(" ")) {
            while (text.contains(" ")) {
                if (text.indexOf(" ") == 0)
                    text = text.substring(1, text.length());
                else {
                    word = text.substring(0, text.indexOf(" "));
                    ret.add(word);
                    text = text.substring(word.length(), text.length());
                }
            }
            if (!text.isEmpty())
                ret.add(text);
        } else {
            word = text;
            ret.add(word);
        }
        return ret;
    }

    private String consumeSecondLine(int start_index, int end_index, String text) {
        text = text.substring(start_index, text.length() - 1);
        String firstLine = text.substring(0, end_index);
        text = text.substring(firstLine.length(), text.length());

        while (text.indexOf('<') != -1) {
            text = text.replace("<", " ");
        }

        while (!isNotNumber(text.charAt(0))) {
            text = text.substring(1, text.length());
        }

        return text;
    }

    private boolean checkControlDigit(String characters, String controlDigit1) {
        int[] matrix = {7, 3, 1};
        int sum = 0;
        int weight, x;
        for (int i = 0; i <= 5; i++) {
            weight = matrix[i % 3];
            x = Integer.parseInt(String.valueOf(characters.charAt(i))) * weight;
            sum = sum + x;
        }

        return Integer.parseInt(controlDigit1) == (Math.abs(sum) % 10);
    }

    private String consumeDocumentLine(int start_index, int end_index, String text) {
        text = text.substring(start_index, text.length() - 1);
        String firstLine = text.substring(0, end_index);
        text = text.substring(firstLine.length(), text.length());

        while (text.indexOf('<') == 0) {
            text = text.replace("<", " ");
        }

        while (isNotNumber(text.charAt(0))) {
            text = text.substring(1, text.length());
        }

        return text;
    }

    private boolean isNotNumber(char c) {
        try {
            String k = String.valueOf(c);
            int i = Integer.parseInt(k);

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private boolean checkDocument(char[] documentNumber) {
        if (documentNumber.length < 9)
            return false;

        String numbers = String.valueOf(documentNumber).substring(0, 8);
        Integer num = Integer.parseInt(numbers);

        String lettersTable;
        int controlDigit;

        lettersTable = "TRWAGMYFPDXBNJZSQVHLCKE";
        controlDigit = 23;

        char let1 = documentNumber[8];
        char let2 = lettersTable.charAt(num % controlDigit);

        if (let1 == let2)
            return true;

        lettersTable = "XYV";
        controlDigit = 3;

        let2 = lettersTable.charAt(num % controlDigit);

        if (let1 == let2)
            return true;

        return false;
    }
}
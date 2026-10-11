
package com.rhix.barcodeqr.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.rhix.barcodeqr.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiBarcodeFragment extends Fragment {

    private static final String API_URL =
            "http://pzf.22b.mytemp.website/barcodeqrapi/api/save-image.php";

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private EditText editValue;
    private ImageView imageBarcode;
    private TextView textResult;
    private Button buttonGenerate;
    private ProgressBar progressBarcode;

    public ApiBarcodeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_api_barcode,
                container,
                false
        );

        editValue = view.findViewById(R.id.edit_barcode_value);
        imageBarcode = view.findViewById(R.id.image_barcode);
        textResult = view.findViewById(R.id.text_barcode_result);
        buttonGenerate = view.findViewById(R.id.button_generate_barcode);
        progressBarcode = view.findViewById(R.id.progress_barcode);

        buttonGenerate.setOnClickListener(v -> generateAndUploadBarcode());

        return view;
    }

    private void generateAndUploadBarcode() {
        String value = editValue.getText().toString().trim();

        if (TextUtils.isEmpty(value)) {
            editValue.setError("Please enter a barcode value.");
            return;
        }

        Bitmap bitmap;

        try {
            bitmap = createBarcodeBitmap(value);
        } catch (WriterException e) {
            textResult.setText(
                    "Barcode generation failed: " + e.getMessage()
            );
            return;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);

        String base64Image = Base64.encodeToString(
                output.toByteArray(),
                Base64.NO_WRAP
        );

        String imageData = "data:image/png;base64," + base64Image;

        try {
            JSONObject json = new JSONObject();
            json.put("type", "barcode");
            json.put("value", value);
            json.put("image_data", imageData);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    JSON
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            setLoading(true);
            textResult.setText("Uploading barcode to server...");

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(
                        @NonNull Call call,
                        @NonNull IOException e) {

                    showMessage("Upload failed: " + e.getMessage());

                    Log.e("error upload", "Upload failed: " + e.getMessage());
                }

                @Override
                public void onResponse(
                        @NonNull Call call,
                        @NonNull Response response) {

                    try (Response res = response) {
                        String responseText = res.body() != null
                                ? res.body().string()
                                : "";

                        if (!res.isSuccessful()) {
                            showMessage(
                                    "Server returned HTTP " + res.code()
                                            + "\n" + responseText
                            );

                            Log.e("error upload", "Server returned HTTP " + res.code()
                                    + "\n" + responseText);
                            return;
                        }

                        JSONObject result = new JSONObject(responseText);

                        if (!result.optBoolean("success", false)) {
                            showMessage(
                                    result.optString(
                                            "message",
                                            "The server rejected the upload."
                                    )
                            );
                            return;
                        }

                        String imageUrl = result.optString(
                                "image_url",
                                ""
                        );

                        if (imageUrl.isEmpty()) {
                            showMessage(
                                    "Upload succeeded, but image_url "
                                            + "was not returned."
                            );
                            return;
                        }

                        downloadSavedImage(imageUrl, value);

                    } catch (Exception e) {
                        showMessage(
                                "Could not process server response: "
                                        + e.getMessage()
                        );
                    }
                }
            });

        } catch (Exception e) {
            setLoading(false);
            textResult.setText("Request error: " + e.getMessage());
        }
    }

    private Bitmap createBarcodeBitmap(String value)
            throws WriterException {

        int width = 1000;
        int height = 150;

        Map<EncodeHintType, Object> hints =
                new EnumMap<>(EncodeHintType.class);

        hints.put(EncodeHintType.MARGIN, 10);

        BitMatrix matrix = new MultiFormatWriter().encode(
                value,
                BarcodeFormat.CODE_128,
                width,
                height,
                hints
        );

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        );

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(
                        x,
                        y,
                        matrix.get(x, y)
                                ? android.graphics.Color.BLACK
                                : android.graphics.Color.WHITE
                );
            }
        }

        return bitmap;
    }

    private void downloadSavedImage(String imageUrl, String value) {
        Request request = new Request.Builder()
                .url(imageUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(
                    @NonNull Call call,
                    @NonNull IOException e) {

                showMessage(
                        "Barcode uploaded, but image retrieval failed: "
                                + e.getMessage()
                );
            }

            @Override
            public void onResponse(
                    @NonNull Call call,
                    @NonNull Response response) {

                try (Response res = response) {
                    if (!res.isSuccessful() || res.body() == null) {
                        showMessage(
                                "Barcode uploaded, but image retrieval "
                                        + "returned HTTP " + res.code()
                        );
                        return;
                    }

                    Bitmap savedBitmap = BitmapFactory.decodeStream(
                            res.body().byteStream()
                    );

                    if (savedBitmap == null) {
                        showMessage(
                                "The returned file could not be decoded "
                                        + "as an image."
                        );
                        return;
                    }

                    if (!isAdded()) {
                        return;
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        imageBarcode.setImageBitmap(savedBitmap);
                        setLoading(false);

                        textResult.setText(
                                "Barcode uploaded and retrieved successfully."
                                        + "\n\nValue: " + value
                                        + "\n\nImage URL:\n" + imageUrl
                        );
                    });

                } catch (Exception e) {
                    showMessage(
                            "Image retrieval error: " + e.getMessage()
                    );
                }
            }
        });
    }

    private void showMessage(String message) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }

            setLoading(false);
            textResult.setText(message);
        });
    }

    private void setLoading(boolean loading) {
        progressBarcode.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        buttonGenerate.setEnabled(!loading);
    }
}

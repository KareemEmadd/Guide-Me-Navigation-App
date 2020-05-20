
package com.example.graduationproject;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.graduationproject.customview.OverlayView;
import com.example.graduationproject.env.BorderedText;
import com.example.graduationproject.env.ImageUtils;
import com.example.graduationproject.tflite.Classifier;
import com.example.graduationproject.tflite.TFLiteObjectDetectionAPIModel;
import com.example.graduationproject.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;



/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
//  private static final Logger LOGGER = new Logger();
  private TextToSpeech tts;
  View childView;
  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/Label.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Classifier detector;
  Vibrator vibrator;
  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;
  private DisplayMetrics displayMetrics;
  int height;
  int width;
  int screenCenter;
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    height = displayMetrics.heightPixels;
    width = displayMetrics.widthPixels;
    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//    System.out.println;
//    LOGGER.i("height ="+height +" h "+width +" w ");

    screenCenter=width/2;
    Log.d("CoordinatesCenter", Integer.toString(screenCenter));
    Log.d("CoordinatesCenter", Integer.toString(width));
    Log.d("CoordinatesCenter", Integer.toString(height));

    initializeTextToSpeech();
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
              TFLiteObjectDetectionAPIModel.create(
                      getAssets(),
                      TF_OD_API_MODEL_FILE,
                      TF_OD_API_LABELS_FILE,
                      TF_OD_API_INPUT_SIZE,
                      TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
//      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
//    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

//    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    cropSize, cropSize,
                    sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new OverlayView.DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
//    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }
    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                Log.d("detect","Running detection on image ");
//                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();

                paint.setColor(Color.RED);
                paint.setStyle(Style.STROKE);
                paint.setStrokeWidth(2.0f);


                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                switch (MODE) {
                  case TF_OD_API:
                    minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    break;
                }

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();
                for (final Classifier.Recognition result : results) {
                  final RectF location = result.getLocation();
                  if (searcher.toLowerCase().equals(""))
                  {
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                      canvas.drawRect(location, paint);
                      if(location.width()>200){
                        vibrator.vibrate(100);
                      }
                      cropToFrameTransform.mapRect(location);
                      result.setLocation(location);
                      mappedRecognitions.add(result);
                    }
                  }
                  else
                  {
                    if(location != null && result.getConfidence() >= minimumConfidence && result.getTitle().equals(searcher))
                    {
                      if(location.width()>200){
                        vibrator.vibrate(100);
                      }
                      canvas.drawRect(location, paint);
                      double x = location.centerX();
                      cropToFrameTransform.mapRect(location);
                      result.setLocation(location);
                      mappedRecognitions.add(result);
                      getDistance(result.getLocation(),result.getTitle(),x);
                    }
                  }
                }

                tracker.trackResults(mappedRecognitions, currTimestamp);
                trackingOverlay.postInvalidate();

                computingDetection = false;

                runOnUiThread(
                        new Runnable() {
                          @Override
                          public void run() {
                            showFrameInfo(previewWidth + "x" + previewHeight);
                            showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                            showInference(lastProcessingTimeMs + "ms");
                          }
                        });
              }
            });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }





  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(new Runnable() {
      @Override
      public void run() {
        detector.setUseNNAPI(isChecked);
      }
    });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(new Runnable() {
      @Override
      public void run() {
        detector.setNumThreads(numThreads);
      }
    });
  }
  private void speak(String message) {
    if(Build.VERSION.SDK_INT >= 21){
      tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,null);
    } else {
      tts.speak(message, TextToSpeech.QUEUE_FLUSH,null);
    }
  }
  private void initializeTextToSpeech() {
    tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (tts.getEngines().size() == 0 ){
          Toast.makeText(getBaseContext(), getString(R.string.app_name),Toast.LENGTH_LONG).show();
          finish();
        } else {
          tts.setLanguage(Locale.US);
//                    speak("Hello there, I am ready to start our conversation");
        }
      }
    });
  }
}

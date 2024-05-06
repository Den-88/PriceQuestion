package com.den.shak.pq.fragments;

import static com.den.shak.pq.cloud.SetOrder.setOrder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.den.shak.pq.R;
import com.den.shak.pq.activity.MapActivity;
import com.den.shak.pq.adapters.PhotoAdapter;
import com.den.shak.pq.cloud.CloudPhoto;
import com.den.shak.pq.cloud.SetOrder;
import com.den.shak.pq.models.Order;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class NewAdvert extends Fragment implements SetOrder.SetOrderCallback {
    private List<String> categoryes;
    private TextInputLayout new_adverts_category_layout;
    private AutoCompleteTextView new_adverts_category;
    private TextInputLayout new_adverts_title_layout;
    private TextView new_adverts_title;
    private TextInputLayout new_adverts_description_layout;
    private TextView new_adverts_description;
    private TextView new_adverts_price;
    private TextView setLocationText;
    private Button button_set_coordinates;
    private Button button_take_photo;
    private Button button_choose_photo;
    public static Order order;
    private ImageCapture imageCapture;
    private Preview preview;
    private Dialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Инфлейт макета фрагмента
        View rootView =  inflater.inflate(R.layout.new_advert_fragment, container, false);

        // Инициализация элементов пользовательского интерфейса
        new_adverts_category_layout = rootView.findViewById(R.id.new_adverts_category_layout);
        new_adverts_category = rootView.findViewById(R.id.new_adverts_category);
        new_adverts_title_layout = rootView.findViewById(R.id.new_adverts_title_layout);
        new_adverts_title = rootView.findViewById(R.id.new_adverts_title);
        new_adverts_description_layout = rootView.findViewById(R.id.new_adverts_description_layout);
        new_adverts_description = rootView.findViewById(R.id.new_adverts_description);
        new_adverts_price = rootView.findViewById(R.id.new_adverts_price);
        button_set_coordinates = rootView.findViewById(R.id.new_adverts_button_set_coordinates);
        button_take_photo = rootView.findViewById(R.id.new_adverts_add_photo_button);
        button_choose_photo = rootView.findViewById(R.id.new_adverts_add_gallery_photo_button);
        setLocationText = rootView.findViewById(R.id.new_adverts_set_location_text);
        Button button_create_order = rootView.findViewById(R.id.new_adverts_button_create_order);

        // Инициализация предустановленных вариантов категорий
        categoryes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.category)));

        // Инициализация и настройка AutoCompleteTextView
        initializeAutoCompleteTextView(new_adverts_category);

        // Установка слушателей для заголовка и описания
        setTitleAndDescriptionListeners();

        // Настройка кнопок и других элементов
        button_set_coordinates.setOnClickListener(v -> setCoordinates());
        setupCamera();
        button_take_photo.setOnClickListener(v -> showCameraDialog());
        button_choose_photo.setOnClickListener(v -> dispatchChoosePictureIntent());
        button_create_order.setOnClickListener(v -> createOrder());

        // Создание нового заказа и установка его ID
        order = new Order();
        UUID uuid = UUID.randomUUID();
        order.setId(uuid.toString());
        return rootView;
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onResume() {
        super.onResume();
        // Получение текстового поля для отображения статуса установки координат
        TextView text_position_seted = requireActivity().findViewById(R.id.new_adverts_position_seted);
        // Проверка, установлены ли координаты
        if (order.getLocation() != null) {
            // Если координаты установлены, отображаем соответствующий текст и меняем текст кнопки
            text_position_seted.setVisibility(View.VISIBLE);
            button_set_coordinates.setText("Изменить местоположение");

            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
            if (isDarkTheme) {
                setLocationText.setTextColor(ContextCompat.getColorStateList(requireContext(), com.google.android.material.R.color.m3_dark_default_color_secondary_text));
            } else {
                setLocationText.setTextColor(ContextCompat.getColorStateList(requireContext(), com.google.android.material.R.color.m3_default_color_secondary_text));
            }

        } else {
            // Если координаты не установлены, скрываем текст и меняем текст кнопки
            text_position_seted.setVisibility(View.GONE);
            button_set_coordinates.setText("Указать местоположение");
        }
    }

    private void initializeAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
        // Создание адаптера с предустановленными вариантами
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_dropdown_item_1line, categoryes);

        // Установка адаптера для AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter);

        // Настройка фильтрации и проверки введенного текста
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Проверка введенного текста на соответствие предустановленным вариантам
                String newText = s.toString();
                if (!TextUtils.isEmpty(newText)) {
                    new_adverts_category_layout.setError(null);
                    if (!categoryes.toString().toLowerCase().contains(newText.toLowerCase())) {
                        // Если текст не соответствует предустановленным вариантам, очищаем поле
                        autoCompleteTextView.setText("");
                    }
                }
            }
        });

        // Настройка обработчика потери фокуса для AutoCompleteTextView
        autoCompleteTextView.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String enteredText = autoCompleteTextView.getText().toString().trim();
                if (!TextUtils.isEmpty(enteredText) && !categoryes.contains(enteredText)) {
                    // Если текст не соответствует предустановленным вариантам, очищаем поле
                    autoCompleteTextView.setText("");
                }
            }
        });
    }

    private void setTitleAndDescriptionListeners() {
        // Установка слушателя для заголовка
        new_adverts_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Проверка наличия текста в поле заголовка
                if (!s.toString().isEmpty()) {
                    new_adverts_title_layout.setError(null);
                }
            }
        });

        // Установка слушателя для описания
        new_adverts_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Проверка наличия текста в поле описания
                if (!s.toString().isEmpty()) {
                    new_adverts_description_layout.setError(null);
                }
            }
        });
    }

    // Метод для перехода к активности выбора местоположения на карте
    private void setCoordinates() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        // Запуск активности карты
        startActivity(intent);
    }

    // Регистрация Activity Result Launcher для запроса разрешений на использование камеры
    private final ActivityResultLauncher<String[]> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                // Проверка предоставленных разрешений
                if (permissions.containsValue(false)) {
                    // Вывод сообщения в лог о непредоставленных разрешениях
                    Log.d("permissions", permissions.toString());
                } else {
                    // Запуск метода для отображения диалогового окна с камерой
                    showCameraDialog();
                }
            });

    // Регистрация Activity Result Launcher для запроса разрешений на использование галереи
    private final ActivityResultLauncher<String[]> requestGalleryPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                // Проверка предоставленных разрешений
                if (permissions.containsValue(false)) {
                    // Вывод сообщения в лог о непредоставленных разрешениях
                    Log.d("permissions", permissions.toString());
                } else {
                    // Запуск метода для отображения диалогового окна
                    showCameraDialog();
                }
            });

    ActivityResultLauncher<Intent> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Проверка результата выбора изображения
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Получаем данные из результата
                    Intent data = result.getData();
                    if (data != null) {
                        // Получаем URI выбранного изображения
                        Uri selectedImageUri = data.getData();
                        // Создаем папку для сохранения изображений во внутреннем хранилище приложения
                        File directory = new File(requireActivity().getFilesDir() + "/images/" +  order.getId());
                        if (!directory.exists()) {
                            // Создаем папку, если она не существует
                            //noinspection ResultOfMethodCallIgnored
                            directory.mkdirs();
                        }

                        // Создаем уникальное имя файла изображения
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String fileName = "image_" + timeStamp + ".jpg";
                        File photoFile = new File(directory, fileName);

                        try {
                            // Открываем входной поток для выбранного изображения
                            assert selectedImageUri != null;
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(selectedImageUri);
                            OutputStream outputStream; // Используем Files.newOutputStream()
                            // Используем Files.newOutputStream() для сохранения файла изображения
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                outputStream = Files.newOutputStream(photoFile.toPath());
                            } else {
                                //noinspection IOStreamConstructor
                                outputStream = new FileOutputStream(photoFile);
                            }
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            // Читаем данные из входного потока и записываем их в файл
                            while (true) {
                                assert inputStream != null;
                                if ((bytesRead = inputStream.read(buffer)) == -1) break;
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            // Закрываем потоки
                            outputStream.close();
                            inputStream.close();
                            // Обновляем интерфейс после добавления изображения
                            setPhoto();
                        } catch (IOException e) {
                            Log.e("SaveImage", "Ошибка сохранения изображения", e);
                        }
                    }
                }
            }
    );
    private void dispatchChoosePictureIntent() {
        // Проверяем версию Android и запрос разрешений в зависимости от нее
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Для Android 12 (API 33) и выше используется новое разрешение
            String[] permission = {Manifest.permission.READ_MEDIA_IMAGES};
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                     != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                    // Если разрешение на чтение из галереи не предоставлено, запрашиваем его у пользователя
                requestGalleryPermissionLauncher.launch(permission);
            }
            else {
                // Разрешение на чтение из галереи уже предоставлено, можно запускать интент выбора изображения
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Запуск активности выбора изображения из галереи
                pickPhotoLauncher.launch(pickPhotoIntent);
            }
        } else {
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Если разрешение на чтение из галереи не предоставлено, запросите его у пользователя
                requestGalleryPermissionLauncher.launch(permission);
            } else {
                // Разрешение на чтение из галереи уже предоставлено, можно запускать интент выбора изображения
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Запуск активности выбора изображения из галереи
                pickPhotoLauncher.launch(pickPhotoIntent);
            }
        }
    }

    // Метод для настройки CameraX
    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Настройка параметров съемки
                ImageCapture.Builder builder = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY);

                imageCapture = builder.build();

                // Подключение просмотра камеры
                 preview = new Preview.Builder()
                         .build();
                 CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                // Запуск камеры с настроенными параметрами
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);


            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Ошибка при настройке камеры", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }
    private void showCameraDialog() {
        // Проверяем, предоставлены ли необходимые разрешения
        String[] permissions = {Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение на использование камеры не предоставлено, запросите его у пользователя
            requestCameraPermissionLauncher.launch(permissions);
        } else {
            // Создаем экземпляр билдера MaterialAlertDialogBuilder
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireActivity());

            // Устанавливаем заголовок диалогового окна
            dialogBuilder.setTitle("Сделайте фото");

            // Инфлейтим макет диалогового окна
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_camera_preview, null);

            // Находим PreviewView в макете диалогового окна
            PreviewView previewView = dialogView.findViewById(R.id.preview_view);

            // Устанавливаем PreviewView для отображения предварительного просмотра камеры
            // Добавляем представление в диалоговое окно
            dialogBuilder.setView(dialogView);

            // Создаем и отображаем диалоговое окно
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            // Создаем и возвращаем экземпляр AlertDialog
            preview.setSurfaceProvider(previewView.getSurfaceProvider());
            Button button_take_photo = dialogView.findViewById(R.id.new_adverts_button_take_photo);
            button_take_photo.setOnClickListener(v -> {
                takePicture();
                dialog.dismiss();
            });

        }
    }

    // Метод для захвата изображения с камеры
    private void takePicture() {
        if (imageCapture != null) {
            // Создаем папку для сохранения изображений во внутреннем хранилище приложения
            File directory = new File(requireActivity().getFilesDir() + "/images/" +  order.getId());
            if (!directory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                directory.mkdirs(); // Создаем папку, если она не существует
            }

            // Генерация уникального имени файла изображения на основе временной метки
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "image_" + timeStamp + ".jpg";
            File photoFile = new File(directory, fileName);

            // Создание настроек для сохранения изображения
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            // Захват изображения с камеры и сохранение его в файл
            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    // Обработка сохраненного изображения
                    Bitmap savedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    // Получение пути к сохраненному файлу из результата сохранения изображения
                    String savedFilePath = new File(Objects.requireNonNull(outputFileResults.getSavedUri()).getPath()).getAbsolutePath();

                    // Поворот изображения на 90 градусов (для корректного отображения)
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(savedBitmap, 0, 0, savedBitmap.getWidth(), savedBitmap.getHeight(), matrix, true);
                    try {
                        File file = new File(savedFilePath);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        // Сжатие и сохранение повернутого изображения в файл
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        outputStream.close();
                        // Обновление интерфейса после добавления изображения
                        setPhoto();
                    } catch (IOException e) {
                        Log.e("SaveImage", "Ошибка сохранения изображения", e);
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    // Обработка ошибки при сохранении изображения
                    Log.e("SaveImage", "Ошибка при сохранении изображения", exception);
                }
            });
        }
    }

    // Метод для обновления интерфейса после добавления фотографии
    public void setPhoto() {
        File directory = new File(requireActivity().getFilesDir() + "/images/" +  order.getId());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            ArrayList<String> photoPaths = new ArrayList<>();
            assert files != null;
            for (File file : files) {
                photoPaths.add(file.getAbsolutePath());
            }

            // Получение RecyclerView для отображения добавленных изображений
            RecyclerView recyclerView = requireActivity().findViewById(R.id.new_adverts_photo_viewer);

            // Создание и установка LayoutManager для RecyclerView
            GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 5);
            recyclerView.setLayoutManager(layoutManager);

            // Создание адаптера для отображения фотографий
            PhotoAdapter adapter = new PhotoAdapter(photoPaths, requireContext(), position -> {
                button_take_photo.setVisibility(View.VISIBLE);
                button_choose_photo.setVisibility(View.VISIBLE);
            });

            recyclerView.setAdapter(adapter);

            // Отображение кнопок для добавления новых фотографий в зависимости от количества добавленных фотографий
            if (files.length < 5) {
                button_take_photo.setVisibility(View.VISIBLE);
                button_choose_photo.setVisibility(View.VISIBLE);
            }
            else {
                button_take_photo.setVisibility(View.GONE);
                button_choose_photo.setVisibility(View.GONE);
            }
        }
    }

    // Метод для создания новой заявки
    @SuppressLint("PrivateResource")
    private void createOrder() {
        // Получаем значения полей для создания заявки
        String category = new_adverts_category.getText().toString().trim();
        String title = new_adverts_title.getText().toString().trim();
        String description = new_adverts_description.getText().toString().trim();

        // Проверяем, что все обязательные поля заполнены
        boolean check_passed = true;
        if (category.isEmpty()) {
            new_adverts_category_layout.setError(getString(R.string.new_adert_not_be_empty));
            check_passed = false;
        }
        if (title.isEmpty()) {
            new_adverts_title_layout.setError(getString(R.string.new_adert_not_be_empty));
            check_passed = false;
        }
        if (description.isEmpty()) {
            new_adverts_description_layout.setError(getString(R.string.new_adert_not_be_empty));
            check_passed = false;
        }
        if (order.getLocation() == null) {
            check_passed = false;
            setLocationText.setTextColor(ContextCompat.getColorStateList(requireContext(), com.google.android.material.R.color.design_error));
        }

        // Если все поля заполнены, создаем заявку
        if (check_passed) {
            String[] categories = getResources().getStringArray(R.array.category);
            order.setCategory(Arrays.asList(categories).indexOf(category));
            order.setTitle(title);
            order.setDescription(description);
            if (!new_adverts_price.getText().toString().isEmpty()) {
                order.setPrice(Integer.parseInt(new_adverts_price.getText().toString().trim()));
            } else {
                order.setPrice(null);
            }

            // Отображаем диалоговое окно для подтверждения создания заявки
            MaterialAlertDialogBuilder confirmationDialogBuilder = new MaterialAlertDialogBuilder(requireContext());
            confirmationDialogBuilder.setTitle(R.string.new_adert_set_order);
            confirmationDialogBuilder.setMessage(R.string.new_adert_set_order_description);
            confirmationDialogBuilder.setPositiveButton(R.string.new_adert_yes, (dialogInterface, i) -> {
                try {
                    Button button_create_order = requireActivity().findViewById(R.id.new_adverts_button_create_order);
                    button_create_order.setEnabled(false);
                    // Вызываем метод для загрузки заявки на сервер
                    DoUpload();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            confirmationDialogBuilder.setNegativeButton("Отмена", (dialogInterface, i) -> {
                // Отменить действие
            });

            // Показать диалог подтверждения
            confirmationDialogBuilder.show();
        }
    }

    // Метод для загрузки данных на сервер
    private void DoUpload() throws IOException {

        // Создаем и отображаем диалоговое окно с индикатором прогресса
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        progressDialog = dialogBuilder.create();
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Создаем и выполняем задачу загрузки данных на сервер
        CloudPhoto.UploadTask uploadTask = new CloudPhoto.UploadTask(requireContext(), order.getId());
        uploadTask.setProgressListener(progress -> {
            ProgressBar progressBar = progressDialog.findViewById(R.id.dialog_progress_bar);
            int percentage = progress.getUploadedFiles() * 100 / progress.getTotalFiles();
            progressBar.setProgress(percentage); // Обновление ProgressBar
        });
        // Установка слушателя завершения задачи
        uploadTask.setOnUploadTaskCompletedListener(() -> {
            // Вызываем setOrder() после завершения задачи загрузки
            setOrder(order, this, requireContext());
        });
        // Запускаем задачу загрузки
        uploadTask.execute();
    }

    // Метод для обработки результатов загрузки заявки на сервер
    @Override
    public void onSetOrderResult() {
        requireActivity().runOnUiThread(() -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_to_new_advert);
            progressDialog.dismiss();
            showSuccessDialog();
        });
    }


    // Метод для отображения диалогового окна с сообщением об успешной загрузке
    private void showSuccessDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.new_adert_create_done);
        builder.setMessage(R.string.new_adert_create_done_desscriprion);
        builder.setPositiveButton(R.string.new_adert_ok, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }
}

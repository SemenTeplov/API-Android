package com.main.api;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends AndroidViewModel {
    private static final  String BASE_URL = "https://dog.ceo/api/breeds/image/random";

    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STATUS = "status";
    private static final String TAG = "MainActivity";

    private MutableLiveData<Dogimage> dogimage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isError = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MainViewModel(@NonNull Application application) {
        super(application);
    }
    public LiveData<Dogimage> getDogimage() {
        return dogimage;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<Boolean> getIsError() {
        return isError;
    }
    @SuppressLint("CheckResult")
    public void loadDogimage() {
        Disposable disposable = loadDogimageRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Throwable {
                        isError.setValue(false);
                        isLoading.setValue(true);
                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Throwable {
                        isLoading.setValue(false);
                    }
                })
                .subscribe(new Consumer<Dogimage>() {
                    @Override
                    public void accept(Dogimage image) throws Throwable {
                        dogimage.setValue(image);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        isError.setValue(true);
                        Log.d(TAG, "Error: " + throwable.getMessage());
                    }
                });
        compositeDisposable.add(disposable);
    }
    private Single<Dogimage> loadDogimageRx() {
        return Single.fromCallable(new Callable<Dogimage>() {
            @Override
            public Dogimage call() throws Exception {
                URL url = new URL(BASE_URL);
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder data = new StringBuilder();
                String result;

                do {
                    result = bufferedReader.readLine();

                    if (result != null) {
                        data.append(result);
                    }
                }while (result != null);

                JSONObject jsonObject = new JSONObject(data.toString());

                String message = jsonObject.getString(KEY_MESSAGE);
                String status = jsonObject.getString(KEY_STATUS);

                return new Dogimage(message, status);
            }
        });
    }
    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
    }
}

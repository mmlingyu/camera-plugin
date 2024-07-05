package com.inspace.plugin;

import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.List;

public class ResultBundle {
     List<ObjectDetectorResult> results;
    Long inferenceTime;

    int inputImageHeight;
    int inputImageWidth;
    int inputImageRotation;

    public ResultBundle(List<ObjectDetectorResult> results, Long inferenceTime, int inputImageHeight, int inputImageWidth, int inputImageRotation) {
        this.results = results;
        this.inferenceTime = inferenceTime;
        this.inputImageHeight = inputImageHeight;
        this.inputImageWidth = inputImageWidth;
        this.inputImageRotation = inputImageRotation;
    }

    public List<ObjectDetectorResult> getResults() {
        return results;
    }

    public void setResults(List<ObjectDetectorResult> results) {
        this.results = results;
    }

    public Long getInferenceTime() {
        return inferenceTime;
    }

    public void setInferenceTime(Long inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    public int getInputImageHeight() {
        return inputImageHeight;
    }

    public void setInputImageHeight(int inputImageHeight) {
        this.inputImageHeight = inputImageHeight;
    }

    public int getInputImageWidth() {
        return inputImageWidth;
    }

    public void setInputImageWidth(int inputImageWidth) {
        this.inputImageWidth = inputImageWidth;
    }

    public int getInputImageRotation() {
        return inputImageRotation;
    }

    public void setInputImageRotation(int inputImageRotation) {
        this.inputImageRotation = inputImageRotation;
    }
}

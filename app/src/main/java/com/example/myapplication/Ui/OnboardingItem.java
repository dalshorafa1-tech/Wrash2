package com.example.myapplication.Ui;

public class OnboardingItem {
    private String title;
    private String description;
    private int image;
    private boolean showSpinner;

    public OnboardingItem(String title, String description, int image, boolean showSpinner) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.showSpinner = showSpinner;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImage() {
        return image;
    }

    public boolean isShowSpinner() {
        return showSpinner;
    }
}
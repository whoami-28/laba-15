package com.library.model;

import com.library.exception.ValidationException;

public class ElectronicBook extends LibraryMaterial {
    private String format;
    private double fileSizeMb;
    private String downloadUrl;

    public static final int DEFAULT_DIGITAL_LOAN_DAYS = 30;

    public ElectronicBook(String id, String title, String author, int publicationYear, int totalCopies,
                          String format, double fileSizeMb, String downloadUrl) throws ValidationException {
        this(id, title, author, publicationYear, totalCopies, totalCopies, format, fileSizeMb, downloadUrl);
    }

    public ElectronicBook(String id, String title, String author, int publicationYear, int totalCopies,
                          int availableCopies, String format, double fileSizeMb, String downloadUrl)
            throws ValidationException {
        super(id, title, author, publicationYear, totalCopies, availableCopies);
        setFormat(format);
        setFileSizeMb(fileSizeMb);
        setDownloadUrl(downloadUrl);
    }

    @Override
    public String getMaterialType() {
        return "Электронное издание";
    }

    @Override
    public int getDefaultLoanDays() {
        return DEFAULT_DIGITAL_LOAN_DAYS;
    }

    @Override
    public double calculateFine(int overdueDays) {
        return 0.0;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Формат: %s | Размер: %.2f МБ | URL: %s | Штраф: не взимается",
                format, fileSizeMb, downloadUrl);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) throws ValidationException {
        if (format == null || format.trim().isEmpty()) {
            throw new ValidationException("Формат электронного издания не может быть пустым.");
        }
        this.format = format.trim().toUpperCase();
    }

    public double getFileSizeMb() {
        return fileSizeMb;
    }

    public void setFileSizeMb(double fileSizeMb) throws ValidationException {
        if (fileSizeMb <= 0) {
            throw new ValidationException("Размер файла должен быть положительным числом.");
        }
        this.fileSizeMb = fileSizeMb;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) throws ValidationException {
        if (downloadUrl == null || downloadUrl.trim().isEmpty()) {
            throw new ValidationException("URL или путь к файлу не может быть пустым.");
        }
        this.downloadUrl = downloadUrl.trim();
    }
}

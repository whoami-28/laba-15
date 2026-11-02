package com.library.model;

import com.library.exception.ValidationException;

public class PrintedBook extends LibraryMaterial {
    private int pageCount;
    private String shelfLocation;
    private double dailyOverdueFine;
    private int loanPeriodDays;

    public static final double DEFAULT_DAILY_FINE = 50.0;
    public static final int DEFAULT_LOAN_DAYS = 14;

    public PrintedBook(String id, String title, String author, int publicationYear, int totalCopies,
                       int pageCount, String shelfLocation) throws ValidationException {
        this(id, title, author, publicationYear, totalCopies, totalCopies, pageCount, shelfLocation,
             DEFAULT_DAILY_FINE, DEFAULT_LOAN_DAYS);
    }

    public PrintedBook(String id, String title, String author, int publicationYear, int totalCopies,
                       int pageCount, String shelfLocation,
                       double dailyOverdueFine, int loanPeriodDays) throws ValidationException {
        this(id, title, author, publicationYear, totalCopies, totalCopies, pageCount, shelfLocation,
             dailyOverdueFine, loanPeriodDays);
    }

    public PrintedBook(String id, String title, String author, int publicationYear, int totalCopies,
                       int availableCopies, int pageCount, String shelfLocation,
                       double dailyOverdueFine, int loanPeriodDays) throws ValidationException {
        super(id, title, author, publicationYear, totalCopies, availableCopies);
        setPageCount(pageCount);
        setShelfLocation(shelfLocation);
        setDailyOverdueFine(dailyOverdueFine);
        setLoanPeriodDays(loanPeriodDays);
    }

    @Override
    public String getMaterialType() {
        return "Печатная книга";
    }

    @Override
    public int getDefaultLoanDays() {
        return loanPeriodDays;
    }

    @Override
    public double calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return 0.0;
        }
        return overdueDays * dailyOverdueFine;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Стр.: %d | Полка: %s | Тариф штрафа: %.2f руб./день | Срок выдачи: %d дн.",
                pageCount, shelfLocation, dailyOverdueFine, loanPeriodDays);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) throws ValidationException {
        if (pageCount <= 0) {
            throw new ValidationException("Количество страниц должно быть положительным числом.");
        }
        this.pageCount = pageCount;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) throws ValidationException {
        if (shelfLocation == null || shelfLocation.trim().isEmpty()) {
            throw new ValidationException("Местоположение на полке не может быть пустым.");
        }
        this.shelfLocation = shelfLocation.trim();
    }

    public double getDailyOverdueFine() {
        return dailyOverdueFine;
    }

    public void setDailyOverdueFine(double dailyOverdueFine) throws ValidationException {
        if (dailyOverdueFine < 0) {
            throw new ValidationException("Штраф за день просрочки не может быть отрицательным.");
        }
        this.dailyOverdueFine = dailyOverdueFine;
    }

    public int getLoanPeriodDays() {
        return loanPeriodDays;
    }

    public void setLoanPeriodDays(int loanPeriodDays) throws ValidationException {
        if (loanPeriodDays <= 0) {
            throw new ValidationException("Срок выдачи должен быть больше 0 дней.");
        }
        this.loanPeriodDays = loanPeriodDays;
    }
}

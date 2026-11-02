package com.library.model;

import com.library.exception.MaterialNotAvailableException;
import com.library.exception.ValidationException;

import java.time.Year;
import java.util.Objects;

public abstract class LibraryMaterial implements Identifiable<String>, Borrowable {
    private String id;
    private String title;
    private String author;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    public LibraryMaterial(String id, String title, String author, int publicationYear, int totalCopies)
            throws ValidationException {
        setId(id);
        setTitle(title);
        setAuthor(author);
        setPublicationYear(publicationYear);
        setTotalCopies(totalCopies);
        this.availableCopies = totalCopies;
    }

    public LibraryMaterial(String id, String title, String author, int publicationYear, int totalCopies, int availableCopies)
            throws ValidationException {
        setId(id);
        setTitle(title);
        setAuthor(author);
        setPublicationYear(publicationYear);
        setTotalCopies(totalCopies);
        setAvailableCopies(availableCopies);
    }

    public abstract String getMaterialType();

    public abstract int getDefaultLoanDays();

    public abstract double calculateFine(int overdueDays);

    public abstract String getSpecificDetails();

    @Override
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public synchronized void borrowCopy() throws MaterialNotAvailableException {
        if (!isAvailable()) {
            throw new MaterialNotAvailableException(
                    "Материал \"" + title + "\" (ID: " + id + ") временно недоступен для выдачи (все экземпляры заняты)."
            );
        }
        this.availableCopies--;
    }

    @Override
    public synchronized void returnCopy() {
        if (availableCopies < totalCopies) {
            this.availableCopies++;
        }
    }

    @Override
    public int getAvailableCopies() {
        return availableCopies;
    }

    public String getFormattedSummary() {
        return getFormattedSummary(false);
    }

    public String getFormattedSummary(boolean includeDetails) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] ID: %s | \"%s\" — %s (%d г.) | Доступно: %d/%d",
                getMaterialType(), id, title, author, publicationYear, availableCopies, totalCopies));
        if (includeDetails) {
            sb.append("\n    Детали: ").append(getSpecificDetails());
        }
        return sb.toString();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Идентификатор материала не может быть пустым.");
        }
        this.id = id.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Название материала не может быть пустым.");
        }
        this.title = title.trim();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) throws ValidationException {
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Автор материала не может быть пустым.");
        }
        this.author = author.trim();
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) throws ValidationException {
        int currentYear = Year.now().getValue() + 1;
        if (publicationYear < 868 || publicationYear > currentYear) {
            throw new ValidationException(
                    "Год издания должен быть в диапазоне от 868 до " + currentYear + ", получено: " + publicationYear
            );
        }
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) throws ValidationException {
        if (totalCopies < 0) {
            throw new ValidationException("Общее количество копий не может быть отрицательным.");
        }
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(int availableCopies) throws ValidationException {
        if (availableCopies < 0) {
            throw new ValidationException("Количество доступных копий не может быть отрицательным.");
        }
        if (availableCopies > this.totalCopies) {
            throw new ValidationException("Доступное количество (" + availableCopies +
                    ") не может превышать общее количество (" + this.totalCopies + ").");
        }
        this.availableCopies = availableCopies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryMaterial that = (LibraryMaterial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getFormattedSummary(true);
    }
}

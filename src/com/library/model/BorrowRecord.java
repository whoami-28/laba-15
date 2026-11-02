package com.library.model;

import com.library.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class BorrowRecord implements Identifiable<String> {
    private String id;
    private String materialId;
    private String materialTitle;
    private String readerId;
    private String readerName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineCharged;
    private boolean active;

    public BorrowRecord(String id, String materialId, String materialTitle,
                        String readerId, String readerName,
                        LocalDate borrowDate, LocalDate dueDate) throws ValidationException {
        this(id, materialId, materialTitle, readerId, readerName, borrowDate, dueDate, null, 0.0, true);
    }

    public BorrowRecord(String id, String materialId, String materialTitle,
                        String readerId, String readerName,
                        LocalDate borrowDate, LocalDate dueDate,
                        LocalDate returnDate, double fineCharged, boolean active) throws ValidationException {
        setId(id);
        setMaterialId(materialId);
        setMaterialTitle(materialTitle);
        setReaderId(readerId);
        setReaderName(readerName);
        setBorrowDate(borrowDate);
        setDueDate(dueDate);
        this.returnDate = returnDate;
        this.fineCharged = fineCharged;
        this.active = active;
    }

    public boolean isOverdue(LocalDate onDate) {
        if (!active) {
            return returnDate != null && returnDate.isAfter(dueDate);
        }
        return onDate.isAfter(dueDate);
    }

    public int getOverdueDays(LocalDate onDate) {
        LocalDate endPoint = (returnDate != null) ? returnDate : onDate;
        if (endPoint.isAfter(dueDate)) {
            return (int) ChronoUnit.DAYS.between(dueDate, endPoint);
        }
        return 0;
    }

    public void completeReturn(LocalDate returnDate, double fineCharged) {
        this.returnDate = returnDate;
        this.fineCharged = fineCharged;
        this.active = false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Идентификатор записи не может быть пустым.");
        }
        this.id = id.trim();
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) throws ValidationException {
        if (materialId == null || materialId.trim().isEmpty()) {
            throw new ValidationException("Идентификатор материала не может быть пустым.");
        }
        this.materialId = materialId.trim();
    }

    public String getMaterialTitle() {
        return materialTitle;
    }

    public void setMaterialTitle(String materialTitle) {
        this.materialTitle = materialTitle != null ? materialTitle.trim() : "";
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) throws ValidationException {
        if (readerId == null || readerId.trim().isEmpty()) {
            throw new ValidationException("Идентификатор читателя не может быть пустым.");
        }
        this.readerId = readerId.trim();
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName != null ? readerName.trim() : "";
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) throws ValidationException {
        if (borrowDate == null) {
            throw new ValidationException("Дата выдачи не может быть null.");
        }
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) throws ValidationException {
        if (dueDate == null) {
            throw new ValidationException("Срок возврата не может быть null.");
        }
        if (this.borrowDate != null && dueDate.isBefore(this.borrowDate)) {
            throw new ValidationException("Срок возврата не может предшествовать дате выдачи.");
        }
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineCharged() {
        return fineCharged;
    }

    public void setFineCharged(double fineCharged) {
        this.fineCharged = Math.max(0.0, fineCharged);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecord that = (BorrowRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String status = active ? "АКТИВНА" : "ЗАКРЫТА (" + returnDate + ")";
        String fineStr = fineCharged > 0 ? String.format(", Штраф: %.2f руб.", fineCharged) : "";
        return String.format("Выдача [#%s] \"%s\" -> Читатель: %s [%s] | Выдано: %s | Срок: %s | Статус: %s%s",
                id, materialTitle, readerName, readerId, borrowDate, dueDate, status, fineStr);
    }
}

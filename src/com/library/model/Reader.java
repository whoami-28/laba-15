package com.library.model;

import com.library.exception.ValidationException;

import java.util.Objects;

public class Reader implements Identifiable<String> {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private int maxBorrowLimit;

    public static final int DEFAULT_MAX_BORROW_LIMIT = 3;

    public Reader(String id, String fullName, String email, String phone) throws ValidationException {
        this(id, fullName, email, phone, DEFAULT_MAX_BORROW_LIMIT);
    }

    public Reader(String id, String fullName, String email, String phone, int maxBorrowLimit)
            throws ValidationException {
        setId(id);
        setFullName(fullName);
        setEmail(email);
        setPhone(phone);
        setMaxBorrowLimit(maxBorrowLimit);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) throws ValidationException {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Номер читательского билета не может быть пустым.");
        }
        this.id = id.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("ФИО читателя не может быть пустым.");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws ValidationException {
        if (email == null || !email.contains("@")) {
            throw new ValidationException("Некорректный адрес электронной почты: " + email);
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Номер телефона не может быть пустым.");
        }
        this.phone = phone.trim();
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) throws ValidationException {
        if (maxBorrowLimit <= 0) {
            throw new ValidationException("Лимит выдачи должен быть больше 0.");
        }
        this.maxBorrowLimit = maxBorrowLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reader reader = (Reader) o;
        return Objects.equals(id, reader.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Читатель [#%s] %s (Тел: %s, Email: %s, Лимит: %d книг)",
                id, fullName, phone, email, maxBorrowLimit);
    }
}

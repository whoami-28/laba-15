package com.library.storage;

import com.library.exception.StorageException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.ElectronicBook;
import com.library.model.LibraryMaterial;
import com.library.model.PrintedBook;
import com.library.model.Reader;
import com.library.repository.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class FileStorageManager {
    private final String dataDirectory;
    private static final String DELIMITER = ";";

    public FileStorageManager() {
        this("data");
    }

    public FileStorageManager(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private String getMaterialsFilePath() {
        return dataDirectory + File.separator + "materials.txt";
    }

    private String getReadersFilePath() {
        return dataDirectory + File.separator + "readers.txt";
    }

    private String getBorrowsFilePath() {
        return dataDirectory + File.separator + "borrows.txt";
    }

    public void saveAll(Repository<LibraryMaterial, String> materialRepo,
                        Repository<Reader, String> readerRepo,
                        Repository<BorrowRecord, String> borrowRepo) throws StorageException {
        saveMaterials(materialRepo.findAll());
        saveReaders(readerRepo.findAll());
        saveBorrows(borrowRepo.findAll());
    }

    public void saveMaterials(List<LibraryMaterial> materials) throws StorageException {
        BufferedWriter writer = null;
        try {
            ensureDirectoryExists();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getMaterialsFilePath()), StandardCharsets.UTF_8));
            for (LibraryMaterial m : materials) {
                if (m instanceof PrintedBook) {
                    PrintedBook b = (PrintedBook) m;
                    writer.write(String.join(DELIMITER,
                            "PRINTED",
                            b.getId(),
                            b.getTitle(),
                            b.getAuthor(),
                            String.valueOf(b.getPublicationYear()),
                            String.valueOf(b.getTotalCopies()),
                            String.valueOf(b.getAvailableCopies()),
                            String.valueOf(b.getPageCount()),
                            b.getShelfLocation(),
                            String.valueOf(b.getDailyOverdueFine()),
                            String.valueOf(b.getLoanPeriodDays())
                    ));
                } else if (m instanceof ElectronicBook) {
                    ElectronicBook e = (ElectronicBook) m;
                    writer.write(String.join(DELIMITER,
                            "ELECTRONIC",
                            e.getId(),
                            e.getTitle(),
                            e.getAuthor(),
                            String.valueOf(e.getPublicationYear()),
                            String.valueOf(e.getTotalCopies()),
                            String.valueOf(e.getAvailableCopies()),
                            e.getFormat(),
                            String.valueOf(e.getFileSizeMb()),
                            e.getDownloadUrl()
                    ));
                }
                writer.newLine();
            }
        } catch (IOException e) {
            throw new StorageException("Ошибка записи файла материалов: " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока записи: " + e.getMessage());
                }
            }
        }
    }

    public void saveReaders(List<Reader> readers) throws StorageException {
        BufferedWriter writer = null;
        try {
            ensureDirectoryExists();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getReadersFilePath()), StandardCharsets.UTF_8));
            for (Reader r : readers) {
                writer.write(String.join(DELIMITER,
                        r.getId(),
                        r.getFullName(),
                        r.getEmail(),
                        r.getPhone(),
                        String.valueOf(r.getMaxBorrowLimit())
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new StorageException("Ошибка записи файла читателей: " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока записи: " + e.getMessage());
                }
            }
        }
    }

    public void saveBorrows(List<BorrowRecord> records) throws StorageException {
        BufferedWriter writer = null;
        try {
            ensureDirectoryExists();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBorrowsFilePath()), StandardCharsets.UTF_8));
            for (BorrowRecord rec : records) {
                writer.write(String.join(DELIMITER,
                        rec.getId(),
                        rec.getMaterialId(),
                        rec.getMaterialTitle(),
                        rec.getReaderId(),
                        rec.getReaderName(),
                        rec.getBorrowDate().toString(),
                        rec.getDueDate().toString(),
                        rec.getReturnDate() != null ? rec.getReturnDate().toString() : "NULL",
                        String.valueOf(rec.getFineCharged()),
                        String.valueOf(rec.isActive())
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new StorageException("Ошибка записи файла выдач: " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока записи: " + e.getMessage());
                }
            }
        }
    }

    public void loadAll(Repository<LibraryMaterial, String> materialRepo,
                        Repository<Reader, String> readerRepo,
                        Repository<BorrowRecord, String> borrowRepo) throws StorageException {
        loadMaterials(materialRepo);
        loadReaders(readerRepo);
        loadBorrows(borrowRepo);
    }

    public void loadMaterials(Repository<LibraryMaterial, String> materialRepo) throws StorageException {
        File file = new File(getMaterialsFilePath());
        if (!file.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            materialRepo.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length < 7) {
                    continue;
                }
                String type = parts[0];
                String id = parts[1];
                String title = parts[2];
                String author = parts[3];
                int year = Integer.parseInt(parts[4]);
                int totalCopies = Integer.parseInt(parts[5]);
                int availableCopies = Integer.parseInt(parts[6]);

                if ("PRINTED".equalsIgnoreCase(type) && parts.length >= 11) {
                    int pages = Integer.parseInt(parts[7]);
                    String shelf = parts[8];
                    double fine = Double.parseDouble(parts[9]);
                    int loanDays = Integer.parseInt(parts[10]);
                    materialRepo.add(new PrintedBook(id, title, author, year, totalCopies, availableCopies,
                            pages, shelf, fine, loanDays));
                } else if ("ELECTRONIC".equalsIgnoreCase(type) && parts.length >= 10) {
                    String format = parts[7];
                    double size = Double.parseDouble(parts[8]);
                    String url = parts[9];
                    materialRepo.add(new ElectronicBook(id, title, author, year, totalCopies, availableCopies,
                            format, size, url));
                }
            }
        } catch (IOException | NumberFormatException | ValidationException e) {
            throw new StorageException("Ошибка чтения данных материалов: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока чтения: " + e.getMessage());
                }
            }
        }
    }

    public void loadReaders(Repository<Reader, String> readerRepo) throws StorageException {
        File file = new File(getReadersFilePath());
        if (!file.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            readerRepo.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length >= 5) {
                    String id = parts[0];
                    String name = parts[1];
                    String email = parts[2];
                    String phone = parts[3];
                    int limit = Integer.parseInt(parts[4]);
                    readerRepo.add(new Reader(id, name, email, phone, limit));
                }
            }
        } catch (IOException | NumberFormatException | ValidationException e) {
            throw new StorageException("Ошибка чтения данных читателей: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока чтения: " + e.getMessage());
                }
            }
        }
    }

    public void loadBorrows(Repository<BorrowRecord, String> borrowRepo) throws StorageException {
        File file = new File(getBorrowsFilePath());
        if (!file.exists()) {
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            borrowRepo.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(DELIMITER, -1);
                if (parts.length >= 10) {
                    String id = parts[0];
                    String matId = parts[1];
                    String matTitle = parts[2];
                    String readerId = parts[3];
                    String readerName = parts[4];
                    LocalDate borrowDate = LocalDate.parse(parts[5]);
                    LocalDate dueDate = LocalDate.parse(parts[6]);
                    LocalDate returnDate = "NULL".equalsIgnoreCase(parts[7]) ? null : LocalDate.parse(parts[7]);
                    double fine = Double.parseDouble(parts[8]);
                    boolean active = Boolean.parseBoolean(parts[9]);

                    borrowRepo.add(new BorrowRecord(id, matId, matTitle, readerId, readerName,
                            borrowDate, dueDate, returnDate, fine, active));
                }
            }
        } catch (IOException | NumberFormatException | ValidationException e) {
            throw new StorageException("Ошибка чтения данных выдач: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Ошибка при закрытии потока чтения: " + e.getMessage());
                }
            }
        }
    }

    public boolean hasExistingData() {
        return new File(getMaterialsFilePath()).exists() ||
               new File(getReadersFilePath()).exists() ||
               new File(getBorrowsFilePath()).exists();
    }
}

package com.library.service;

import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.exception.MaterialNotAvailableException;
import com.library.exception.MaterialNotFoundException;
import com.library.exception.StorageException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.LibraryMaterial;
import com.library.model.PrintedBook;
import com.library.model.ElectronicBook;
import com.library.model.Reader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LibraryService {

    void registerPrintedBook(String id, String title, String author, int year, int totalCopies,
                             int pageCount, String shelfLocation, double dailyFine, int loanDays)
            throws ValidationException;

    void registerElectronicBook(String id, String title, String author, int year, int totalCopies,
                                String format, double fileSizeMb, String downloadUrl)
            throws ValidationException;

    boolean removeMaterial(String materialId) throws LibraryException;

    Optional<LibraryMaterial> findMaterialById(String id);

    List<LibraryMaterial> getAllMaterials();

    List<LibraryMaterial> searchMaterials(String query);

    List<LibraryMaterial> searchMaterials(String query, boolean onlyAvailable);

    List<LibraryMaterial> searchMaterials(String query, String typeFilter, boolean onlyAvailable);

    void registerReader(String id, String fullName, String email, String phone, int maxLimit)
            throws ValidationException;

    Optional<Reader> findReaderById(String id);

    List<Reader> getAllReaders();

    BorrowRecord borrowMaterial(String materialId, String readerId)
            throws MaterialNotFoundException, MaterialNotAvailableException, BorrowLimitExceededException, ValidationException;

    BorrowRecord borrowMaterial(String materialId, String readerId, int customLoanDays)
            throws MaterialNotFoundException, MaterialNotAvailableException, BorrowLimitExceededException, ValidationException;

    BorrowRecord returnMaterial(String recordId) throws LibraryException;

    BorrowRecord returnMaterial(String recordId, LocalDate actualReturnDate) throws LibraryException;

    double calculateFine(BorrowRecord record);

    double calculateFine(BorrowRecord record, LocalDate onDate);

    double calculateFine(BorrowRecord record, LocalDate onDate, double customDailyRate);

    List<BorrowRecord> getAllBorrowRecords();

    List<BorrowRecord> getActiveBorrows();

    List<BorrowRecord> getOverdueBorrows(LocalDate onDate);

    List<BorrowRecord> getBorrowsByReader(String readerId);

    void saveData() throws StorageException;

    void loadData() throws StorageException;

    void seedInitialDataIfEmpty() throws ValidationException, StorageException;
}

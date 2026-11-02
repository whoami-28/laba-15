package com.library.service;

import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.exception.MaterialNotAvailableException;
import com.library.exception.MaterialNotFoundException;
import com.library.exception.StorageException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.ElectronicBook;
import com.library.model.LibraryMaterial;
import com.library.model.PrintedBook;
import com.library.model.Reader;
import com.library.repository.InMemoryRepository;
import com.library.repository.Repository;
import com.library.storage.FileStorageManager;
import com.library.util.GenericUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class LibraryServiceImpl implements LibraryService {
    private final Repository<LibraryMaterial, String> materialRepository;
    private final Repository<Reader, String> readerRepository;
    private final Repository<BorrowRecord, String> borrowRepository;
    private final FileStorageManager fileStorageManager;
    private final AtomicInteger recordCounter = new AtomicInteger(1000);

    public LibraryServiceImpl() {
        this(new InMemoryRepository<>(), new InMemoryRepository<>(), new InMemoryRepository<>(), new FileStorageManager());
    }

    public LibraryServiceImpl(Repository<LibraryMaterial, String> materialRepository,
                              Repository<Reader, String> readerRepository,
                              Repository<BorrowRecord, String> borrowRepository,
                              FileStorageManager fileStorageManager) {
        this.materialRepository = materialRepository;
        this.readerRepository = readerRepository;
        this.borrowRepository = borrowRepository;
        this.fileStorageManager = fileStorageManager;
    }

    @Override
    public synchronized void registerPrintedBook(String id, String title, String author, int year,
                                                 int totalCopies, int pageCount, String shelfLocation,
                                                 double dailyFine, int loanDays) throws ValidationException {
        if (materialRepository.findById(id).isPresent()) {
            throw new ValidationException("Материал с идентификатором " + id + " уже зарегистрирован.");
        }
        PrintedBook book = new PrintedBook(id, title, author, year, totalCopies, totalCopies,
                pageCount, shelfLocation, dailyFine, loanDays);
        materialRepository.add(book);
    }

    @Override
    public synchronized void registerElectronicBook(String id, String title, String author, int year,
                                                    int totalCopies, String format, double fileSizeMb,
                                                    String downloadUrl) throws ValidationException {
        if (materialRepository.findById(id).isPresent()) {
            throw new ValidationException("Материал с идентификатором " + id + " уже зарегистрирован.");
        }
        ElectronicBook ebook = new ElectronicBook(id, title, author, year, totalCopies, totalCopies,
                format, fileSizeMb, downloadUrl);
        materialRepository.add(ebook);
    }

    @Override
    public synchronized boolean removeMaterial(String materialId) throws LibraryException {
        if (materialId == null || materialId.trim().isEmpty()) {
            throw new ValidationException("Идентификатор материала не может быть пустым.");
        }

        List<BorrowRecord> activeBorrows = borrowRepository.findByPredicate(
                b -> b.isActive() && b.getMaterialId().equalsIgnoreCase(materialId)
        );
        if (!activeBorrows.isEmpty()) {
            throw new LibraryException(
                    "Невозможно удалить материал: найдено " + activeBorrows.size() +
                    " активных выдач читателям. Сначала оформите возврат экземпляров."
            );
        }
        boolean removed = materialRepository.deleteById(materialId);
        if (!removed) {
            throw new MaterialNotFoundException("Материал с ID " + materialId + " не найден в библиотеке.");
        }
        return true;
    }

    @Override
    public Optional<LibraryMaterial> findMaterialById(String id) {
        return materialRepository.findById(id);
    }

    @Override
    public List<LibraryMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public List<LibraryMaterial> searchMaterials(String query) {
        return searchMaterials(query, null, false);
    }

    @Override
    public List<LibraryMaterial> searchMaterials(String query, boolean onlyAvailable) {
        return searchMaterials(query, null, onlyAvailable);
    }

    @Override
    public List<LibraryMaterial> searchMaterials(String query, String typeFilter, boolean onlyAvailable) {
        final String q = (query == null) ? "" : query.trim().toLowerCase();
        return GenericUtils.filter(materialRepository.findAll(), material -> {

            boolean matchesQuery = q.isEmpty()
                    || material.getTitle().toLowerCase().contains(q)
                    || material.getAuthor().toLowerCase().contains(q)
                    || material.getId().toLowerCase().contains(q);
            if (!matchesQuery) {
                return false;
            }

            if (onlyAvailable && !material.isAvailable()) {
                return false;
            }

            if (typeFilter != null && !typeFilter.trim().isEmpty()) {
                if (!material.getMaterialType().equalsIgnoreCase(typeFilter.trim())) {
                    return false;
                }
            }

            return true;
        });
    }

    @Override
    public synchronized void registerReader(String id, String fullName, String email, String phone, int maxLimit)
            throws ValidationException {
        if (readerRepository.findById(id).isPresent()) {
            throw new ValidationException("Читатель с номером билета " + id + " уже зарегистрирован.");
        }
        Reader reader = new Reader(id, fullName, email, phone, maxLimit);
        readerRepository.add(reader);
    }

    @Override
    public Optional<Reader> findReaderById(String id) {
        return readerRepository.findById(id);
    }

    @Override
    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    @Override
    public BorrowRecord borrowMaterial(String materialId, String readerId)
            throws MaterialNotFoundException, MaterialNotAvailableException, BorrowLimitExceededException, ValidationException {
        Optional<LibraryMaterial> materialOpt = materialRepository.findById(materialId);
        if (materialOpt.isEmpty()) {
            throw new MaterialNotFoundException("Материал с ID " + materialId + " не найден в библиотеке.");
        }
        int defaultDays = materialOpt.get().getDefaultLoanDays();
        return borrowMaterial(materialId, readerId, defaultDays);
    }

    @Override
    public synchronized BorrowRecord borrowMaterial(String materialId, String readerId, int customLoanDays)
            throws MaterialNotFoundException, MaterialNotAvailableException, BorrowLimitExceededException, ValidationException {
        if (customLoanDays <= 0) {
            throw new ValidationException("Срок выдачи должен быть больше 0 дней.");
        }

        LibraryMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException("Материал с ID " + materialId + " не найден."));

        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ValidationException("Читатель с ID " + readerId + " не найден."));

        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdueRecords = GenericUtils.filter(
                borrowRepository.findAll(),
                rec -> rec.isActive() && rec.getReaderId().equals(readerId) && rec.isOverdue(today)
        );
        if (!overdueRecords.isEmpty()) {
            throw new BorrowLimitExceededException(
                    "У читателя " + reader.getFullName() + " имеются просроченные материалы (" +
                    overdueRecords.size() + " шт.). Выдача заблокирована до возврата долга и оплаты штрафа."
            );
        }

        if (material instanceof PrintedBook) {
            List<BorrowRecord> activePrintedLoans = GenericUtils.filter(
                    borrowRepository.findAll(),
                    rec -> rec.isActive() && rec.getReaderId().equals(readerId) &&
                            materialRepository.findById(rec.getMaterialId())
                                    .map(m -> m instanceof PrintedBook)
                                    .orElse(false)
            );
            if (activePrintedLoans.size() >= reader.getMaxBorrowLimit()) {
                throw new BorrowLimitExceededException(
                        "Превышен допустимый лимит выдачи печатных книг (" +
                        reader.getMaxBorrowLimit() + " шт.) для читателя " + reader.getFullName() + "."
                );
            }
        }

        material.borrowCopy();

        String recordId = "BR-" + recordCounter.incrementAndGet();
        LocalDate dueDate = today.plusDays(customLoanDays);
        BorrowRecord record = new BorrowRecord(
                recordId, material.getId(), material.getTitle(),
                reader.getId(), reader.getFullName(),
                today, dueDate
        );

        borrowRepository.add(record);
        materialRepository.update(material);

        return record;
    }

    @Override
    public BorrowRecord returnMaterial(String recordId) throws LibraryException {
        return returnMaterial(recordId, LocalDate.now());
    }

    @Override
    public synchronized BorrowRecord returnMaterial(String recordId, LocalDate actualReturnDate)
            throws LibraryException {
        if (actualReturnDate == null) {
            throw new ValidationException("Дата фактического возврата не может быть null.");
        }

        BorrowRecord record = borrowRepository.findById(recordId)
                .orElseThrow(() -> new LibraryException("Запись о выдаче с ID " + recordId + " не найдена."));

        if (!record.isActive()) {
            throw new LibraryException("Материал по данной записи уже был возвращён ранее (" + record.getReturnDate() + ").");
        }

        LibraryMaterial material = materialRepository.findById(record.getMaterialId()).orElse(null);
        double fine = 0.0;

        if (material != null) {

            material.returnCopy();
            materialRepository.update(material);

            int overdueDays = record.getOverdueDays(actualReturnDate);
            fine = material.calculateFine(overdueDays);
        }

        record.completeReturn(actualReturnDate, fine);
        borrowRepository.update(record);

        return record;
    }

    @Override
    public double calculateFine(BorrowRecord record) {
        return calculateFine(record, LocalDate.now());
    }

    @Override
    public double calculateFine(BorrowRecord record, LocalDate onDate) {
        if (record == null) {
            return 0.0;
        }
        int overdueDays = record.getOverdueDays(onDate);
        if (overdueDays <= 0) {
            return 0.0;
        }
        Optional<LibraryMaterial> matOpt = materialRepository.findById(record.getMaterialId());
        if (matOpt.isPresent()) {
            return matOpt.get().calculateFine(overdueDays);
        }
        return PrintedBook.DEFAULT_DAILY_FINE * overdueDays;
    }

    @Override
    public double calculateFine(BorrowRecord record, LocalDate onDate, double customDailyRate) {
        if (record == null || customDailyRate < 0) {
            return 0.0;
        }
        int overdueDays = record.getOverdueDays(onDate);
        if (overdueDays <= 0) {
            return 0.0;
        }
        return overdueDays * customDailyRate;
    }

    @Override
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowRepository.findAll();
    }

    @Override
    public List<BorrowRecord> getActiveBorrows() {
        return GenericUtils.filter(borrowRepository.findAll(), BorrowRecord::isActive);
    }

    @Override
    public List<BorrowRecord> getOverdueBorrows(LocalDate onDate) {
        LocalDate checkDate = (onDate != null) ? onDate : LocalDate.now();
        return GenericUtils.filter(borrowRepository.findAll(), rec -> rec.isActive() && rec.isOverdue(checkDate));
    }

    @Override
    public List<BorrowRecord> getBorrowsByReader(String readerId) {
        return GenericUtils.filter(borrowRepository.findAll(), rec -> rec.getReaderId().equalsIgnoreCase(readerId));
    }

    @Override
    public synchronized void saveData() throws StorageException {
        fileStorageManager.saveAll(materialRepository, readerRepository, borrowRepository);
    }

    @Override
    public synchronized void loadData() throws StorageException {
        fileStorageManager.loadAll(materialRepository, readerRepository, borrowRepository);

        int maxId = 1000;
        for (BorrowRecord r : borrowRepository.findAll()) {
            try {
                if (r.getId().startsWith("BR-")) {
                    int num = Integer.parseInt(r.getId().substring(3));
                    if (num > maxId) {
                        maxId = num;
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
        recordCounter.set(maxId);
    }

    @Override
    public synchronized void seedInitialDataIfEmpty() throws ValidationException, StorageException {
        if (materialRepository.count() > 0 || readerRepository.count() > 0) {
            return;
        }

        if (fileStorageManager.hasExistingData()) {
            loadData();
            if (materialRepository.count() > 0) {
                return;
            }
        }

        registerPrintedBook("PB-101", "Война и мир (Том 1)", "Лев Толстой", 1869, 3,
                480, "A-1-01", 50.0, 14);
        registerPrintedBook("PB-102", "Преступление и наказание", "Фёдор Достоевский", 1866, 2,
                380, "A-1-02", 50.0, 14);
        registerPrintedBook("PB-103", "Чистый код", "Роберт Мартин", 2008, 2,
                464, "B-2-05", 70.0, 21);
        registerPrintedBook("PB-104", "Паттерны объектно-ориентированного проектирования", "Эрих Гамма и др.", 1994, 1,
                395, "B-2-06", 80.0, 14);

        registerElectronicBook("EB-201", "Effective Java (3rd Edition)", "Joshua Bloch", 2018, 10,
                "PDF", 15.4, "https://library.local/ebooks/effective-java-3e.pdf");
        registerElectronicBook("EB-202", "Java Concurrency in Practice", "Brian Goetz", 2006, 10,
                "EPUB", 8.2, "https://library.local/ebooks/java-concurrency.epub");
        registerElectronicBook("EB-203", "Мастер и Маргарита", "Михаил Булгаков", 1967, 50,
                "FB2", 3.1, "https://library.local/ebooks/master-i-margarita.fb2");

        registerReader("R-001", "Иванов Иван Иванович", "ivanov@example.com", "+7-999-111-22-33", 3);
        registerReader("R-002", "Петрова Анна Сергеевна", "petrova@example.com", "+7-999-222-33-44", 2);
        registerReader("R-003", "Сидоров Алексей Павлович", "sidorov@example.com", "+7-999-333-44-55", 3);

        saveData();
    }
}

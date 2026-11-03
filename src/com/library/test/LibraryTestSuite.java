package com.library.test;

import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.exception.MaterialNotAvailableException;
import com.library.exception.ValidationException;
import com.library.model.BorrowRecord;
import com.library.model.ElectronicBook;
import com.library.model.LibraryMaterial;
import com.library.model.PrintedBook;
import com.library.model.Reader;
import com.library.repository.InMemoryRepository;
import com.library.service.LibraryService;
import com.library.service.LibraryServiceImpl;
import com.library.storage.FileStorageManager;
import com.library.util.GenericUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class LibraryTestSuite {
    private int passedCount = 0;
    private int failedCount = 0;

    public static void main(String[] args) {
        LibraryTestSuite suite = new LibraryTestSuite();
        suite.runAllTests();
    }

    public boolean runAllTests() {
        System.out.println("=================================================");
        System.out.println("   ЗАПУСК АВТОМАТИЧЕСКИХ ТЕСТОВ (ВАРИАНТ 3)     ");
        System.out.println("=================================================");

        testModelCreationAndValidation();
        testInheritanceAndPolymorphicFineCalculation();
        testGenericRepositoryAndUtils();
        testBorrowSuccessAndCopyDecrement();
        testBorrowRejectionWhenUnavailable();
        testBorrowLimitExceeded();
        testOverdueFineCalculationOnReturn();
        testBorrowBlockedWhenOverdueExists();
        testCannotDeleteBorrowedMaterial();
        testFilePersistenceAndRecovery();

        System.out.println("-------------------------------------------------");
        System.out.printf("ИТОГО: Пройдено: %d | Провалено: %d\n", passedCount, failedCount);
        System.out.println("=================================================");
        return failedCount == 0;
    }

    private void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passedCount++;
        } else {
            System.err.println("[FAIL] " + testName + " -> " + message);
            failedCount++;
        }
    }

    private void testModelCreationAndValidation() {

        try {
            PrintedBook book = new PrintedBook("P-1", "Тест Книга", "Автор", 2020, 5, 200, "A-1");
            assertTrue("Создание валидной книги", book.getTotalCopies() == 5 && book.getAvailableCopies() == 5,
                    "Копии не совпадают");
        } catch (ValidationException e) {
            assertTrue("Создание валидной книги", false, e.getMessage());
        }

        try {
            new PrintedBook("P-2", "Тест", "Автор", 2020, -1, 100, "A-1");
            assertTrue("Валидация отрицательных копий", false, "Исключение не было выброшено");
        } catch (ValidationException e) {
            assertTrue("Валидация отрицательных копий", true, "");
        }

        try {
            new PrintedBook("P-3", "Тест", "Автор", 3000, 1, 100, "A-1");
            assertTrue("Валидация года издания", false, "Исключение не было выброшено");
        } catch (ValidationException e) {
            assertTrue("Валидация года издания", true, "");
        }
    }

    private void testInheritanceAndPolymorphicFineCalculation() {
        try {
            LibraryMaterial printed = new PrintedBook("P-10", "Книга", "Автор", 2021, 1, 150, "A-1", 40.0, 14);
            LibraryMaterial digital = new ElectronicBook("E-10", "Электронка", "Автор", 2021, 1, "PDF", 5.0, "http://test");

            double printedFine = printed.calculateFine(5);
            double digitalFine = digital.calculateFine(5);

            assertTrue("Полиморфный расчет штрафа печатной книги", printedFine == 200.0,
                    "Ожидалось 200.0, получено " + printedFine);
            assertTrue("Полиморфный расчет штрафа электронного издания", digitalFine == 0.0,
                    "Ожидалось 0.0, получено " + digitalFine);
            assertTrue("Полиморфный тип печатной книги", "Печатная книга".equals(printed.getMaterialType()), "");
            assertTrue("Полиморфный тип электронного издания", "Электронное издание".equals(digital.getMaterialType()), "");
        } catch (Exception e) {
            assertTrue("Наследование и полиморфизм", false, e.getMessage());
        }
    }

    private void testGenericRepositoryAndUtils() {
        InMemoryRepository<Reader, String> repo = new InMemoryRepository<>();
        try {
            Reader r1 = new Reader("R-1", "Иван", "ivan@test.com", "123", 3);
            Reader r2 = new Reader("R-2", "Петр", "petr@test.com", "456", 2);
            repo.add(r1);
            repo.add(r2);

            assertTrue("Обобщённый репозиторий: поиск по ID", repo.findById("R-1").isPresent(), "Не найден R-1");
            assertTrue("Обобщённый репозиторий: размер", repo.count() == 2, "Размер не 2");

            List<Reader> filtered = GenericUtils.filter(repo.findAll(), r -> r.getMaxBorrowLimit() > 2);
            assertTrue("Обобщённый метод GenericUtils.filter", filtered.size() == 1 && filtered.get(0).getId().equals("R-1"),
                    "Ошибка фильтрации");
        } catch (Exception e) {
            assertTrue("Обобщённые репозиторий и утилиты", false, e.getMessage());
        }
    }

    private void testBorrowSuccessAndCopyDecrement() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("PB-1", "Java", "Гослинг", 2015, 2, 500, "A-1", 50.0, 14);
            service.registerReader("R-1", "Тестовый Читатель", "test@test.ru", "12345", 3);

            BorrowRecord record = service.borrowMaterial("PB-1", "R-1");
            LibraryMaterial material = service.findMaterialById("PB-1").orElseThrow();

            assertTrue("Успешная выдача: списание копии", material.getAvailableCopies() == 1,
                    "Ожидалась 1 копия, осталось " + material.getAvailableCopies());
            assertTrue("Успешная выдача: активная запись", record.isActive(), "Запись не активна");
        } catch (Exception e) {
            assertTrue("Успешная выдача материала", false, e.getMessage());
        }
    }

    private void testBorrowRejectionWhenUnavailable() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("PB-1", "Java", "Гослинг", 2015, 1, 500, "A-1", 50.0, 14);
            service.registerReader("R-1", "Читатель 1", "test1@test.ru", "111", 3);
            service.registerReader("R-2", "Читатель 2", "test2@test.ru", "222", 3);

            service.borrowMaterial("PB-1", "R-1");

            try {
                service.borrowMaterial("PB-1", "R-2");
                assertTrue("Отказ при отсутствии свободных копий", false, "Исключение не выброшено");
            } catch (MaterialNotAvailableException e) {
                assertTrue("Отказ при отсутствии свободных копий (MaterialNotAvailableException)", true, "");
            }
        } catch (Exception e) {
            assertTrue("Отказ при отсутствии копий", false, e.getMessage());
        }
    }

    private void testBorrowLimitExceeded() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("B-1", "Книга 1", "Автор 1", 2020, 5, 200, "A-1", 50.0, 14);
            service.registerPrintedBook("B-2", "Книга 2", "Автор 2", 2020, 5, 200, "A-2", 50.0, 14);
            service.registerReader("R-1", "Читатель", "mail@mail.ru", "123", 1);

            service.borrowMaterial("B-1", "R-1");

            try {
                service.borrowMaterial("B-2", "R-1");
                assertTrue("Отказ при превышении лимита читателя", false, "Исключение не выброшено");
            } catch (BorrowLimitExceededException e) {
                assertTrue("Отказ при превышении лимита (BorrowLimitExceededException)", true, "");
            }
        } catch (Exception e) {
            assertTrue("Проверка лимита читателя", false, e.getMessage());
        }
    }

    private void testOverdueFineCalculationOnReturn() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("PB-FINE", "Штрафная Книга", "Автор", 2020, 2, 300, "S-1", 50.0, 10);
            service.registerReader("R-FINE", "Должник", "fine@test.com", "999", 5);

            BorrowRecord record = service.borrowMaterial("PB-FINE", "R-FINE", 10);

            LocalDate actualReturnDate = record.getDueDate().plusDays(5);
            BorrowRecord closedRecord = service.returnMaterial(record.getId(), actualReturnDate);

            double expectedFine = 5 * 50.0;
            assertTrue("Расчет штрафа за просрочку возврата печатной книги",
                    closedRecord.getFineCharged() == expectedFine,
                    "Ожидался штраф " + expectedFine + ", начислено: " + closedRecord.getFineCharged());

            LibraryMaterial mat = service.findMaterialById("PB-FINE").orElseThrow();
            assertTrue("Возврат копии в фонд после возврата", mat.getAvailableCopies() == 2, "");
        } catch (Exception e) {
            assertTrue("Расчет штрафа за просрочку", false, e.getMessage());
        }
    }

    private void testBorrowBlockedWhenOverdueExists() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("B-1", "Книга 1", "Автор 1", 2020, 2, 200, "A-1", 50.0, 5);
            service.registerPrintedBook("B-2", "Книга 2", "Автор 2", 2020, 2, 200, "A-2", 50.0, 5);
            service.registerReader("R-OVER", "Забывчивый Читатель", "over@test.com", "123", 5);

            BorrowRecord rec = service.borrowMaterial("B-1", "R-OVER", 5);

            rec.setBorrowDate(LocalDate.now().minusDays(10));
            rec.setDueDate(LocalDate.now().minusDays(2));

            try {
                service.borrowMaterial("B-2", "R-OVER");
                assertTrue("Блокировка выдачи при наличии просроченных книг", false, "Исключение не выброшено");
            } catch (BorrowLimitExceededException e) {
                assertTrue("Блокировка выдачи должнику (BorrowLimitExceededException)", true, "");
            }
        } catch (Exception e) {
            assertTrue("Блокировка выдачи при просрочке", false, e.getMessage());
        }
    }

    private void testCannotDeleteBorrowedMaterial() {
        LibraryService service = createCleanService();
        try {
            service.registerPrintedBook("B-LOCK", "Неудаляемая Книга", "Автор", 2020, 1, 100, "L-1", 50.0, 14);
            service.registerReader("R-USER", "Читатель", "user@test.com", "123", 5);

            service.borrowMaterial("B-LOCK", "R-USER");

            try {
                service.removeMaterial("B-LOCK");
                assertTrue("Запрет удаления материала, находящегося на руках", false, "Исключение не выброшено");
            } catch (LibraryException e) {
                assertTrue("Запрет удаления выданного материала (LibraryException)", true, "");
            }
        } catch (Exception e) {
            assertTrue("Запрет удаления выданного материала", false, e.getMessage());
        }
    }

    private void testFilePersistenceAndRecovery() {
        String testDir = "build/test_data_" + System.currentTimeMillis();
        FileStorageManager storage = new FileStorageManager(testDir);
        LibraryService service1 = new LibraryServiceImpl(
                new InMemoryRepository<>(), new InMemoryRepository<>(), new InMemoryRepository<>(), storage
        );

        try {
            service1.registerPrintedBook("P-PERSIST", "Книга Памяти", "Автор", 2022, 3, 250, "P-1", 60.0, 14);
            service1.registerElectronicBook("E-PERSIST", "Цифровой Архив", "Автор", 2023, 10, "EPUB", 12.5, "http://dl");
            service1.registerReader("R-PERSIST", "Сохраненный Читатель", "p@test.com", "555", 4);
            service1.borrowMaterial("P-PERSIST", "R-PERSIST");

            service1.saveData();

            LibraryService service2 = new LibraryServiceImpl(
                    new InMemoryRepository<>(), new InMemoryRepository<>(), new InMemoryRepository<>(), storage
            );
            service2.loadData();

            assertTrue("Восстановление: количество материалов", service2.getAllMaterials().size() == 2, "");
            assertTrue("Восстановление: количество читателей", service2.getAllReaders().size() == 1, "");
            assertTrue("Восстановление: активные выдачи", service2.getActiveBorrows().size() == 1, "");

            LibraryMaterial mat = service2.findMaterialById("P-PERSIST").orElseThrow();
            assertTrue("Восстановление: сохранение доступных копий с учетом выдачи", mat.getAvailableCopies() == 2, "");
        } catch (Exception e) {
            assertTrue("Сохранение и восстановление данных из файла", false, e.getMessage());
        } finally {

            deleteDirectory(new File(testDir));
        }
    }

    private LibraryService createCleanService() {
        return new LibraryServiceImpl(
                new InMemoryRepository<>(),
                new InMemoryRepository<>(),
                new InMemoryRepository<>(),
                new FileStorageManager("build/temp_storage")
        );
    }

    private void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}

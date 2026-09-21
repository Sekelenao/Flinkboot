package io.github.sekelenao.flinkboot.core.internal.resource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

import java.nio.file.FileSystems;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FileSystemSupports")
class FileSystemSupportsTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldPreventInstantiation() throws Exception {
            var constructor = FileSystemSupports.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertInstanceOf(AssertionError.class, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Type")
    class Type {

        @Test
        @DisplayName("Should return detected file system type based on default separator")
        void shouldReturnDetectedType() {
            var type = FileSystemSupports.type();
            if ("\\".equals(FileSystems.getDefault().getSeparator())) {
                assertEquals(FileSystemSupports.FileSystemType.WINDOWS, type);
            } else {
                assertEquals(FileSystemSupports.FileSystemType.POSIX, type);
            }
        }
    }

    @Nested
    @DisplayName("Normalize")
    class Normalize {

        @Test
        @DisplayName("Should delegate to detected file system type when type is omitted")
        void shouldDelegateToDetectedType() {
            var location = "///C:/job.yaml";
            var expected = FileSystemSupports.normalize(location, FileSystemSupports.type());
            assertEquals(expected, FileSystemSupports.normalize(location));
        }

        @Test
        @DisplayName("Should throw NullPointerException with explicit message when location or type is null")
        void shouldThrowExceptionWhenNull() {
            assertAll(
                () -> {
                    var exception = assertThrows(NullPointerException.class, () -> FileSystemSupports.normalize(null));
                    assertEquals("location must not be null", exception.getMessage());
                },
                () -> {
                    var exception = assertThrows(NullPointerException.class, () -> FileSystemSupports.normalize(null, FileSystemSupports.FileSystemType.POSIX));
                    assertEquals("location must not be null", exception.getMessage());
                },
                () -> {
                    var exception = assertThrows(NullPointerException.class, () -> FileSystemSupports.normalize(null, FileSystemSupports.FileSystemType.WINDOWS));
                    assertEquals("location must not be null", exception.getMessage());
                },
                () -> {
                    var exception = assertThrows(NullPointerException.class, () -> FileSystemSupports.normalize("/path", null));
                    assertEquals("type must not be null", exception.getMessage());
                }
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "/c:/job.yaml",
            "///c:/job.yaml",
            "/etc/hosts",
            "///var/log/app.log",
            "relative/path/job.yaml",
            "",
            "   "
        })
        @DisplayName("Should keep location unchanged on POSIX file systems")
        void shouldKeepLocationUnchangedOnPosix(String location) {
            var normalized = FileSystemSupports.normalize(location, FileSystemSupports.FileSystemType.POSIX);
            assertEquals(location, normalized);
        }

        @ParameterizedTest
        @CsvSource({
            "///C:/job.yaml, C:/job.yaml",
            "/C:/job.yaml, C:/job.yaml",
            "////C:/job.yaml, C:/job.yaml",
            "///c:/job.yaml, c:/job.yaml",
            "/D:/job.yaml, D:/job.yaml",
            "///C:\\job.yaml, C:\\job.yaml",
            "\\C:\\job.yaml, C:\\job.yaml",
            "\\\\\\C:\\job.yaml, C:\\job.yaml",
            "/\\C:/job.yaml, C:/job.yaml",
            "\\/C:/job.yaml, C:/job.yaml",
            "\\/\\C:\\job.yaml, C:\\job.yaml"
        })
        @DisplayName("Should strip leading slashes before Windows drive letter on Windows file systems")
        void shouldStripLeadingSlashesBeforeDriveLetterOnWindows(String input, String expected) {
            var normalized = FileSystemSupports.normalize(input, FileSystemSupports.FileSystemType.WINDOWS);
            assertEquals(expected, normalized);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "C:/job.yaml",
            "C:\\job.yaml",
            "\\\\server\\share\\job.yaml",
            "//server/share/job.yaml",
            "\\dir\\job.yaml",
            "/dir/job.yaml",
            "///",
            "job.yaml",
            "folder/C:/job.yaml",
            "/dir/C:/job.yaml",
            "/1:/job.yaml",
            "",
            "   "
        })
        @DisplayName("Should keep non-drive or already normalized paths unchanged on Windows file systems")
        void shouldKeepPathsWithoutLeadingSlashesBeforeDriveUnchangedOnWindows(String location) {
            var normalized = FileSystemSupports.normalize(location, FileSystemSupports.FileSystemType.WINDOWS);
            assertEquals(location, normalized);
        }
    }
}

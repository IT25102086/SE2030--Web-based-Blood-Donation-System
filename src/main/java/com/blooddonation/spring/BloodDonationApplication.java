package com.blooddonation.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class BloodDonationApplication {
    public static void main(String[] args) {
        configureSqlServerAuthenticationLibrary();
        SpringApplication.run(BloodDonationApplication.class, args);
    }

    private static void configureSqlServerAuthenticationLibrary() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        Path nativeLibrary = Path.of(
                System.getProperty("user.dir"),
                "target",
                "native",
                "mssql-jdbc_auth-12.4.2.x64.dll");

        if (!Files.isRegularFile(nativeLibrary)) {
            Path m2Library = Path.of(
                    System.getProperty("user.home"),
                    ".m2", "repository", "com", "microsoft", "sqlserver", "mssql-jdbc_auth", "12.4.2.x64",
                    "mssql-jdbc_auth-12.4.2.x64.dll");
            if (Files.isRegularFile(m2Library)) {
                try {
                    Files.createDirectories(nativeLibrary.getParent());
                    Files.copy(m2Library, nativeLibrary, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {
                    nativeLibrary = m2Library;
                }
            }
        }

        if (!Files.isRegularFile(nativeLibrary)) {
            throw new IllegalStateException(
                    "SQL Server authentication library was not found at " + nativeLibrary
                            + ". Run 'mvn process-resources' before starting the application.");
        }

        try {
            System.load(nativeLibrary.toAbsolutePath().toString());
        } catch (UnsatisfiedLinkError ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("already loaded")) {
                return;
            }
            throw new IllegalStateException(
                    "Unable to load the SQL Server authentication library at " + nativeLibrary, ex);
        }
    }
}
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38
Commit at 2026-09-25T04:35:13
Commit at 2026-10-02T20:26:15
Commit at 2026-09-30T20:13:06
Commit at 2026-10-01T05:10:46
Commit at 2026-09-25T09:40:09
Commit at 2026-09-29T19:47:07
Commit at 2026-10-02T01:42:21
Commit at 2026-10-04T17:18:10
Commit at 2026-10-02T12:57:37
Commit at 2026-10-04T07:27:49
Commit at 2026-10-05T02:28:31
Commit at 2026-10-04T14:49:49
Commit at 2026-09-29T21:30:56
Commit at 2026-09-25T19:55:53
Commit at 2026-09-27T17:13:43
Commit at 2026-10-04T09:38:02
Commit at 2026-10-05T07:36:11
Commit at 2026-09-28T10:37:31
Commit at 2026-09-26T03:02:07
Commit at 2026-09-25T16:11:22

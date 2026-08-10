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
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
Commit at 2026-08-06T20:15:36
Commit at 2026-08-04T03:30:23
Commit at 2026-08-16T23:49:26
Commit at 2026-08-31T01:18:15
Commit at 2026-08-10T18:31:13
Commit at 2026-08-29T03:35:22
Commit at 2026-08-25T01:12:12
Commit at 2026-09-16T19:12:49
Commit at 2026-08-18T01:22:25
Commit at 2026-08-28T04:46:08
Commit at 2026-08-05T00:13:47
Commit at 2026-08-16T16:28:09
Commit at 2026-09-05T03:19:43
Commit at 2026-08-13T08:30:31
Commit at 2026-08-10T22:12:36
Commit at 2026-09-07T14:07:50
Commit at 2026-08-24T13:39:55
Commit at 2026-08-17T16:23:04
Commit at 2026-08-20T03:23:53
Commit at 2026-08-27T06:06:59
Commit at 2026-08-22T07:51:52
Commit at 2026-08-19T10:19:15
Commit at 2026-08-10T20:17:15

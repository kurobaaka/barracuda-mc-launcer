import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Launcher {

    private static final String GITHUB_REPO = "https://api.github.com/repos/ваш-юзернейм/ваш-репозиторий/releases/latest";
    private static final String SERVER_JAR = "server.jar";
    private static final String CONFIG_FILE = "launcher.properties";

    
    public static void main(String[] args) {
        try {
            // 1. Проверка наличия Java
            if (!checkJavaInstalled()) {
                System.out.println("Java не установлена. Скачиваем...");
                downloadJava(); // Реализуйте этот метод в зависимости от вашей логики
            }

            // 2. Проверка и загрузка сервера с GitHub
            if (!isServerUpToDate()) {
                System.out.println("Обновление сервера...");
                downloadLatestServer();
            }

            // 3. Загрузка параметров запуска
            Properties config = loadConfig();

            // 4. Запуск сервера
            startServer(config);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // Проверка установки Java
    private static boolean checkJavaInstalled() {
        try {
            Process process = new ProcessBuilder("java", "-version").start();
            process.waitFor();
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static void downloadJava() {
        try {
            // Пример для Windows: скачивание OpenJDK 17
            String downloadUrl = "https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip";
            String javaZipPath = "jdk-17.zip";
            
            System.out.println("Скачивание Java...");
            downloadFile(downloadUrl, javaZipPath);
            
            // Распаковка и добавление в PATH (упрощённо)
            System.out.println("Установка Java...");
            unzipFile(javaZipPath, "jdk-17");
            
        } catch (IOException e) {
            System.err.println("Ошибка при скачивании Java: " + e.getMessage());
        }
    }
    
    // Вспомогательный метод для загрузки файлов
    private static void downloadFile(String urlStr, String outputPath) throws IOException {
        URL url = new URL(urlStr);
        try (InputStream in = url.openStream();
             FileOutputStream out = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    // Вспомогательный метод для распаковки ZIP
    private static void unzipFile(String zipPath, String destDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir + File.separator + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
    

    // Проверка актуальности сервера
    private static boolean isServerUpToDate() throws IOException {
        // Здесь можно сравнить версии через GitHub API или локальный файл
        // Пример: проверка наличия файла server.jar
        return Files.exists(Paths.get(SERVER_JAR));
    }

    // Загрузка последней версии сервера с GitHub
    private static void downloadLatestServer() throws IOException {
        // Получаем URL для скачивания из GitHub Releases
        URL url = new URL(GITHUB_REPO);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Парсим JSON-ответ GitHub API (например, ссылку на server.jar)
        // Здесь нужна библиотека вроде Jackson или Gson для парсинга JSON
        // Примерный код:
        /*
        JsonNode response = new ObjectMapper().readTree(conn.getInputStream());
        String downloadUrl = response.get("assets").get(0).get("browser_download_url").asText();
        */

        // Загружаем файл (заглушка)
        System.out.println("Скачивание сервера...");
        // Реализуйте загрузку через InputStream и сохранение в server.jar
    }

    // Загрузка параметров запуска
    private static Properties loadConfig() throws IOException {
        Properties props = new Properties();
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            props.load(new FileReader(CONFIG_FILE));
        } else {
            // Значения по умолчанию
            props.setProperty("xmx", "4096");
            props.setProperty("xms", "1024");
            props.store(new FileWriter(CONFIG_FILE), "Launcher Config");
        }
        return props;
    }

    // Запуск сервера
    private static void startServer(Properties config) throws IOException {
        String xmx = config.getProperty("xmx", "4096");
        String xms = config.getProperty("xms", "1024");

        ProcessBuilder pb = new ProcessBuilder(
            "java",
            "-Xmx" + xmx + "M",
            "-Xms" + xms + "M",
            "-jar", SERVER_JAR,
            "nogui" // для Minecraft-сервера
        );

        pb.directory(new File("."));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        System.out.println("Запуск сервера...");
        Process process = pb.start();

        // Ожидание завершения сервера (опционально)
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Сервер прерван");
        }
    }
}
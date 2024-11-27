package org.argos.file.manager.repository;

import java.util.List;
import java.util.Map;

public interface IStorageRepository {
    Map<String, String> uploadDirectory(String localDir);
    List<String> listFiles();
    String getFileContent(String key);
}

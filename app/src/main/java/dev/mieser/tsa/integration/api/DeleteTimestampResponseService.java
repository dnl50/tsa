package dev.mieser.tsa.integration.api;

public interface DeleteTimestampResponseService {

    boolean deleteById(long id);

    void deleteAll();

}

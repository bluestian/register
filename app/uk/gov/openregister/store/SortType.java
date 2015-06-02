package uk.gov.openregister.store;

public interface SortType {
    interface SortBy {
        String sortBy();
        boolean isHistoric();
    }

    SortBy getDefault();
    SortBy getLastUpdate();
}

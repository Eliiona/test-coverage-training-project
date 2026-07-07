package lv.bootcamp.shelter.audit;

public interface AuditLogger {
    void logRejection(Long adopterId, Long animalId, RejectionReason reason);

    void logApproval(Long adopterId, Long animalId, int priorityScore);
}

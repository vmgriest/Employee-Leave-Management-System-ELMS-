package com.tcs.elms.repository;

import com.tcs.elms.entity.LeaveRequest;
import com.tcs.elms.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * Find all leave requests submitted by a given employee (by DB row id).
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    /**
     * Find all leave requests in a given status.
     */
    List<LeaveRequest> findByStatus(LeaveStatus status);

    /**
     * Find leave requests for an employee filtered by status.
     */
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Find all PENDING leave requests whose employee reports to the given manager.
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.managerId = :managerId AND lr.status = 'PENDING'")
    List<LeaveRequest> findPendingForManager(@Param("managerId") Long managerId);

    /**
     * Count overlapping approved/pending leaves for balance / overlap validation.
     */
    long countByEmployeeIdAndStatusAndStartDateBetween(Long employeeId, LeaveStatus status,
                                                        LocalDate startDate, LocalDate endDate);
}

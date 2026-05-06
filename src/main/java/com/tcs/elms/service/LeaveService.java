package com.tcs.elms.service;

import com.tcs.elms.dto.LeaveRequestDto;
import com.tcs.elms.entity.Employee;
import com.tcs.elms.entity.LeaveRequest;
import com.tcs.elms.enums.LeaveStatus;
import com.tcs.elms.enums.Role;
import com.tcs.elms.exception.AccessDeniedException;
import com.tcs.elms.exception.BusinessException;
import com.tcs.elms.exception.ResourceNotFoundException;
import com.tcs.elms.repository.EmployeeRepository;
import com.tcs.elms.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    // -------------------------------------------------------------------------
    // Apply for leave
    // -------------------------------------------------------------------------

    @Transactional
    public LeaveRequest applyForLeave(Long id, LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        // Reject if any PENDING leave already exists for this employee
        List<LeaveRequest> pendingLeaves =
                leaveRequestRepository.findByEmployeeIdAndStatus(employee.getId(), LeaveStatus.PENDING);
        if (!pendingLeaves.isEmpty()) {
            throw new BusinessException(
                    "You already have a pending leave request. Please wait for it to be reviewed.");
        }

        // Validate dates
        LocalDate today = LocalDate.now();
        if (dto.getStartDate().isBefore(today)) {
            throw new BusinessException("Start date cannot be in the past.");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("End date must be on or after the start date.");
        }

        // Calculate number of days (inclusive)
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        // Check sufficient balance
        if (days > employee.getAvailableLeaves()) {
            throw new BusinessException(
                    "Insufficient leave balance. Requested: " + days
                            + " day(s), available: " + employee.getAvailableLeaves());
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .leaveType(dto.getLeaveType())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        return leaveRequestRepository.save(leaveRequest);
    }

    // -------------------------------------------------------------------------
    // Approve leave
    // -------------------------------------------------------------------------

    @Transactional
    public LeaveRequest approveLeave(Long requestId, Long managerId, String comments) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", requestId));

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee (manager)", "id", managerId));

        // Only MANAGER or ADMIN can approve
        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only a MANAGER or ADMIN can approve leave requests.");
        }

        // The approving manager must be the direct manager of the applicant
        Employee applicant = leaveRequest.getEmployee();
        if (!managerId.equals(applicant.getManagerId())) {
            throw new AccessDeniedException(
                    "You are not the direct manager of employee '" + applicant.getName() + "'.");
        }

        // Leave must still be PENDING
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(
                    "This leave request has already been " + leaveRequest.getStatus().name().toLowerCase() + ".");
        }

        // Deduct leave balance
        long days = calculateDays(leaveRequest);
        applicant.setAvailableLeaves(applicant.getAvailableLeaves() - days);
        employeeRepository.save(applicant);

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setRejectionReason(comments);  // store optional approval comments in same field
        leaveRequest.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    // -------------------------------------------------------------------------
    // Reject leave
    // -------------------------------------------------------------------------

    @Transactional
    public LeaveRequest rejectLeave(Long requestId, Long managerId, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", requestId));

        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee (manager)", "id", managerId));

        // Only MANAGER or ADMIN can reject
        if (manager.getRole() != Role.MANAGER && manager.getRole() != Role.ADMIN) {
            throw new AccessDeniedException(
                    "Only a MANAGER or ADMIN can reject leave requests.");
        }

        // The rejecting manager must be the direct manager of the applicant
        Employee applicant = leaveRequest.getEmployee();
        if (!managerId.equals(applicant.getManagerId())) {
            throw new AccessDeniedException(
                    "You are not the direct manager of employee '" + applicant.getName() + "'.");
        }

        // Leave must still be PENDING
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(
                    "This leave request has already been " + leaveRequest.getStatus().name().toLowerCase() + ".");
        }

        // Do NOT deduct balance on rejection
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason(rejectionReason);
        leaveRequest.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    // -------------------------------------------------------------------------
    // Cancel leave (by employee)
    // -------------------------------------------------------------------------

    @Transactional
    public LeaveRequest cancelLeave(Long requestId, Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", requestId));

        // Verify the request belongs to this employee
        if (!leaveRequest.getEmployee().getId().equals(id)) {
            throw new AccessDeniedException("You can only cancel your own leave requests.");
        }

        // Only PENDING leaves can be cancelled
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING leave requests can be cancelled. Current status: "
                            + leaveRequest.getStatus().name());
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setRejectionReason("Cancelled by employee.");
        leaveRequest.setReviewedAt(LocalDateTime.now());

        return leaveRequestRepository.save(leaveRequest);
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    public List<LeaveRequest> getEmployeeLeaves(Long id) {
        employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return leaveRequestRepository.findByEmployeeId(id);
    }

    public List<LeaveRequest> getPendingLeavesForManager(Long managerId) {
        employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee (manager)", "id", managerId));
        return leaveRequestRepository.findPendingForManager(managerId);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }

    public LeaveRequest getLeaveById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private long calculateDays(LeaveRequest leaveRequest) {
        return ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
    }
}

package com.tcs.elms.controller;

import com.tcs.elms.dto.ApiResponse;
import com.tcs.elms.dto.LeaveRequestDto;
import com.tcs.elms.entity.LeaveRequest;
import com.tcs.elms.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Management", description = "APIs for managing employee leave requests")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply/{id}")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<ApiResponse<LeaveRequest>> applyForLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequest leaveRequest = leaveService.applyForLeave(id, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave application submitted successfully", leaveRequest));
    }

    @GetMapping("/employee/{id}")
    @Operation(summary = "Get all leave requests for an employee")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getEmployeeLeaves(
            @PathVariable Long id) {
        List<LeaveRequest> leaves = leaveService.getEmployeeLeaves(id);
        return ResponseEntity.ok(ApiResponse.success("Leave requests fetched successfully", leaves));
    }

    @GetMapping("/pending/manager/{managerId}")
    @Operation(summary = "Get pending leave requests for a manager's team")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getPendingLeavesForManager(
            @PathVariable Long managerId) {
        List<LeaveRequest> leaves = leaveService.getPendingLeavesForManager(managerId);
        return ResponseEntity.ok(ApiResponse.success("Pending leaves fetched successfully", leaves));
    }

    @PutMapping("/{requestId}/approve/{managerId}")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeave(
            @PathVariable Long requestId,
            @PathVariable Long managerId,
            @RequestBody(required = false) String comments) {
        LeaveRequest leaveRequest = leaveService.approveLeave(requestId, managerId, comments);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", leaveRequest));
    }

    @PutMapping("/{requestId}/reject/{managerId}")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> rejectLeave(
            @PathVariable Long requestId,
            @PathVariable Long managerId,
            @RequestBody(required = false) String rejectionReason) {
        LeaveRequest leaveRequest = leaveService.rejectLeave(requestId, managerId, rejectionReason);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", leaveRequest));
    }

    @PutMapping("/{requestId}/cancel/{id}")
    @Operation(summary = "Cancel a pending leave request (by the employee)")
    public ResponseEntity<ApiResponse<LeaveRequest>> cancelLeave(
            @PathVariable Long requestId,
            @PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.cancelLeave(requestId, id);
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled successfully", leaveRequest));
    }

    @GetMapping
    @Operation(summary = "Get all leave requests (admin view)")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getAllLeaves() {
        List<LeaveRequest> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success("All leave requests fetched successfully", leaves));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single leave request by ID")
    public ResponseEntity<ApiResponse<LeaveRequest>> getLeaveById(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request fetched successfully", leaveRequest));
    }
}

package com.programtom.toma_velev_employees.model;

import java.time.LocalDate;

public record EmployeeData(int employeeId, int projectId, LocalDate start, LocalDate end) {
}

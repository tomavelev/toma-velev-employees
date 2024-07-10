package com.programtom.toma_velev_employees.rest;

import com.programtom.toma_velev_employees.model.EmployeePair;
import com.programtom.toma_velev_employees.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class EmployeeRestController {

    @Autowired
    EmployeeService employeeService;

    @PostMapping("/importCsv")
    public List<EmployeePair> parseEmployeesJson(@RequestParam(value = "file") MultipartFile file, @RequestParam(required = false) String dateFormat) throws IOException {

        if (dateFormat == null) {
            dateFormat = "yyyy-MM-dd";
        }
        File tempFile = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), "");
            file.transferTo(tempFile);
            List<EmployeePair> list = employeeService.mapToEmployeePairFromFile(tempFile, dateFormat);

            list.sort(
                    Comparator.comparingInt(EmployeePair::timeWorkedTogether).reversed()
            );

            return list;
        } finally {
            if (tempFile != null) {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        }
    }


    @PostMapping("/importCsvStr")
    public String parseEmployeesString(@RequestParam(value = "file") MultipartFile file, @RequestParam(required = false) String dateFormat) throws IOException {
        return parseEmployeesJson(file, dateFormat).stream().map(
                employeePair -> employeePair.employee1() + ", " + employeePair.employee2() + ", " + employeePair.timeWorkedTogether()
        ).collect(Collectors.joining("\n"));
    }
}

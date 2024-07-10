package com.programtom.toma_velev_employees.service;

import com.opencsv.CSVReader;
import com.programtom.toma_velev_employees.model.EmployeeData;
import com.programtom.toma_velev_employees.model.EmployeePair;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class EmployeeService {
    public List<EmployeePair> mapToEmployeePairFromFile(File file, String dateFormat) throws IOException {
        List<EmployeeData> temp = new ArrayList<>();
        try (Reader reader = new FileReader(file)) {
            try (CSVReader csvReader = new CSVReader(reader)) {
                csvReader.skip(1);

                try {
                    for (String[] line : csvReader) {
                        if (line.length == 4) {
                            temp.add(new EmployeeData(
                                    Integer.parseInt(line[0].trim()),
                                    Integer.parseInt(line[1].trim()),
                                    LocalDate.parse(line[2].trim(), DateTimeFormatter.ofPattern(dateFormat)),
                                    ("NULL".equals(line[3].trim())) ? LocalDate.now() :
                                            LocalDate.parse(line[3].trim(), DateTimeFormatter.ofPattern(dateFormat))
                            ));

                        } else {
                            throw new RuntimeException("Not full data row found");
                        }
                    }

                    return mapToEmployeePairFrom(temp);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Some of the employees had not integer ID");
                }
            }
        }
    }

    public List<EmployeePair> mapToEmployeePairFrom(List<EmployeeData> temp) {
        List<EmployeePair> pairs = new ArrayList<>();
//        143, 12, 2013-11-01, 2014-01-05
//        218, 10, 2012-05-16, NULL
//        143, 10, 2009-01-01, 2011-04-27
//        ...
//        Sample output:
//        143, 218, 8

//        projects
        //
        HashMap<Integer, List<EmployeeData>> projects = new HashMap<>();

        temp.forEach(
                employeeData -> {
                    List<EmployeeData> employeeData1 = projects.get(employeeData.projectId());
                    if (employeeData1 == null) {
                        employeeData1 = new ArrayList<>();
                        projects.put(employeeData.projectId(), employeeData1);
                    }
                    employeeData1.add(employeeData);
                }
        );

        HashMap<Pair<Integer, Integer>, Integer> workTogether = new HashMap<>();

        projects.forEach(
                (integer, employeeData) -> {
                    if (employeeData.size() > 1) {
                        for (int i = 0; i < employeeData.size(); i++) {
                            for (int j = i + 1; j < employeeData.size(); j++) {

                                EmployeeData employeeData1 = employeeData.get(i);
                                EmployeeData employeeData2 = employeeData.get(j);
//        1)case
//        1 --------s----e
//        2 --se------
//        2)case
//        1 --se------
//        2 --------s----e


//        3)case
//        1 --s--------e-----
//        2 --------s----e

//        4)case
//        1 --------s----e
//        2 --s--------e---


//        5)case ???
//        1 ------s----e
//        2 --s--------e

                                if (

                                        (employeeData1.start().isBefore(employeeData2.start()) &&
                                         employeeData1.end().isAfter(employeeData2.start()))
                                        ||

                                        (employeeData1.end().isAfter(employeeData2.start()) &&
                                         employeeData1.start().isBefore(employeeData2.end()))
                                ) {

                                    Pair<Integer, Integer> pair1 = Pair.of(employeeData1.employeeId(),
                                            employeeData2.employeeId());
                                    Pair<Integer, Integer> pair2 = Pair.of(employeeData2.employeeId(),
                                            employeeData1.employeeId());
                                    int calculatedCommonMonths = calculateCommonMonths(employeeData1.start(),
                                            employeeData1.end(), employeeData2.start(), employeeData2.end());
                                    if (workTogether.get(pair1) != null || workTogether.get(pair2) != null) {
                                        int previousValue =
                                                (workTogether.get(pair1) != null ? workTogether.get(pair1) : 0)
                                                +
                                                (workTogether.get(pair2) != null ? workTogether.get(pair2) : 0);

                                        previousValue += calculatedCommonMonths;


                                        workTogether.put(pair1, previousValue);

                                    } else {
                                        //create new key-value
                                        workTogether.put(pair1, calculatedCommonMonths);
                                    }

                                }

                            }

                        }
                    }


                }
        );

        workTogether.forEach((integerIntegerPair, timeWorkedTogether) -> {
            pairs.add(new EmployeePair(integerIntegerPair.getFirst(), integerIntegerPair.getSecond(), timeWorkedTogether));
        });

        return pairs;
    }

    public static int calculateCommonMonths(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
        // Determine the latest start date and earliest end date
        LocalDate commonStartDate = startDate1.isAfter(startDate2) ? startDate1 : startDate2;
        LocalDate commonEndDate = endDate1.isBefore(endDate2) ? endDate1 : endDate2;

        // If the common date range is valid, calculate the number of months
        if (!commonStartDate.isAfter(commonEndDate)) {
            Period period = Period.between(commonStartDate, commonEndDate);
            return period.getYears() * 12 + period.getMonths() + 1;  // +1 to include the starting month
        } else {
            return 0; // No overlap
        }
    }
}

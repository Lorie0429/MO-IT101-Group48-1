/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.motorph_group48;

/**
 *
 * @author lance
 */
import java.io.*;
import java.util.Scanner;

public class MotorPH_Group48 {

    public static void main(String[] args) {
        // File names of the CSV files we will read
        String empFile = "MotorPH_Employee Data - Tucker, L. - Employee Details.csv";
        String attFile = "MotorPH_Employee Data - Tucker, L. - Attendance Record.csv";

        // Scanner for user input
        Scanner sc = new Scanner(System.in);
        
        // Ask the user to enter the employee number
        System.out.print("Enter Employee Number: ");
        String targetID = sc.nextLine().trim();

        try {
            // 1. Get Employee Details
            
            // BufferedReader is used to read the CSV file line by line
            BufferedReader br = new BufferedReader(new FileReader(empFile));
            
            // Read the first line (header) and skip it
            String line = br.readLine();
            
            // Variables to store employee information
            String fName = "", lName = "", bday = "";
            double hourlyRate = 0;
            
            // This variable will check if the employee was found
            boolean found = false;

            // Loop through the file until the end
            while ((line = br.readLine()) != null) {
                
                
                // Split the line into columns
                // This split handles commas inside quotation marks
                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // Column 0 contains the employee number
                // Remove quotes before comparing
                if (cols[0].replace("\"", "").equals(targetID)) {
                    
                    // Get employee information from the CSV columns
                    lName = cols[1].replace("\"", "");
                    fName = cols[2].replace("\"", "");
                    bday = cols[3].replace("\"", "");
                    
                    // Hourly rate is located in column index 18
                    // Remove quotes and commas before converting to double
                    hourlyRate = Double.parseDouble(cols[18].replace("\"", "").replace(",", ""));
                    
                    // Mark that the employee has been found
                    found = true;
                    break;
                }
            }
            
            // Close the employee file
            br.close();

            // If employee was not found, stop the program
            if (!found) {
                System.out.println("Employee not found.");
                return;
            }

            // Display Employee Information
            System.out.println("\nEmployee: " + fName + " " + lName);
            System.out.println("Birthday: " + bday);
            System.out.println("Hourly Rate: " + hourlyRate);

            // 2. Process Attendance (JUNE - DECEMBER)
            
            // Open attendance CSV file
            BufferedReader attBr = new BufferedReader(new FileReader(attFile));
            attBr.readLine(); 
            
            // Variable to store total hours worked
            double totalHours = 0;
            
            // Array to store gross salary per month
            // index 1-12 represents January to December
            double[] monthlyGross = new double[13];

            System.out.println("\n--- Attendance Logs ---");
            
            // Read attendance records line by line
            while ((line = attBr.readLine()) != null) {
                
                // Split columns
                String[] cols = line.split(",");
                
                // Column 0 contains employee number
                if (cols[0].equals(targetID)) {
                    
                    // Column 3 contains the date
                    String date = cols[3];
                    
                    // Extract the month from the date
                    int month = Integer.parseInt(date.split("/")[0]);

                    // Only process records from June to December
                    if (month >= 6 && month <= 12) {
                        
                        // Calculate hours worked for that day
                        double dayHours = calculateHours(cols[4], cols[5]);
                        
                        // Add to total hours
                        totalHours += dayHours;
                        
                        // Add the daily salary to the monthly gross
                        monthlyGross[month] += dayHours * hourlyRate;
                        System.out.println(date + " | Hours: " + dayHours);
                    }
                }
            }
            
            // Close attendance file
            attBr.close();

            // 3. Calculate Totals and Deductions
            
            // Total gross salary
            double totalGross = totalHours * hourlyRate;
            
            // Total deductions
            double totalDeductions = 0;

            System.out.println("\n--- Monthly Deductions ---");
            
            // Loop through months June to December
            for (int i = 6; i <= 12; i++) {
                
                // If the employee worked that month
                if (monthlyGross[i] > 0) {
                    
                    // Government deductions
                    
                    // Get SSS contribution based on salary
                    double sss = getSSS(monthlyGross[i]);
                    
                    // PhilHealth is simplified to 2%
                    double ph = monthlyGross[i] * 0.02;
                    
                    // Pag-IBIG fixed value
                    double pi = 100.0;
                    
                    // Simplified tax computation
                    double tax = (monthlyGross[i] - (sss + ph + pi)) * 0.15; 
                    
                    // Total deductions for that month
                    double mDed = sss + ph + pi + tax;
                    
                    // Add to total deductions
                    totalDeductions += mDed;
                    
                    // Display monthly result
                    System.out.println("Month " + i + " Gross: " + monthlyGross[i] + " | Ded: " + mDed);
                }
            }

            // FINAL SUMMARY
  
            System.out.println("\n--- FINAL SUMMARY ---");
            
            // Display totals
            System.out.println("Total Hours: " + totalHours);
            System.out.println("Total Gross: " + totalGross);
            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("NET PAY: " + (totalGross - totalDeductions));

        } catch (Exception e) {
            
            // If an error happens while reading the files
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Method: Calculate Hours Worked
    
    // This method computes hours worked based on login and logout time
    public static double calculateHours(String in, String out) {
        
        // Split time into hour and minute
        String[] tIn = in.split(":");
        String[] tOut = out.split(":");
        
        int hIn = Integer.parseInt(tIn[0]);
        int mIn = Integer.parseInt(tIn[1]);
        int hOut = Integer.parseInt(tOut[0]);
        int mOut = Integer.parseInt(tOut[1]);

        // Teacher rule: 10-minute grace period
        // If employee logs in between 8:00 and 8:10 it is still considered 8:00
        if (hIn == 8 && mIn <= 10) mIn = 0;
        
        // Only count time starting 8:00 AM
        if (hIn < 8) { hIn = 8; mIn = 0; }
        
        // Only count until 5:00 PM
        if (hOut >= 17) { hOut = 17; mOut = 0; }

        // Convert time to decimal format
        double start = hIn + (mIn / 60.0);
        double end = hOut + (mOut / 60.0);
        
        // Subtract 1 hour lunch break
        double total = (end - start) - 1.0; 
        
        // Prevent negative hours
        return total > 0 ? total : 0;
    }

    // Method: Get SSS Contribution
    
    // Simple SSS table based on salary range
    public static double getSSS(double gross) {
        if (gross < 10000) return 450;
        if (gross < 20000) return 900;
        return 1125;
    }
}

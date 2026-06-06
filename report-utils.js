/**
 * Utility functions for exporting and downloading report data.
 * This module uses the 'jspdf' library to generate a printer-friendly PDF of the monthly summary report.
 */

/**
 * Generates an elegant, printer-friendly PDF of the monthly summary report and triggers a download or writes to disk,
 * depending on whether this is executing in a browser environment or on node.
 * 
 * @param {Object} reportData Object containing the monthly summary data.
 * @param {string} reportData.monthName Name of the month (e.g. "June").
 * @param {number} reportData.year Year of the report (e.g. 2026).
 * @param {number} reportData.totalIncome Sum of all collections/income.
 * @param {number} reportData.totalExpenses Sum of all utility & bazaar expenses.
 * @param {number} reportData.netBalance Calculated surplus or deficit of the period.
 * @param {string} [outputPath] Local file path for node environments. Defaults to 'monthly_summary_report.pdf'.
 * @returns {Promise<any>} Resolves when the PDF generation is successful.
 */
async function generateMonthlyReportPDF(reportData, outputPath = 'monthly_summary_report.pdf') {
    const { monthName, year, totalIncome, totalExpenses, netBalance } = reportData;
    let jsPDF;

    try {
        // Dynamic requiring/importing jsPDF to support both front-end (browser) and back-end environments smoothly.
        if (typeof window !== 'undefined' && window.jspdf) {
            jsPDF = window.jspdf.jsPDF;
        } else {
            // Require standard Node module if running on terminal
            const jspdfModule = require('jspdf');
            jsPDF = jspdfModule.jsPDF;
        }
    } catch (e) {
        console.warn("jsPDF is not fully installed or available in this context. Emulating PDF generation output directly instead.");
    }

    if (!jsPDF) {
        // Fallback placeholder/printer simulator log if the library cannot be found in the current environment
        const fallbackText = `
========================================
       PRINT-READY PDF GENERATOR
========================================
Monthly Summary Report: ${monthName} ${year}
----------------------------------------
- Total Member Income:    ৳ ${totalIncome.toLocaleString()}
- Total Mess Expenses:   ৳ ${totalExpenses.toLocaleString()}
- Combined Net Balance:  ৳ ${netBalance.toLocaleString()} (${netBalance >= 0 ? "Surplus" : "Deficit"})
========================================
`;
        console.log("Simulating PDF download:", fallbackText);
        return fallbackText;
    }

    // Creating document
    const doc = new jsPDF({
        orientation: 'p',
        unit: 'mm',
        format: 'a4'
    });

    // Outer framing and style accents
    doc.setDrawColor(15, 23, 42); // slate-900 primary tone
    doc.setLineWidth(1);
    doc.rect(10, 10, 190, 277); // Outer frame border

    // Header banner segment
    doc.setFillColor(15, 23, 42);
    doc.rect(10, 10, 190, 30, 'F');

    // Header Text
    doc.setFont("helvetica", "bold");
    doc.setFontSize(18);
    doc.setTextColor(255, 255, 255);
    doc.text("SMART MESS BD - MONTHLY FINANCIAL SUMMARY", 20, 28);

    doc.setFontSize(10);
    doc.setFont("helvetica", "normal");
    doc.text(`Generated: ${new Date().toLocaleString()}`, 145, 34);

    // Context metadata
    doc.setTextColor(15, 23, 42);
    doc.setFontSize(12);
    doc.setFont("helvetica", "bold");
    doc.text(`Financial Period: ${monthName} ${year}`, 20, 60);

    // Section title divider line
    doc.setDrawColor(203, 213, 225); // light slate border
    doc.setLineWidth(0.5);
    doc.line(20, 65, 190, 65);

    // Table labels
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.text("Description", 25, 80);
    doc.text("Calculation (BDT)", 150, 80);
    doc.line(20, 84, 190, 84);

    // Row 1: Income
    doc.setFont("helvetica", "normal");
    doc.text("Total Monthly Income / Member Collections", 25, 95);
    doc.text(`+ ৳ ${totalIncome.toFixed(2)}`, 150, 95);
    doc.line(20, 99, 190, 99);

    // Row 2: Expenses
    doc.text("Total Monthly Expenses (Bazaar & Utility Bills)", 25, 110);
    doc.text(`- ৳ ${totalExpenses.toFixed(2)}`, 150, 110);
    doc.setLineWidth(0.8);
    doc.line(20, 114, 190, 114);

    // Total balance row
    doc.setFont("helvetica", "bold");
    doc.setFontSize(12);
    doc.text("Net Period Balance", 25, 126);
    
    const balanceSign = netBalance >= 0 ? "+" : "";
    doc.text(`${balanceSign} ৳ ${netBalance.toFixed(2)}`, 150, 126);

    // Indicator highlight context box
    const balanceColor = netBalance >= 0 ? [6, 95, 70] : [153, 27, 27]; // green or red
    doc.setFillColor(balanceColor[0], balanceColor[1], balanceColor[2]);
    doc.rect(25, 134, 45, 10, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(10);
    doc.text(netBalance >= 0 ? "SURPLUS PERIOD" : "DEFICIT PERIOD", 29, 140.5);

    // Footer section instructions
    doc.setTextColor(100, 116, 139); // slate-500 neutral text
    doc.setFont("helvetica", "italic");
    doc.setFontSize(9);
    doc.text("This report was generated securely directly from Smart Mess BD database modules.", 20, 260);
    doc.text("Please store copies safely and verify with your designated mess committee manager/admin.", 20, 265);

    // Check environment context before saving file path vs triggering system download
    if (typeof window !== 'undefined' && typeof window.document !== 'undefined') {
        doc.save(outputPath);
        console.log(`Successfully completed and downloaded PDF file: "${outputPath}"`);
    } else {
        // Node FS storage
        try {
            const fs = require('fs');
            const buffer = Buffer.from(doc.output('arraybuffer'));
            fs.writeFileSync(outputPath, buffer);
            console.log(`Successfully completed and written local system PDF binary: "${outputPath}"`);
        } catch (nodeError) {
            console.error("Could not write node file system binary output directly:", nodeError.message);
        }
    }

    return doc;
}

module.exports = {
    generateMonthlyReportPDF
};

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.sql.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Add this annotation to handle file uploads
@MultipartConfig
public class PlotServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/png");

        // Get form parameters
        String xCoords = request.getParameter("x");
        String yCoords = request.getParameter("y");
        String chartType = request.getParameter("chartType");

        // Get the uploaded Excel file (if any)
//        Part filePart = request.getPart("excelFile");

        // Get user_id from session
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            response.sendRedirect("login.html");
            return;
        }

        double[] xValues = null;
        double[] yValues = null;

        // If an Excel file is uploaded, extract data from it
//        if (filePart != null && filePart.getSize() > 0) {
//            try (InputStream is = filePart.getInputStream()) {
//                Workbook workbook = new XSSFWorkbook(is);
//                Sheet sheet = workbook.getSheetAt(0); // Assume data is in the first sheet
//                int rows = sheet.getPhysicalNumberOfRows();
//                xValues = new double[rows];
//                yValues = new double[rows];
//
//                for (int i = 0; i < rows; i++) {
//                    Row row = sheet.getRow(i);
//                    xValues[i] = row.getCell(0).getNumericCellValue();
//                    yValues[i] = row.getCell(1).getNumericCellValue();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error reading Excel file.");
//                return;
//            }
//        } else 
        if (xCoords != null && yCoords != null) {
            // If no Excel file is uploaded, use the text inputs
            String[] xStrings = xCoords.split(",");
            String[] yStrings = yCoords.split(",");
            xValues = new double[xStrings.length];
            yValues = new double[yStrings.length];

            for (int i = 0; i < xStrings.length; i++) {
                xValues[i] = Double.parseDouble(xStrings[i].trim());
                yValues[i] = Double.parseDouble(yStrings[i].trim());
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No data provided.");
            return;
        }

        // Generate the chart based on the selected type
        JFreeChart chart = null;

        switch (chartType) {
            case "line":
                chart = createLineChart(xValues, yValues);
                break;
            case "scatter":
                chart = createScatterPlot(xValues, yValues);
                break;
            case "bar":
                chart = createBarChart(xValues, yValues);
                break;
            case "pie":
                chart = createPieChart(xValues, yValues);
                break;
            case "histogram":
                chart = createHistogram(xValues);
                break;
            case "area":
                chart = createAreaChart(xValues, yValues);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid chart type");
                return;
        }

        // Output the chart as a PNG image
        BufferedImage chartImage = chart.createBufferedImage(800, 600);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(chartImage, "png", baos);
        byte[] chartImageBytes = baos.toByteArray();

        // Store the data in the database
        storeChartData(userId, xCoords, yCoords, chartType, chartImageBytes);

        // Send the image as a response
        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        out.write(chartImageBytes);
        out.close();
    }

    // Method to create a Line Chart
    private JFreeChart createLineChart(double[] xValues, double[] yValues) {
        XYSeries series = new XYSeries("Data");
        for (int i = 0; i < xValues.length; i++) {
            series.add(xValues[i], yValues[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return ChartFactory.createXYLineChart(
            "Line Chart", "X", "Y",
            dataset, PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    // Method to create a Scatter Plot
    private JFreeChart createScatterPlot(double[] xValues, double[] yValues) {
        XYSeries series = new XYSeries("Data");
        for (int i = 0; i < xValues.length; i++) {
            series.add(xValues[i], yValues[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return ChartFactory.createScatterPlot(
            "Scatter Plot", "X", "Y",
            dataset, PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    // Method to create a Bar Chart
    private JFreeChart createBarChart(double[] xValues, double[] yValues) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < xValues.length; i++) {
            dataset.addValue(yValues[i], "Value", Double.toString(xValues[i]));
        }

        return ChartFactory.createBarChart(
            "Bar Chart", "X", "Y",
            dataset, PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    // Method to create a Pie Chart
    private JFreeChart createPieChart(double[] xValues, double[] yValues) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < xValues.length; i++) {
            dataset.setValue(Double.toString(xValues[i]), yValues[i]);
        }

        return ChartFactory.createPieChart(
            "Pie Chart", dataset,
            true, true, false
        );
    }

    // Method to create a Histogram
    private JFreeChart createHistogram(double[] values) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Histogram", values, 10);

        return ChartFactory.createHistogram(
            "Histogram", "Value", "Frequency",
            dataset, PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    // Method to create an Area Chart
    private JFreeChart createAreaChart(double[] xValues, double[] yValues) {
        XYSeries series = new XYSeries("Data");
        for (int i = 0; i < xValues.length; i++) {
            series.add(xValues[i], yValues[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return ChartFactory.createXYAreaChart(
            "Area Chart", "X", "Y",
            dataset, PlotOrientation.VERTICAL,
            true, true, false
        );
    }

    // Method to store chart data in the database
    private void storeChartData(int userId, String xCoords, String yCoords, String chartType, byte[] chartImageBytes) {
        String dbUrl = "jdbc:mysql://localhost:3306/your_database";  // Replace with your DB URL
        String dbUser = "your_username";  // Replace with your DB username
        String dbPassword = "your_password";  // Replace with your DB password

        String sql = "INSERT INTO charts (user_id, x_coords, y_coords, chart_type, chart_image) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, xCoords);
            pstmt.setString(3, yCoords);
            pstmt.setString(4, chartType);
            pstmt.setBytes(5, chartImageBytes);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

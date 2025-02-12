package MpesaStatementExtractor;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class Main {
	public static void main(String[] args) throws SQLException {

		int minheight = 317;
		try {
			PDDocument document = Loader.loadPDF(new File("/home/iman/Downloads/porjects resources/unprotected.pdf"));
			ObjectExtractor oe = new ObjectExtractor(document);

			for (int i = 1; i <= document.getNumberOfPages(); i++) {
				Page page = oe.extract(i);

				SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
				List<Table> tables = sea.extract(page);

				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Mpesa", "root",
						"lesterxCgR570#");

//				String sql = "INSERT INTO transactions ( receipt_no ,completion_time ,details ,t"
//						+ "ransaction_status ,paid_in ,withdrawn ," + "balance ) VALUES (?, ?, ?, ? , ?, ?, ?)";
//
//				

				for (Table table : tables) {

					if (table.getHeight() >= minheight) {
						List<List<RectangularTextContainer>> rows = table.getRows();

						for (List<RectangularTextContainer> row : rows) {
							// Skip header row if needed
							if (rows.indexOf(row) == 0)
								continue;

							StringBuilder sql = new StringBuilder(
									"INSERT INTO transactions (receipt_no,completion_time,");
							StringBuilder values = new StringBuilder("VALUES (?,?,");

							List<Object> parameters = new ArrayList<>();
							Map<String, Object> dynamicFields = new HashMap<>();

							// Get text from each cell
							for (int j = 0; j < row.size() - 1; j++) {
								switch (j) {
								case (0):
									parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", ""));
									break;
								case (1):
									parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", ""));
									break;
								case (2):
									if (row.get(j).getText().trim() != "") {
										sql.append("details,");
										values.append("?,");
										parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", "")
												.replaceAll("[\\s\\p{Z}\\p{C}]+", "").replaceAll("\\t", " ")
												.replaceAll("\\s+", " "));
									}

									break;

								case (3):
									sql.append("transaction_status,");
									values.append("?,");
									parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", ""));

									break;
								case (4):
									if (row.get(j).getText().trim() != "") {
										sql.append("paid_in,");
										values.append("?,");
										parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", "")
												.replace(",", ""));

									}
									break;
								case (5):
									if (row.get(j).getText().trim() != "") {
										sql.append("withdrawn,");
										values.append("?,");
										parameters.add(row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", "")
												.replace(",", ""));

									}
									break;
								case (6):
									sql.append("balance) ");
									values.append("?)");
									parameters.add(
											row.get(j).getText().trim().replaceAll("\\r?\\n|\\r", "").replace(",", ""));

									break;
								}

							}
							sql.append(values.toString());
							System.out.println();
							System.out.println(parameters);
							PreparedStatement pstmt = conn.prepareStatement(sql.toString());
							for (int l = 0; l < parameters.size(); l++) {
								pstmt.setObject(l + 1, parameters.get(l));
								System.out.println(l + 1);

							}
							pstmt.execute();
							pstmt.close();

						}

					}

				}
				conn.close();

			}

			oe.close();
			document.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

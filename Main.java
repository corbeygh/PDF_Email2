import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPages;
import com.lowagie.text.pdf.PdfReader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.mail.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * esc100 interface
 * <p>
 * ability to enter new grower/florist
 * <p>
 * change their status from outer towner
 * <p>
 * save data in csv file
 * <p>
 * send emails using:
 * https://cloud.google.com/appengine/docs/standard/java/mail/sending-mail-with-mail-api
 */

public class Main {

    public static final String pdfFileLocation = "E:\\Intellij\\PDF_Email\\src\\main\\resources\\" + "asdf.pdf";


    public static void main(String[] args) throws IOException {
        //Two Problems to overcome:
        //Find which growers have more than 1 Page.
        //Also Name the file as the grower/florist code.

        System.out.println("Breaking Large PDF into smaller files for each person...");
//        System.out.println(Collections.singletonList(getPDFSaveNames()));
        separatePDFIntoCodeFiles();
        System.out.println("Done.");
    }

    public static void sendEmails() {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "your server here");
        Session session = Session.getDefaultInstance(props, null);


    }

    public static List<String> getPDFSaveNames() throws IOException {
        //Loading an existing document
        File file = new File(pdfFileLocation);
        PDDocument document = PDDocument.load(file);

        //Instantiate PDFTextStripper class
        PDFTextStripper pdfStripper = new PDFTextStripper();

        //Retrieving text from PDF document
        String text = pdfStripper.getText(document);

        List<String> codeToPDFPageNumber = new ArrayList<String>();

        try {
            Scanner sc = new Scanner(text);
            String recentCode = null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.contains("Code")) {
                    String[] splitWords = line.split("Code");
                    recentCode = splitWords[0];
                }

                if (line.contains("Page")) {
                    codeToPDFPageNumber.add(recentCode);
                }

            }

        } catch (Exception e) {
            System.out.println(e);
        }

        //Closing the document
        document.close();

        return codeToPDFPageNumber;
    }

    public static void separatePDFIntoCodeFiles() throws IOException {
        List<String> saveName = getPDFSaveNames();

        try {
            String inFile = pdfFileLocation;
            System.out.println("Reading " + inFile);
            PdfReader reader = new PdfReader(inFile);
            int n = reader.getNumberOfPages();
            System.out.println("Number of pages : " + n);
            int i = 0;
            while (i < n && saveName.get(i) != null) {


                String directoryOfFile = inFile.substring(0, inFile.indexOf(".pdf")) + "-";
                String codeName = saveName.get(i) + ".pdf";
                String outFile = directoryOfFile + codeName;
                System.out.println("Writing " + outFile);

                //----------------------------
                int pagesToWrite = 1;
                if (i + 1 < saveName.size() && saveName.get(i + 1) != null && saveName.get(i + 1).equals(saveName.get(i))) {
                    //Next Page belows to the same person.
                    int count = 1;
                    while (i + count < saveName.size() && saveName.get(count + i).equals(saveName.get(i))) {
                        pagesToWrite++;
                        count++;
                    }
                }

                if (pagesToWrite > 1) {
                    Document document = new Document(reader.getPageSizeWithRotation(1));
                    PdfCopy writer = new PdfCopy(document, new FileOutputStream(outFile));


                    document.open();
                    while (pagesToWrite > 0) {
                        PdfImportedPage page = writer.getImportedPage(reader, ++i);
                        writer.addPage(page);


                        pagesToWrite--;
                    }
                    document.close();
                    writer.close();

                } else {
                    Document document = new Document(reader.getPageSizeWithRotation(1));
                    PdfCopy writer = new PdfCopy(document, new FileOutputStream(outFile));
                    document.open();
                    PdfImportedPage page = writer.getImportedPage(reader, ++i);
                    writer.addPage(page);
                    document.close();
                    writer.close();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


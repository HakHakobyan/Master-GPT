import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;

public class SpyurScraperAPI {
    public static void main(String[] args) throws IOException, CsvException {

        String[] headers = {"ID", "Company Name", "Executive", "Phone Number", "Address", "Website", "Activity types by Spyur's classifier",
                "Number of Employees", "Form of Ownership", "Year Established"};

        try (FileWriter fw = new FileWriter("companies.csv");
             CSVWriter writer = new CSVWriter(fw)) {
            writer.writeNext(headers);

            String companyName = "NA";
            String executive = "NA";
            String companyPhone = "NA";
            String companyAddress = "NA";
            String site = "NA";
            String activities = "NA";
            String noe = "NA";
            String foo = "NA";
            String year = "NA";

            for (int i = 1; i <= 100; i++) {

                //We can scrape data of all companies registered in spyur.am setting i<=100000, but we won't do that.

                Document doc = Jsoup.connect("https://www.spyur.am/en/companies/" + i)
                        .get();
                String error = doc.select("div.content h1.page_title").text();

                if (!error.equals("ERROR!")) {
                    String ID = Integer.toString(i);
                    companyName = doc.select("h1.page_title").first().text();
                    executive = doc.select("div.main_info_block div.text_block li.text_block").text();
                    companyPhone = doc.select("a.call").text();
                    Elements divs = doc.select("div.branch_contacts").not("div.branch_contact_child");
                    String ca = divs.text();
                    String catail = doc.select("div.branch_contact_child").text();
                    companyAddress = ca.replace(catail, "");
                    site = doc.select("div[class='col-12 col-lg-6'] div.contacts_list ul[class='communications text_block'] li div div a")
                            .eq(2).attr("href");
                    activities = doc.select("div.info_inner_block li.sahd").eq(1).select("a.sub3").text();
                    String felem = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(0).text();
                    switch (felem) {
                        case "Number of employees":
                            noe = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            foo = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(3).text();
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(5).text();
                            break;
                        case "Form of ownership":
                            noe = "NA";
                            foo = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(3).text();
                            break;
                        case "Year established":
                            noe = "NA";
                            foo = "NA";
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            break;
                        default:
                            noe = "NA";
                            foo = "NA";
                            year = "NA";
                            break;
                    }
                    String[] data = {ID, companyName, executive, companyPhone, companyAddress, site, activities, noe, foo, year};
                    writer.writeNext(data);
                }
                else{continue;}
                }
            }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
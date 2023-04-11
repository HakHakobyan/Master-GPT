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
                    //System.out.println(ID);
                    companyName = doc.select("h1.page_title").first().text();
                    //System.out.println(companyName);
                    executive = doc.select("div.main_info_block div.text_block li.text_block").text();
                    //System.out.println(executive);
                    companyPhone = doc.select("a.call").text();
                    //System.out.println("Phone number:" + companyPhone);
                    Elements divs = doc.select("div.branch_contacts").not("div.branch_contact_child");
                    String ca = divs.text();
                    String catail = doc.select("div.branch_contact_child").text();
                    companyAddress = ca.replace(catail, "");
                    //System.out.println("Company Address: " + companyAddress);
                    site = doc.select("div[class='col-12 col-lg-6'] div.contacts_list ul[class='communications text_block'] li div div a")
                            .eq(2).attr("href");
                    //System.out.println("Company Website:" + Site);
                    activities = doc.select("div.info_inner_block li.sahd").eq(1).select("a.sub3").text();
                    //System.out.println("Activity types by Spyur's classifier:" + activities);

                    String felem = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(0).text();
                    switch (felem) {
                        case "Number of employees":
                            noe = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            //System.out.println("Number of employees:" + noe);
                            foo = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(3).text();
                            //System.out.println("Form of ownership:" + foo);
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(5).text();
                            //System.out.println("Year established:" + year);
                            break;
                        case "Form of ownership":
                            noe = "NA";
                            //System.out.println("Number of employees: NA");
                            foo = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            //System.out.println("Form of ownership:" + foo);
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(3).text();
                            //System.out.println("Year established:" + year);
                            break;
                        case "Year established":
                            noe = "NA";
                            foo = "NA";
                            year = doc.select("div.add_info_inner div.info_inner_block ul[class='otherInfo info_listing'] li").eq(1).text();
                            //System.out.println("Number of employees:" + noe);
                            //System.out.println("Form of ownership:" + foo);
                            //System.out.println("Year established:" + year);
                            break;
                        default:
                            noe = "NA";
                            //System.out.println("Number of employees: NA");
                            foo = "NA";
                            //System.out.println("Form of ownership:" + foo);
                            year = "NA";
                            //System.out.println("Year established:" + year);
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
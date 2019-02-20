package org.bitnick.web.distrograb.scraper;

import org.bitnick.web.distrograb.entities.Distro;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.postgresql.util.PSQLException;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.lang.NullPointerException;

public class WebScraper {
    private WebDriver driver = null;

    private WebScraper() {
        //System.setProperty("phantomjs.binary.path", "E:/programs/phantomjs-2.1.1-windows/bin/phantomjs.exe");
        System.setProperty("webdriver.chrome.driver","/home/ghost/IdeaProjects/distrograb/src/main/resources/chromedriver");

        //FirefoxOptions options = new FirefoxOptions();
        //options.setBinary("C:/Program Files (x86)/Mozilla Firefox/firefox.exe");
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/google-chrome-stable");
        options.addArguments("headless");
        options.addArguments("window-size=1200x600");

        this.driver = new ChromeDriver(options);
    }

    public Boolean getDistroList() throws Exception {
        Connection conn = null;

        String databaseServerIP = "192.168.1.100";
        String databaseServerPort = "5432";
        String databaseName = "distrobunny";
        String username = "postgres";
        String password = "haze9856";

        try {
            List<Distro> linuxDistros = new ArrayList<Distro>();

            // Dynamically load our database driver at runtime.
            Class.forName("org.postgresql.Driver");

            conn = DriverManager
                    .getConnection(
                            "jdbc:postgresql://" + databaseServerIP + ":" + databaseServerPort + "/" + databaseName,
                            username,
                            password
                    );
            conn.setAutoCommit(false);


            driver.get("https://distrowatch.com/search.php?ostype=All&category=All&origin=All&basedon=All&notbasedon=None&desktop=All&architecture=x86_64&package=All&rolling=All&isosize=All&netinstall=All&language=All&defaultinit=All&status=Active#simple");

            List<WebElement> elements = new WebDriverWait(driver, 10).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.xpath("/html/body/table[3]/tbody/tr/td[1]/table/tbody/tr[3]/td/table/tbody/tr[2]/td/b[2]/a")
                    )
            ).findElements(By.xpath("//b/a"));
            System.out.println("[*] -- Found " + elements.size() + " Urls --");
            System.out.println("[*] -- Pausing --");
            Thread.sleep(1000);

            for (WebElement element : elements) {
                if (!element.getAttribute("href").equals("https://distrowatch.com/dwres.php?resource=popularity")) {
                    Distro distro = new Distro();
                    distro.setDistroName(element.getText());
                    distro.setDistroMainUrl(element.getAttribute("href"));

                    linuxDistros.add(distro);
                }
            }

            for (Distro distro : linuxDistros) {
                try {
                    //
                    driver.navigate().to(distro.getDistroMainUrl());

                    System.out.println("[*] Visiting -- " + distro.getDistroMainUrl());

                    try {
                        WebElement newElement = driver.findElement(By.xpath("//tr[10]/td/a"));

                        distro.setDistroDownloadUrl(newElement.getAttribute("href"));
                        System.out.println("[*] -- Colleted data --");
                    }

                    catch (Exception ex) {
                        WebElement newElement = driver.findElement(By.xpath("//tr[10]/td/a[0]"));

                        distro.setDistroDownloadUrl(newElement.getAttribute("href"));
                        System.out.println("[*] -- Colleted data --");
                        break;
                    }

                    if (!distro.getDistroDownloadUrl().equals("")) {
                        PreparedStatement preparedStatement = conn.prepareStatement(
                                "INSERT INTO \"distros\"(\"distro_name\") " +
                                        "	VALUES (?); " +
                                        "INSERT INTO \"urls\"(\"distro_main_url\", \"distro_download_url\") " +
                                        "	VALUES (?, ?); " +
                                        "INSERT INTO \"distro_url\"(\"distro_id\", \"url_id\") " +
                                        "	VALUES((SELECT DISTINCT" +
                                        "				\"id\" " +
                                        "			FROM " +
                                        "				\"distros\" " +
                                        "			WHERE " +
                                        "				\"distro_name\" = ? ) , ( " +
                                        "			SELECT DISTINCT" +
                                        "				\"id\" " +
                                        "			FROM " +
                                        "				\"urls\" " +
                                        "			WHERE " +

                                        "				\"distro_main_url\" = ? )); "
                        );

                        preparedStatement.setString(1, distro.getDistroName());
                        preparedStatement.setString(2, distro.getDistroMainUrl());
                        preparedStatement.setString(3, distro.getDistroDownloadUrl());
                        preparedStatement.setString(4, distro.getDistroName());
                        preparedStatement.setString(5, distro.getDistroMainUrl());

                        preparedStatement.execute();
                    } else {
                        throw new NullPointerException();
                    }

                    System.out.println("[*] -- Stored data --");

                    conn.commit();

                    driver.navigate().back();

                    Thread.sleep(1000);
                }

                catch(PSQLException ex) {
                    System.out.println("[*] Error -- Already saved in database, moving on...");
                    //ex.printStackTrace();
                    continue;
                }

                catch (Exception ex) {
                    ex.printStackTrace();
                    break;
                    //continue;
                }

            }

			/*for (Distro linuxDistro : linuxDistros) {
				PreparedStatement preparedStatement = conn.prepareStatement(
						"INSERT INTO \"Distros\"(\"DistroName\") " +
						"	VALUES (?) RETURN \"PK_DistroID\" AS last_distro_id; " +
						"INSERT INTO \"Urls\"(\"DistroMainUrl\", \"DistroDownloadUrl\") " +
						"	VALUES (?, ?) RETURN \"PK_UrlID\" AS last_url_id; " +
						"INSERT INTO \"ArchTypes\"(\"ArchTypeName\") " +
						"	VALUES (?) RETURN \"PK_ArchTypeID\" AS last_archtype_id; " +
						"INSERT INTO \"ArchTypeDistro\"(\"FK_DistroID\", \"FK_ArchTypeID\") " +
						"	VALUES (last_distro_id, last_archtype_id); " +
						"INSERT INTO \"DistroUrl\"(\"FK_DistroID\", \"FK_UrlID\")" +
						"	VALUES (last_distro_id, last_url_id); "
						);

				preparedStatement.setString(1, linuxDistro.distroName);
				preparedStatement.setString(2, linuxDistro.distroUrl);
				preparedStatement.setString(3, linuxDistro.distroDownloadUrl.get(0));
				preparedStatement.setString(4, "x86_64");

				System.out.println(linuxDistro.distroName);
				System.out.println(linuxDistro.distroUrl);
				System.out.println(linuxDistro.distroDownloadUrl);
				System.out.println("\tStored!\n");


				conn.commit();
			}*/

            //conn.close();
            //driver.close();

            return true;
        }

        catch (Exception ex) {
            ex.printStackTrace();

            //driver.close();
            return false;
        }

        finally {
            conn.close();
            //driver.close();
            driver.quit();
        }
    }

    public static WebScraper instanceOf() {
        return new WebScraper();
    }
}

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.io.IOException;
import java.util.Scanner;


public class Main {
    private static OrientDB orientdb;
    private static Scanner scanner;
    private static ODatabaseSession db;

    public static void main(String[] args) throws IOException {
        String dbName = "courses";
        //polaczenie z serwerem (nie z baza)
        orientdb = new OrientDB("remote:localhost", OrientDBConfig.defaultConfig());

        //utworzenie sesji bazy "courses" jako admin (domyslnie jest reader, writer i wlasnie admin)
        db = orientdb.open(dbName, "admin", "admin");

        //tworzenie schematu/tabeli/składu
        OClass course = db.getClass(("Course"));
        if (course == null)
            db.createVertexClass("Course");
        //ustawianie parametrow
        setParameter("destination", course, OType.STRING, "Course_destination_index");
        setParameter("departure", course, OType.STRING, "Course_departure_index");
        setParameter("departureHour", course, OType.INTEGER, "Course_departureHour_index");
        setParameter("arrivalHour", course, OType.INTEGER, "Course_arrivalHour_index");

        scanner = new Scanner(System.in);
        while (true) {
            showMenu();
            switch (scanner.nextInt()) {
                case 0:
                    //zamkniecie sesji bazy danych
                    db.close();
                    //zamkniecie polaczenia z serwerem
                    orientdb.close();
                    System.out.println("Zakonczono");
                    return;
                case 1:
                    putNewCourse();
                    break;
                case 2:
                    updateCourse();
                    break;
                case 3:
                    deleteCourse();
                    break;
                case 4:
                    showCourseById();
                    break;
                case 5:
                    advancedDownload();
                    break;
                case 6:
                    processing();
                    break;
                case 7:
                    showAllCourses();
                    break;
            }
            stopSout();
        }
    }

    private static void showAllCourses() {
        String query = "SELECT * from Course";
        OResultSet rs = db.query(query);
        while (rs.hasNext()) {
            OResult row = rs.next();
            System.out.println("ID: " + row.getIdentity() + ", z " + row.getProperty("departure") +
                    ", do " + row.getProperty("destination") + ", o " + row.getProperty("departureHour")
                    + ", przyjazd o " + row.getProperty("arrivalHour"));
            //System.out.println("Rekord: " + row.getRecord() + "\n");
        }
        rs.close();
    }

    private static void processing() {
        showAllCourses();
        System.out.println("Wszystkie kursy powyzej podanej godziny zostana przesuniete o godzine:");
        System.out.println("Podaj godzine: ");
        int from = scanner.nextInt();
        String query = "SELECT * from Course WHERE departureHour > ?";
        OResultSet rs = db.query(query, from);
        while (rs.hasNext()) {
            OResult row = rs.next();
            System.out.println("Aktualizuje: " + row.getVertex());
            int oldDepHour = Integer.parseInt(row.getProperty("departureHour").toString());
            ++oldDepHour;
            String rid = row.getIdentity().toString();
            rid = rid.substring(rid.indexOf("#")+1, rid.indexOf("]") );
            db.command("UPDATE Course SET departureHour = " + oldDepHour + " WHERE @rid = " + rid);
            System.out.println("Zaktualizowano pomyslnie rekord o id: " + rid + "\n");
        }
        rs.close();
    }

    private static void advancedDownload() {
        scanner.nextLine();
        System.out.println("Podaj zapytanie SQL = orientDB: ");
        String query = scanner.nextLine();
        try {
            db.command(query);
        } catch (Exception e) {
            System.out.println("Wystapil wyjatek: " + e);
            return;
        }
        System.out.println("Pomyslnie wykonano: " + query);
    }

    private static void showCourseById() {
        showAllCourses();
        scanner.nextLine();
        System.out.println("Podaj ID kursu [np 52:0]:");
        String courseID = scanner.nextLine();
        courseID = "#" + courseID;
        String query = "SELECT * from Course";
        OResultSet rs = db.query(query);
        while (rs.hasNext()) {
            OResult row = rs.next();
            if (row.getIdentity().toString().equals("Optional[" + courseID + "]")) {
                System.out.println(row.getRecord());
                break;
            }
        }
        rs.close();
    }

    private static void updateCourse() {
        showAllCourses();
        scanner.nextLine();
        System.out.println("Podaj ktory kurs chcesz zaktualizowac [np 52:0]:");
        String toUpdate = scanner.nextLine();
        toUpdate = "#" + toUpdate;
        String destination, departure;
        int arrHour, depHour;
        String query = "SELECT * from Course";
        OResultSet rs = db.query(query);
        while (rs.hasNext()) {
            OResult row = rs.next();
            if (row.getIdentity().toString().equals("Optional[" + toUpdate + "]")) {
                System.out.println("Podaj miejsce wyjazdu: ");
                departure = scanner.nextLine();

                System.out.println("Podaj miejsce docelowe: ");
                destination = scanner.nextLine();

                System.out.println("Podaj godzine wyjazdu(jako int): ");
                depHour = scanner.nextInt();

                System.out.println("Podaj godzine przyjazdu(jako int): ");
                arrHour = scanner.nextInt();
                System.out.println("Aktualizuje: " + row.getVertex());
                db.command("UPDATE Course SET departure = \"" + departure + "\", destination = \"" + destination +
                        "\", departureHour = " + depHour + ", arrivalHour = " + arrHour + " WHERE @rid = " + toUpdate + ";");
                System.out.println("Zaktualizowano pomyslnie. \n");
                rs.close();
                return;
            }
        }
        System.out.println("Nie udalo sie zaktualizowac");
        rs.close();
    }

    private static void putNewCourse() {
        String destination, departure;
        int departureHour, arrivalHour;

        scanner.nextLine();
        System.out.println("Podaj miejsce wyjazdu: ");
        departure = scanner.nextLine();
        System.out.println("Podaj miejsce docelowe: ");
        destination = scanner.nextLine();
        System.out.println("Podaj godzine wyjazdu (jako int): ");
        departureHour = scanner.nextInt();
        System.out.println("Podaj godzine przyjazdu (jako int): ");
        arrivalHour = scanner.nextInt();

        createNewCourse(destination, departure, departureHour, arrivalHour);
    }

    private static void setParameter(String propertyName, OClass oClass, OType otype, String index) {
        if (!(oClass.getProperty(propertyName) == null))
            return;
        oClass.createProperty(propertyName, otype);
        oClass.createIndex(index, OClass.INDEX_TYPE.NOTUNIQUE, propertyName);
    }

    private static OVertex createNewCourse(String destination, String deparature, int departureHour, int arrivalHour) {
        OVertex result = db.newVertex("Course");
        result.setProperty("destination", destination);
        result.setProperty("departure", deparature);
        result.setProperty("departureHour", departureHour);
        result.setProperty("arrivalHour", arrivalHour);
        result.save();
        return result;
    }

    private static void showMenu() {
        System.out.print("\n2) Firma przewozowa\n\nWybierz operacje:\n" +
                "1.Zapisywanie\n2.Aktualizowanie\n3.Kasowanie\n4.Pobieranie po ID\n5.Wlasne zapytanie\n" +
                "6.Przetwarzanie\n7. Pokaz wszystkie kursy\n0.Zakoncz\n\nWpisz cyfre i zatwierdz enterem: ");
    }

    private static void stopSout() throws IOException {
        System.out.println("Wcisnij enter, aby kontynuowac...");
        System.in.read();
    }

    private static void deleteCourse() {
        showAllCourses();
        scanner.nextLine();
        System.out.println("Podaj ktory kurs chcesz usunac [np 52:0]:");
        String toDelete = scanner.nextLine();
        toDelete = "#" + toDelete;
        String query = "SELECT * from Course";
        OResultSet rs = db.query(query);
        while (rs.hasNext()) {
            OResult row = rs.next();
            if (row.getIdentity().toString().equals("Optional[" + toDelete + "]")) {
                System.out.println("Usuwam: " + row.getVertex());
                db.command("DELETE VERTEX FROM Course WHERE @rid = ?", toDelete);
                System.out.println("Usunieto pomyslnie");
                rs.close();
                return;
            }
        }
        System.out.println("Nie udalo sie usunac");
        rs.close();
    }
}

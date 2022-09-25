import java.util.ArrayList;
import java.util.Scanner;

public final class Utility {


    /*
    // Print the list objects in tabular format.
    System.out.println("-----------------------------------------------------------------------------");
    System.out.printf("%10s %30s %20s %5s %5s", "STUDENT ID", "EMAIL ID", "NAME", "AGE", "GRADE");
    System.out.println();
    System.out.println("-----------------------------------------------------------------------------");
    for(Student student: students){
        System.out.format("%10s %30s %20s %5d %5c",
                student.getId(), student.getEmailId(), student.getName(), student.getAge(), student.getGrade());
        System.out.println();
    }
    System.out.println("-----------------------------------------------------------------------------");
    */

    public static boolean check(){
        Scanner sc = new Scanner(System.in);
        String answer = "";
        System.out.println("Press Y for Yes and N for No");
        do{
            answer=sc.nextLine();
        } while(!answer.equals("Y") && !answer.equals("N"));
        if (answer.equals("Y"))
            return true;
        else
            return false;
    }

    public static String chooseType(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Choose the type of resource:");
        System.out.println("1-\t Light");
        System.out.println("2-\t Presence detector");
        String choose = sc.nextLine();
        if (Integer.valueOf(choose)==1)
            return "actuator";
        if (Integer.valueOf(choose)==2)
            return "sensor";
        System.out.println("ERROR: out of range");
        return null;
    }

    public static Boolean checkOptions(ArrayList<String> options, String choose){
        
        for(int i = 0 ; i< options.size(); i++){
            if (choose.equals(options.get(i))){
                return true;
            }
        }
        return false;

    }
    
    public static void menu(){
    
    System.out.println("-----------------------------------------------------------------------------");
        System.out.println("1-\t SHOW Light state");
        System.out.println("2-\t GET info from all devices");
        System.out.println("3-\t VIEW History of all devices");
        System.out.println("4-\t CHANGE state of light");
        System.out.println("5-\t CHANGE nickname of device");
        System.out.println("6-\t SHOW mode of operation of presence detectors");
        System.out.println("7-\t CHANGE mode of operation of presence detectors");

        System.out.println("Choose a number");

    }
}
